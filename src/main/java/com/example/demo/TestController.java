package com.example.demo;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
//@EnableJdbcJobRepository
public class TestController {

    private final TestBatchRepository testBatchRepository;

    //private final WebDriverConfig webDriverConfig;
    private final static String oliveYoungUrl="https://www.oliveyoung.co.kr/store/goods/getGoodsDetail.do?goodsNo=";
    private final static String oliveYoungUrl2="&dispCatNo=100000100010015&trackingCd=Cat100000100010015_Small&t_page=%EC%B9%B4%ED%85%8C%EA%B3%A0%EB%A6%AC%EA%B4%80&t_click=%ED%81%AC%EB%A6%BC_%EC%A0%84%EC%B2%B4__%EC%83%81%ED%92%88%EC%83%81%EC%84%B8&t_number=1";
    private final static String className="li.Accordion_accordion-item__2__Xg:first-child .Accordion_content__alya4";
    private final static String childName=".table-area";


    private final static String productListUrl="https://www.oliveyoung.co.kr/store/display/getMCategoryList.do?dispCatNo=100000100010015&fltDispCatNo=&prdSort=01&pageIdx=1";
    private final static String productListUrl2="&rowsPerPage=24&searchTypeSort=btn_thumb&plusButtonFlag=N&isLoginCnt=0&aShowCnt=0&bShowCnt=0&cShowCnt=0&trackingCd=Cat100000100010015_Small&amplitudePageGubun=&t_page=%EC%B9%B4%ED%85%8C%EA%B3%A0%EB%A6%AC%EA%B4%80&t_click=%EC%B9%B4%ED%85%8C%EA%B3%A0%EB%A6%AC%ED%83%AD_%EC%A4%91%EC%B9%B4%ED%85%8C%EA%B3%A0%EB%A6%AC&midCategory=%ED%81%AC%EB%A6%BC&smallCategory=%EC%A0%84%EC%B2%B4&checkBrnds=&lastChkBrnd=&t_1st_category_type=%EB%8C%80_%EC%8A%A4%ED%82%A8%EC%BC%80%EC%96%B4&t_2nd_category_type=%EC%A4%91_%ED%81%AC%EB%A6%BC";

    private final Job chunkTestJob;
    private final Job rangePartitionJob;
    private final JobOperator jobOperator;
    private final BathRestartService bathRestartService;
    private final JobRepository jobRepository;



    @GetMapping("/batch/run")
    public String runJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("id", System.currentTimeMillis())
                    .toJobParameters();

            jobOperator.start(chunkTestJob, jobParameters);
            return "Batch Job Started";
        } catch (Exception e) {
            return "Failed: " + e.getMessage();
        }
    }

    @GetMapping("/batch/run/range")
    public void rangePartitionJob() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("id", System.currentTimeMillis())
                .toJobParameters();
        try {

            log.info("jobRepository class: {}", jobRepository.getClass().getName());
            log.info("current mvc thread name:{}",Thread.currentThread().getName());
            log.info("current mvc thread is virtual?:{}",Thread.currentThread().isVirtual());


            //job을 Job 이름 + Job Parameter 로 1개의 인스턴스를 구분 즉 같은 이름에 같은 데이터로 구성된 파라미터는
            //같은 job인스턴스로 본다 이말.
            JobExecution jobExecution=jobOperator.start(rangePartitionJob, jobParameters);
            JobParameters restartParameters = new JobParametersBuilder()
                    .addLong("id", System.currentTimeMillis())
                    .toJobParameters();
            bathRestartService.autoRestart(restartParameters);
            log.info("job success");
            //return "Batch Range Job Started";
        } catch (Exception e) {
            log.info("job Failed: " + e.getMessage());

        }
    }



    @GetMapping("/make/testData")
    public String make(){
        for(long i=1;21>i;i++){
            TestBatch testBatch=new TestBatch(i);
            testBatchRepository.save(testBatch);
        }

        return  "ok";
    }


    @GetMapping("/test")
    public String testController(){
        try {
            // HTML 문서 요청 및 파싱
            Document document = Jsoup.connect(oliveYoungUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7") // 한국어 설정 추가 (권장)
                    .referrer("https://www.google.com")
                    .timeout(60_000).get();
            log.info("{}",document.html());
        } catch (Exception e) {
            log.error("Error occurred while crawling events: {}", e.getMessage(), e);
        }
        return "ok";

    }


}
