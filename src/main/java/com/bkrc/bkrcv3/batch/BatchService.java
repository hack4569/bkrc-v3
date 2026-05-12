package com.bkrc.bkrcv3.batch;

import com.bkrc.bkrcv3.aladin.application.AladinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class BatchService {

    private final JobLauncher jobLauncher;
    private final Job aladinRecommendJob;
    private final JobExplorer jobExplorer;

    @Scheduled(cron = "* * * * * *", zone = "Asia/Seoul")
    public void recommendScheduler() throws Exception{
        if (isJobRunning()) {
            System.out.println("이전 실행이 아직 진행 중입니다. 건너뜁니다.");
            return;
        }
        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDate("date", LocalDate.now())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(aladinRecommendJob, jobParameters);
    }

    private boolean isJobRunning() {
        return jobExplorer.findRunningJobExecutions("aladinRecommendJob")
                .stream()
                .anyMatch(exec -> exec.getStatus().isRunning());
    }
}
