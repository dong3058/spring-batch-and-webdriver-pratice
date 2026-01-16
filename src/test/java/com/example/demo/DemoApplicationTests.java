package com.example.demo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@SpringBootTest
class DemoApplicationTests {


	@Autowired
	RedisTemplate redisTemplate;
	@Autowired
	TestBatchRepository testBatchRepository;

	@Autowired
	TestService testService;


	/*@Test
	void normalThread()throws InterruptedException{
		ExecutorService executorService= Executors.newFixedThreadPool(200);


		CountDownLatch countDownLatch=new CountDownLatch(200);
		Long startTime=System.currentTimeMillis();

		for(int i=0;200>i;i++){
			int val=i;
			executorService.submit(()->{
				try{

					redisTemplate.opsForValue().get("hello");
					List<TestBatch> testBatches=testBatchRepository.findAll();
					//Thread.sleep(500);
				}
				catch (Exception e){

				}
				finally {
					countDownLatch.countDown();
				}
			});

		}
		countDownLatch.await();
		Long endTime=System.currentTimeMillis();
		executorService.shutdown();
		System.out.println(endTime-startTime);
	}




	@Test
	void contextLoads()throws InterruptedException{
		ExecutorService executorService= Executors.newVirtualThreadPerTaskExecutor();
		CountDownLatch countDownLatch=new CountDownLatch(200);

		Long startTime=System.currentTimeMillis();

		for(int i=0;200>i;i++){
			int val=i;
			executorService.submit(()->{
				try{

					redisTemplate.opsForValue().get("hello");
					List<TestBatch> testBatches=testBatchRepository.findAll();
					//Thread.sleep(500);
				}
				catch (Exception e){

				}
				finally {
					countDownLatch.countDown();
				}
			});

		}
		countDownLatch.await();
		executorService.shutdown();
		Long endTime=System.currentTimeMillis();
		System.out.println(endTime-startTime);

	}*/

	//가상 스레드가 작업시간이 훨씬 빨리끝난다.


	@Test
	void testRedissonLock()throws InterruptedException{
		testService.testing();

	}
}
