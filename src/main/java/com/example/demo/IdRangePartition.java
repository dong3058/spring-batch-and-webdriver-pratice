package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdRangePartition implements Partitioner {

    private final TestBatchRepository testBatchRepository;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        long totCount=testBatchRepository.count();
        long partitionSize = (totCount / gridSize);
        Map<String,ExecutionContext> partitions = new HashMap<>();

        for (int i=0;gridSize>i;i++) {
            ExecutionContext context =new ExecutionContext();
            long startId = i * partitionSize + 1;
            long endId = (i == gridSize - 1) ? totCount:  (i + 1) * partitionSize ;

            context.putLong("startId", startId);
            context.putLong("endId", endId) ;
            context.putInt("partitionNumber", i)   ;
            partitions.put("partitions-"+i,context);//이거 컨텍스트별 이름을 반드시 다르게 해줘야된다.
        }

        log.info("파티셔닝 완료:{}--{}",totCount,partitionSize);
        return partitions;
    }
}
