package bkrc.chaptersofu.adapter.config;

import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.aladin.AladinConstants;
import bkrc.chaptersofu.adapter.batch.AladinApiItemProcessor;
import bkrc.chaptersofu.adapter.batch.AladinApiItemReader;
import bkrc.chaptersofu.adapter.batch.AladinApiItemWriter;
import bkrc.chaptersofu.adapter.batch.RefreshCacheTasklet;
import bkrc.chaptersofu.domain.exception.AladinClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
@Configuration
@RequiredArgsConstructor
public class AladinBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AladinApiItemReader aladinApiItemReader;
    private final AladinApiItemProcessor aladinApiItemProcessor;
    private final AladinApiItemWriter aladinApiItemWriter;
    private final RefreshCacheTasklet refreshCacheTasklet;

    @Bean
    public Job aladinRecommendJob(Step aladinBooksFetchStep, Step refreshCacheStep) {
        return new JobBuilder("aladinRecommendJob", jobRepository)
                .start(aladinBooksFetchStep)
                .next(refreshCacheStep)
                .build();
    }

    @Bean
    public Step refreshCacheStep() {
        return new StepBuilder("refreshCacheStep", jobRepository)
                .tasklet(refreshCacheTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step aladinBooksFetchStep() {
        return new StepBuilder("aladinBooksFetchStep", jobRepository)
                .<AladinBook, AladinBook>chunk(AladinConstants.ITEM_LIST_PAGE, transactionManager)
                .reader(aladinApiItemReader)
                .processor(aladinApiItemProcessor)
                .writer(aladinApiItemWriter)
                .faultTolerant()
                .skip(Exception.class)
                .noSkip(AladinClientException.class)
                .skipLimit(50)
                .build();
    }
}
