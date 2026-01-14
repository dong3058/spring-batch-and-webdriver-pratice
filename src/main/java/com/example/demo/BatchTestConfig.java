package com.example.demo;


import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.aspectj.weaver.ast.ITestVisitor;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.beans.BeanProperty;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
//@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchTestConfig {


    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final IdRangePartition idRangePartition;


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
        asyncTaskExecutor.setConcurrencyLimit(5);
        asyncTaskExecutor.setThreadNamePrefix("async-thread-pool-");
        return asyncTaskExecutor;
    }

    @Bean
    public PartitionHandler partitionHandler(){

        val partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setTaskExecutor(executor());
        partitionHandler.setStep(rangeSlaveStep());
        partitionHandler.setGridSize(4);
        return partitionHandler;
    }

    @Bean
    public Step rangeMasterStep(){
        return new StepBuilder("rangeMasterStep",jobRepository)
                .partitioner("rangeSlaveStep",idRangePartition)
                .partitionHandler(partitionHandler())
                .build();
    }


    @Bean
    public Step rangeSlaveStep(){
        return new ChunkOrientedStepBuilder<TestBatch,TestBatch>("testChunkStep",jobRepository,10)
                .reader(customPagingItemReader())
                .transactionManager(transactionManager)
                .processor((member)->{
                    member.setAge(0L);
                    return member;
                })
                .writer(customJpaItemWriter())
                .listener(new ChunkTestListener())
                .faultTolerant()
                .skip(RuntimeException.class)
                .skipLimit(3)
                .build();
    }


    @Bean
    public Job rangePartitionJob(){
        return new JobBuilder("rangePartitionJob", jobRepository)
                .listener(new JobLoggerListener())
                .start(rangeMasterStep())
                .build();
    }




    /*@Bean
    public Job chunkTestJob( Step chunkTestStep) {
        return new JobBuilder("chunkTestJob", jobRepository)
                .listener(new JobLoggerListener())
                .start(chunkTestStep)
                .build();
    }

    @Bean
    public Step chunkTestStep() {

        return new ChunkOrientedStepBuilder<TestBatch,TestBatch>("testChunkStep",jobRepository,10)
                .reader(customPagingItemReader())
                .transactionManager(transactionManager)
                .processor((member)->{
                    member.setAge(0L);
                    return member;
                })
                .writer(customJpaItemWriter())
                .listener(new ChunkTestListener())
                .faultTolerant()
                .skip(RuntimeException.class)
                .skipLimit(3)
                //.taskExecutor(asyncTestExecutor())-->멀티 스레드 방식은 왜 잘 작동을 안하지 모르겟다 ㅇㅇ;
                //.retry(RuntimeException.class)
                //.retryLimit(3)
                .build();
    }*/



    @Bean
    @StepScope//bean을 언제 생성할지를 의미 즉 일반적인 싱글톤이아닌 각 step실행시에 생성되도록 한ㄴ다. 즉 step별로 분리된 형태이며
    //파티셔닝 구축에서 서로 스레드간의 안정성을 보장한다.-->이는 reader에서는 state라는 현재 진행상황을 기억하는데 쓰는 값이있기때문.
    //쉽게 말해서 읽다가 실패했을경우 어디까지 읽었는지 meta table에 기록하기위함이다.
    public JpaPagingItemReader<TestBatch> customPagingItemReader(){

        return new JpaPagingItemReaderBuilder<TestBatch>()
                .name("customPagingItemReader")
                .pageSize(10)
                .saveState(true)
                .entityManagerFactory(entityManagerFactory)
                .queryString("select m from TestBatch m")
                .build();
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
