package com.bkrc.bkrcv3.batch;

import com.bkrc.bkrcv3.aladin.application.AladinBookRepository;
import com.bkrc.bkrcv3.aladin.application.AladinService;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshCacheTasklet implements Tasklet {

    private final AladinService aladinService;
    private final AladinBookRepository aladinBookRepository;
    private final DiverseBookOrderer diverseBookOrderer;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        var all = aladinBookRepository.findAll();
        var diverseBooks = diverseBookOrderer.order(all);
        aladinService.saveListForRedis(diverseBooks);
        log.info("[Batch] 장르 분산 캐시 갱신 완료 count={}", diverseBooks.size());
        return RepeatStatus.FINISHED;
    }
}
