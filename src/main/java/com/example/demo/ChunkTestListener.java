package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.infrastructure.item.Chunk;


@Slf4j
public class ChunkTestListener implements ChunkListener {

    @Override
    public void beforeChunk(Chunk chunk) {
        log.info("청크 단위 작업 시작");
    }

    @Override
    public void afterChunk(Chunk chunk) {
        log.info("thread name:{}",Thread.currentThread().getName());
        log.info("청크 단위 작업 종료");
    }

    @Override
    public void onChunkError(Exception exception, Chunk chunk) {
        log.info("청크 작업중 에러발생:{}",exception.getMessage());
    }
}
