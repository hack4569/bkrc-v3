package com.bkrc.bkrcv3.config;

import com.bkrc.bkrcv3.aladin.application.AladinBookRepository;
import com.bkrc.bkrcv3.aladin.application.AladinService;
import com.bkrc.bkrcv3.aladin.client.AladinClient;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.aladin.entity.AladinConstants;
import com.bkrc.bkrcv3.batch.AladinApiItemProcessor;
import com.bkrc.bkrcv3.batch.AladinApiItemReader;
import com.bkrc.bkrcv3.batch.AladinApiItemWriter;
import com.bkrc.bkrcv3.batch.RefreshCacheTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.core.repository.JobRepository;
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
    private final AladinClient aladinClient;
    private final AladinBookRepository aladinBookRepository;
    private final AladinService aladinService;

    @Bean
    public Job aladinRecommendJob(Step aladinBooksFetchStep, Step refreshCacheStep) {
        return new JobBuilder("aladinBooksFetchStep", jobRepository)
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
        return new StepBuilder("fetchAladinBooksStep", jobRepository)
                .<AladinBook, AladinBook>chunk(AladinConstants.ITEM_LIST_PAGE, transactionManager)
                .reader(aladinApiItemReader)
                .processor(aladinApiItemProcessor)
                .writer(aladinApiItemWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(50)
                .build();
    }
}
