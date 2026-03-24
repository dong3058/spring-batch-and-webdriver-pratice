package com.example.demo;


import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.aspectj.weaver.ast.ITestVisitor;
import org.redisson.api.LockOptions;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.MulticasterBatchListener;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JpaCursorItemReader;
import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.infrastructure.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.beans.BeanProperty;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@RequiredArgsConstructor
public class BatchTestConfig {


    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final IdRangePartition idRangePartition;
    private final CustomRetryPolicy customRetryPolicy;
    private final CustomRetryListener customRetryListener;
    private final SkipTestListener skipTestListener;


    /*
    * 스프링 배치는 어떻게 처리할것인가를 의미하고, 스케쥴러는 언제처리 할것인가를 의미.
    * 스프링 배치의 의미는 처리작업에 대한 신뢰성 보장이다.
    *
    * job-->n개의 step-->각 스텝당 chunk단위로 or tasklket으로 처리고 구성된다.
    *
    * 즉 step이 모여서 1개의 job이되고 1개의 step을 처리하는 방식으로 chunk or tasklet 2가지방식으로 나뉜다.
    * 이때 chunk는 1개의 step에서 처리하는 데이터를 n개의 chunk에 나눠서 주는 방식을 의미함.
    * 즉 1개의 스탭에서 100개의 데이터를 처리해야될경우 4개의 chunk로 나누면 각청크는 25개씩 맡아서 처리한다 이말이다.
    * 이때 각 청크는 독립적인 트랜잭션을 가지게된다.
    *
    * tasklet은 step을 그냥 단순하게 1개로처리하는 작업-->대용량 데이터보단 단순하게 파일 입출력같은 곳에 쓰인다고 보면될듯.
    *
    * 음사실 청크 기반 처리도 ChunkOrientedTasklet이 tasklet기반으로 하는대 중요한건 한꺼번에 처리하냐, 나눠서 단위처리하냐
    * 이개념으로 보면될거같음.
    * */


    /*
    * 멀티 스레드와 파티셔닝의 차이
    *
    * 멀티 스레드는 1개의 step을 여러 스레드로 처리한다는말이다.
    *
    * 그에 반해 파티셔닝은 1개의 step을 n개의 sub step으로 분할해서 각기 다른 스레드들이 각각의 step을 맡아서 처리한다는말.
    *
    * 둘이 비슷해보이지만 전자는 1개의 step을 여러 스레드가 공유해서 처리하는거라 한과정에서 에러발생시 step전체가 다운되지만
    *
    * 파티셔닝은 1개의 스텝을 n개로 분해해서 substep으로 다루므로 substep중 1개가 다운되도 나머지 substep은 정상적으로 돌아간다.
    *
    *  쉽게 말해서 스레드에 chunk를 부여하느가 혹은 chunk가 아니라 substep자체를 부여하는가 이차이이다.
    *
    * https://jgrammer.tistory.com/entry/Spring-Batch-%EC%96%B4%EB%96%A4-%EB%B3%91%EB%A0%AC-%EC%B2%98%EB%A6%AC-%EB%B0%A9%EC%8B%9D%EC%9D%84-%EC%84%A0%ED%83%9D%ED%95%B4%EC%95%BC%ED%95%A0%EA%B9%8C
    *
    *
    * 아래에 멀티스레드 코드가있긴한대 멀티스레드 방식은 정확히 로그도 안나오고 실행은되는거같은대 잘모르겠다.
    * */

    /*
    * item:처리할 데이의 가장 작은 구성요소 쉽게 말해서 db로부터 읽어오는 데이터들중 1개의 데이터를 말한다고 봐도된다.
    *
    * item reader:각 입력 으로부터 데이터를 읽어오는것을 수행하는 인터페이스. 보통은 db겠지만 interace이므로 파일로부터
    * 읽어오는 식으로 구현도 가능하다.
    *  ItemReader 는 다양한 구현체들이 구현 -> ex : JdbcPagingItemReader, JpaPagingItemReader-->이런 다양한 구현체가 존재함.
    *
    *
    * item writer:청크 단위로 item reader로부터 데이터를 받아서 일괄 출력작업을 하는 인터페이스
    *
    *
    * item processor: reader로부터 받은 데이터를 변형,가공해서 wirter로 넘기는 인터페이스.쉽게 말해서 여기서 update를 해주고
    * wirter는 실질적으로 db상에 저장하도록ㄹ 전송하는역할을 한다고보면된다.
    * 데이터의 변환 or 필터를 담당한다고 보면됨.
    *
    * chunkorientedtasklet 환경에서 processor외에 wirter,reader는 필수로 설정해야된다.
    *
    * https://dev-jwblog.tistory.com/195#article-3-1--3-0)-item  참고 링크
    *
    * */

    /*
    * skip:step 단위로 카운트즉 skip이 10이 설정되어있을경우 step에 대해서 몇개의 청크든간에 한꺼ㅃ너에 합쳐서 총 10이넘으면
    * 해당 step은 실패처리
    *
    * retry:item단위 즉 처리하는 item별로 몇번 retry까지 할건지를 의미한다.
    *
    * start: 시작횟수인대 이건 부여된 step별로 관리된다.-->아무것도 안하고 맨처음시에도 1개씩 까인다.
    * 만약 시작이 발동되면 다시 job을 실행한다. 이때 step이 가지는 limit은 계속해서 누적해서 기록이된다.
    *
    * 이 옵션은 step별로 failed상태인것들에 대해서 적용되는것들이다.
    *특정 step 단계에서 failed가 되버리면 다시 job을 재가동하는것.
    *
    * 만약 step1이 limit은 1이고 step2는 limit이 2라고 가정하자.
    * step1이 재시작을 한번패서 패스했을경우 가능한 시도횟수는 0이다.
    * 이떄 step2가 실패하고 allowifcomplete옵션이 켜져있어서 성공한 스탭도 다시 시도하는 재시작을 할경우
    * step1이 이과정에서 한번더 실패하면 더이상의 재시도 횟수가 없으므로 에러가 발생하는 형식이다.
    *
    * 파티셔닝 환경은 조금 복잡하다.
    * 1개의step이 곧 여러개의 substep으로 기록된다.
    * 이 substep중 1개만 실패해도 step은 failed로 기록된다. 물론 성공한 substep들은 complete로 기록된다.
    * 이 start값은 각각 main step과substep별로 걸수있다.
    * substep에 매겨진 start는 substep에 따로 기록되는거고, mainstep에 기록된 start는 amin step으로 기록되는값이다.
    * 또한 allow if complete도 마찬가지.
    *
    * 만약 1개의 mainstep의 substep이 failed 처리가 발생했을 경우 mainstep은 failed처리가되서 main step의
    * start횟수를 소진하면서 substep을 실행한다. 이때 substep 별로 allow if complete가 true값일경우 모든 substepㅇ은
    * 각각의 재시도 횟수를 소진하면서 다시 시작한다.
    *
    *   MasterStep(startLimit=3), SubStep(startLimit=2), allowStartIfComplete=true

  1번째 실행:
    MasterStep 시작 (1/3 소진)
      partition-0 성공 (1/2 소진)
      partition-1 성공 (1/2 소진)
      partition-2 실패 (1/2 소진)
    MasterStep FAILED

  2번째 실행 (재시작):
    MasterStep 시작 (2/3 소진)
      partition-0 재실행 (allowStartIfComplete=true) (2/2 소진)
      partition-1 재실행 (allowStartIfComplete=true) (2/2 소진)
      partition-2 재실행 (2/2 소진) → 성공
    MasterStep COMPLETED

  3번째 실행 (다른 이유로 재시작):
    MasterStep 시작 (3/3 소진)
      partition-0 재실행 시도 → StartLimitExceededException (2/2 이미 소진)
    *
    *재시작시에는 같은 job 파리미터를 가지는 새 job으로 시작하나, db상에 기록되는 job은 job execution id는 다르게 기록된다.
    * 즉 job이라는 인스턴스는 같되, 그 실행하는 execution이 달라진다(id만)
    *
    *
    *   JobInstance는 Job 이름 + JobParameter 조합으로 식별되므로:

  BATCH_JOB_INSTANCE
  job_instance_id | job_name      | (내부적으로 파라미터 해시 저장)
  1               | chunkTestJob  | id=1111  ← JobInstance A
  2               | chunkTestJob  | id=2222  ← JobInstance B (다른 파라미터 → 다른 Instance)

  BATCH_JOB_EXECUTION
  job_execution_id | job_instance_id | status
  1                | 1               | FAILED
  2                | 1               | COMPLETED  ← 재시작 (같은 Instance A)
  3                | 2               | COMPLETED  ← 새 파라미터 (새 Instance B)
  *
  * 이런꼴.
  *
  * 추가적으로 restart는 내가 알아서 가동시켜줘야된다. 즉 job실패한다고 자동재시작이 아니라 재시작하는 코드를 넣어줘야한다.
    * */



    @Bean(name ="taskPool")
    public TaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor(); // (2)
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("multi-thread-");
        executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
        executor.initialize();
        return executor;
    }
   @Bean(name="asyncTaskExe")
    public SimpleAsyncTaskExecutor asyncTestExecutor(){
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setVirtualThreads(true);
        asyncTaskExecutor.setConcurrencyLimit(20);
        asyncTaskExecutor.setThreadNamePrefix("virtual-thread-pool-");
        return asyncTaskExecutor;
    }

    @Bean
    public PartitionHandler partitionHandler(){

        val partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setTaskExecutor(asyncTestExecutor());
        partitionHandler.setStep(rangeSlaveStep());//substep을 지정함.
        partitionHandler.setGridSize(4);//-->몇개의 sub step으로 main step을 나눌건지를 의미하는값.
        return partitionHandler;
    }

    @Bean
    public Step rangeMasterStep(){
        return new StepBuilder("rangeMasterStep",jobRepository)
                .partitioner("rangeSlaveStep",idRangePartition)
                .partitionHandler(partitionHandler())
                .startLimit(2)
                .build();
    }


    @Bean
    public Step rangeSlaveStep(){
        return new ChunkOrientedStepBuilder<TestBatch,TestBatch>("rangeSlaveStep",jobRepository,1)
                .reader(customPagingItemReader(null, null))
                .transactionManager(transactionManager)
                .processor((member)->{
                    //throw new RuntimeException();
                    member.setAge(0L);
                    return member;
                })
                .writer(customJpaItemWriter())
                .listener(new ChunkTestListener())
                .faultTolerant()
                .skip(RuntimeException.class)
                .skipLimit(3)
                .skipListener(skipTestListener)
                .retryPolicy(customRetryPolicy)
                .retryListener(customRetryListener)
                .startLimit(2)  //몇번 재시도를 허용하는가
                .allowStartIfComplete(false)//완료된 스탭은 스킵 true라면 job재실행시 모든 step을 다시시도함.
                .build();
    }


    @Bean
    public Job rangePartitionJob(){
        return new JobBuilder("rangePartitionJob", jobRepository)
                .listener(new JobLoggerListener())
                .start(rangeMasterStep())
                .build();
    }


    @Bean
    public Job chunkTestJob( Step chunkTestStep) {
        return new JobBuilder("chunkTestJob", jobRepository)
                .listener(new JobLoggerListener())
                .start(chunkTestStep)
                .build();
    }

    @Bean
    public Step chunkTestStep() {


        return new ChunkOrientedStepBuilder<TestBatch,TestBatch>("testChunkStep",jobRepository,1)
                .reader(customPagingItemReader(null, null))
                .transactionManager(transactionManager)
                .processor((member)->{
                    member.setAge(0L);
                    return member;
                })
                .writer(customJpaItemWriter())
                .faultTolerant()
                .skip(RuntimeException.class)
                .skipLimit(3)
                .taskExecutor(asyncTestExecutor())
                .listener(new ChunkTestListener())
                //.retry(RuntimeException.class)
                //.retryLimit(3)
                .build();
    }



    @Bean
    @StepScope//bean을 언제 생성할지를 의미 즉 일반적인 싱글톤이아닌 각 step실행시에 생성되도록 한ㄴ다. 즉 step별로 분리된 형태이며
    //파티셔닝 구축에서 서로 스레드간의 안정성을 보장한다.-->이는 reader에서는 state라는 현재 진행상황을 기억하는데 쓰는 값이있기때문.
    //쉽게 말해서 읽다가 실패했을경우 어디까지 읽었는지 meta table에 기록하기위함이다.
    public JpaPagingItemReader<TestBatch> customPagingItemReader(
            @Value("#{stepExecutionContext['startId'] ?: null}") Long startId,
            @Value("#{stepExecutionContext['endId'] ?: null}") Long endId){

        /*
        *  null을넣어도 어차피 상관없이 실행시에 step의 context로 부터 뽑아낸값을 넣어서 진행한다.
        * */

        JpaPagingItemReaderBuilder<TestBatch> builder = new JpaPagingItemReaderBuilder<TestBatch>()
                .name("customPagingItemReader")
                .pageSize(10)
                .saveState(false)
                .entityManagerFactory(entityManagerFactory);

        if (startId != null && endId != null) {
            builder.queryString("select m from TestBatch m where m.id between :startId and :endId")
                   .parameterValues(Map.of("startId", startId, "endId", endId));
        } else {
            builder.queryString("select m from TestBatch m");
        }

        return builder.build();
    }


    @Bean
    public JpaCursorItemReader<TestBatch> customCursorItemReader(){
        return new JpaCursorItemReaderBuilder<TestBatch>()
                .name("customCursorItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select m from TestBatch m")
                .build();
    }


    @Bean
    public JpaItemWriter<TestBatch> customJpaItemWriter(){
        return new JpaItemWriterBuilder<TestBatch>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }




}
/**
 *
 *
 * 멀티 스레드 배치와 파티셔닝은 조금 성격이 다른대 전자는 1개의 job에 대해서 여러스레드가 돌아가면서 병렬처리하는거고
 * -->즉 청크 단위이면 1개의 step속에서 여러개의 청크를 여러개의 스레드가 처리.
 * 후자는 1개의 step을 n개의 스레드에 서브 step으로 분할해서 각각 처리하게 하는것이다.-->1개의 스레드가 서브 step전체를 총괄
 *
 *
 * chunk방식은 1개의 스텝을 여러청크로 분할해서 진행하는건대 처리해야될 대상이 100개, chunk가4개인 경우
 * 1개의 스텝에서 100개의 데이터를 25개로 분할해서 다룬다는 의미이다.
 * 이떄 트랜잭션 범위는 1개의 청크당 1개씩 독립적으로 부여된다.
 * 설정에 따라서 1개의 청크에서 에러가 발생할경우 그 청크만 에러처리 즉 롤백을 하고 나머지를 그대로 진행하던가(skip,retry)
 * 혹은 아예 그청크부터 그이후까지의 작업을 중단시키든가 할수있다(기본설정)
 *skip과 retry는 정해진 횟수제한이있는대 이는 청크 단위가 아니라 1개의 step단위이다.
 * 즉 10개의 청크로 구성된 1개의 스텝에 skip이3번제한이면 10개의 chunk통틀어서 에러를 3번만 봐준다 이렇게보면된다.
 *
 * 만약 파티셔닝에서 1개의 서브스텝에서 에러가 일어나서 정지되도 이는 각 다른 스레드에 영향을 안준다.
 * 반대로 멀티스레드는 1개의 step에서 여러스레드가 협업하는 형태라 에러발생시 전체가 다운된다.
 **/
