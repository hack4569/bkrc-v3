package bkrc.chaptersofu.adapter.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@Component
public class BatchService {

    private final JobLauncher jobLauncher;
    private final Job aladinRecommendJob;
    private final JobExplorer jobExplorer;

    @Scheduled(cron = "0 00 03 * * *", zone = "Asia/Seoul")
    public void recommendScheduler() throws Exception {
        if (isJobRunning()) {
            log.info("알라딘 추천 배치의 이전 실행이 아직 진행 중이므로 이번 실행을 건너뜁니다.");
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
