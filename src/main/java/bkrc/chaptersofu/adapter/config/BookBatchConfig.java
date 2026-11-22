package bkrc.chaptersofu.adapter.config;

import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.aladin.AladinConstants;
import bkrc.chaptersofu.adapter.batch.BookItemProcessor;
import bkrc.chaptersofu.adapter.batch.BookItemReader;
import bkrc.chaptersofu.adapter.batch.BookItemWriter;
import bkrc.chaptersofu.adapter.batch.RefreshCacheTasklet;
import bkrc.chaptersofu.domain.aladin.AladinClientException;
import bkrc.chaptersofu.domain.book.Book;
import bkrc.chaptersofu.domain.book.BookConstants;
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
public class BookBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BookItemReader bookItemReader;
    private final BookItemProcessor bookItemProcessor;
    private final BookItemWriter bookItemWriter;
    private final RefreshCacheTasklet refreshCacheTasklet;

    @Bean
    public Job bookRecommendJob(Step booksFetchStep, Step refreshCacheStep) {
        return new JobBuilder("bookRecommendJob", jobRepository)
                .start(booksFetchStep)
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
    public Step booksFetchStep() {
        return new StepBuilder("booksFetchStep", jobRepository)
                .<Book, Book>chunk(BookConstants.ITEM_LIST_PAGE, transactionManager)
                .reader(bookItemReader)
                .processor(bookItemProcessor)
                .writer(bookItemWriter)
                .faultTolerant()
                .skip(Exception.class)
                .noSkip(AladinClientException.class)
                .skipLimit(50)
                .build();
    }
}
