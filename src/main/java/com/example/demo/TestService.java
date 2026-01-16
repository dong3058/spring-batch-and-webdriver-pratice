package com.example.demo;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(timeout = 3)
// 서비스 로직의 전범위에 대한 시간을 카운트함.
// 정확히 말하면 서비스 로직이 실행되는 동안 해당 시간내에 로직내부의 트랜잭션 코드가 실행 완료되어야한다.
//해당 시간내에 서비스 로직 내부의 트랜잭션 이용코드가 모두 수행이된다면 롤백에러는 발생하지않음.
public class TestService {


    private final TestBatchRepository testBatchRepository;


    //대충 이렇게 보니까 트랜잭션 타임아웃이
    @TestLock
    public String testing() throws InterruptedException{
        //Thread.sleep(6000);-->롤백 에러 있음 왜냐 3초가 지난후에 save를 하려고 하기떄문.
        log.info("스레드 타입:{}",Thread.currentThread().isVirtual());
        TestBatch t=new TestBatch(12L);
        testBatchRepository.save(t);
        //Thread.sleep(6000);-->롤백 에러 있음 왜냐 find all이 timeout 시간 3초가 지난 이후에 db를 이용하려고하기떄문.
        //Thread.sleep(2800);-->롤백 에러없음-->왜냐면 3초내에 트랜잭션 이용코드가 전부다 수행되기때문.
        //2850이면 로컬에서 실행시 에러가 터지는대 mysql workbench기준으로 조회 쿼리가 0.3ms정도가 걸린다. 아마 어플리케이션단ㅇ까지
        //가져오는대 걸리는 시간도 포함되는거같다.
        //log.info("find all에서 에러가 발생하는가?");
        Long startTime=System.currentTimeMillis();
        testBatchRepository.findAll();
        Long endTime=System.currentTimeMillis();
        log.info("find all에서 에러가 발생안했내?:{}",endTime-startTime);
        //Thread.sleep(6000);-->롤백 에러 없음 왜냐면3초라고 주어진 시간동안 이미 트랜잭션을 이용하는 모든 코드를 지나쳤기때문.
        return "ok";
    }
}
