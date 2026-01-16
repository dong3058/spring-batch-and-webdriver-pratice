package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.concurrent.*;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class TestingAopWIthRedissonLock {
    private final RedissonClient redissonClient;
    private final static String delStructureKey="del-structure-";
    private final BatchTestConfig batchTestConfig;
    private final PlatformTransactionManager transactionManager;

    @Around("@annotation(com.example.demo.TestLock)")
    public Object mongoRedissonLock(ProceedingJoinPoint point) throws Throwable{
        RLock rLock=redissonClient.getLock("testing");

        TransactionStatus transactionStatus=null;

        log.info("aop thread is virtual?:{}",Thread.currentThread().isVirtual());
        try{
            boolean rockState=rLock.tryLock(10000L,10000L, TimeUnit.MILLISECONDS);
            if(!rockState){
                throw new RuntimeException("대시시간 초과 발생, 재시도 해주세요");
            }
            log.info("redssion 트랜잭션 작동 시작");

          /*  DefaultTransactionDefinition transactionDefinition=new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
            transactionDefinition.setTimeout(5);

            transactionStatus=transactionManager.getTransaction(transactionDefinition);*/


            Object result= point.proceed();

            /*transactionManager.commit(transactionStatus);
            transactionDefinition = null;*/
            return result;
        }
        catch (Throwable e){

            /*if(transactionStatus!=null){
                transactionManager.rollback(transactionStatus);
            }*/

            log.info("최종 에러 처리:{}",e.getMessage());
            throw new RuntimeException(e.getMessage());
            //소켓 통신 에러 통합 컨트롤링 방법 생각하기.
        }
        finally {
            log.info("redssion lock 반납");
            if (rLock.isLocked() && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }
}
