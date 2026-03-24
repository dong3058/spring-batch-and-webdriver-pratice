package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.retry.RetryListener;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryState;
import org.springframework.core.retry.Retryable;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomRetryListener implements RetryListener {

    @Override
    public void onRetryableExecution(RetryPolicy retryPolicy, Retryable<?> retryable, RetryState retryState) {


        log.info("재시도중 횟수:{}-{}",retryState.getRetryCount(),retryable.getName());
    }
}
