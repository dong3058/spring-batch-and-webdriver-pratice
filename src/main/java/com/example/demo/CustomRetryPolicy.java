package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.BackOffExecution;
import org.springframework.util.backoff.ExponentialBackOff;

import java.time.Duration;
@Component
public class CustomRetryPolicy implements RetryPolicy {
    @Override
    public boolean shouldRetry(Throwable throwable) {

        return throwable instanceof RuntimeException;
    }



    @Override
    public Duration getTimeout() {
        return Duration.ofSeconds(10L);
    }

    @Override
    public BackOff getBackOff() {
        ExponentialBackOff backOff=new ExponentialBackOff();

        //지수 증가식인대 최대 증가가능한 interval을 말한다.
        backOff.setMaxInterval(3000L);
        backOff.setMultiplier(2.0);
        backOff.setInitialInterval(1000L);
        backOff.setMaxElapsedTime(5000L);
        return backOff;
    }
}
