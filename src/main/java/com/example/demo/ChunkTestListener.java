package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.ItemWriteListener;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.listener.StepListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;


@Slf4j
public class ChunkTestListener implements StepExecutionListener {


    @Override
    public void beforeStep(StepExecution stepExecution) {

       log.info("stepName:{}",stepExecution.getStepName());
        /*log.info("thread name:{}",Thread.currentThread().getName());
        log.info("작업사이즈:{}",items.getItems().size());
        log.info("current chunk thead is virtual?:{}",Thread.currentThread().isVirtual());
        log.info("청크 단위 작업 종료");*/

    }


}
