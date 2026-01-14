package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;

@Slf4j
public class JobLoggerListener implements JobExecutionListener {

    private static final String BEFORE_LOG = "{} Job is Running";
    private static final String AFTER_LOG = "{} Job is Finished. Status {}";
    private static final String FAILED_LOG = "{} Job is Failed";

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info(BEFORE_LOG, jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info(AFTER_LOG, jobExecution.getJobInstance().getJobName(), jobExecution.getStatus());

        if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.error(FAILED_LOG, jobExecution.getJobInstance().getJobName());
        }
    }
}