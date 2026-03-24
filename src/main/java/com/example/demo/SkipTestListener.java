package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SkipTestListener implements SkipListener<TestBatch,TestBatch> {


    @Override
    public void onSkipInProcess(TestBatch item, Throwable t) {
            log.info("skip 발생:{}",item.getId());
    }
}
