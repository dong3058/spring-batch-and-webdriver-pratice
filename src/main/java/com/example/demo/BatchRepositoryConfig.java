package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing        // 공통 배치 인프라 설정
@EnableJdbcJobRepository(tablePrefix = "batch_")      // JDBC 기반 JobRepository 활성화 (이게 핵심!) 이옵션이 잇어야 db에
// meta 데이터가 기록이되고 tableprefix는 psotgresql에 소문자로 기록되기에 저렇게 해준거임.
public class BatchRepositoryConfig {


}
