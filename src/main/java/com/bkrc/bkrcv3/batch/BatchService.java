package com.bkrc.bkrcv3.batch;

import com.bkrc.bkrcv3.aladin.application.AladinService;
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
    private final AladinService aladinService;

    @Scheduled(cron = "* 43 11 * * *", zone = "Asia/Seoul")
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

//            List<AladinBook> aladinBookList = new ArrayList<>();
//            List<AladinBook> successList = new ArrayList<>();
//            int retry = 0;
//            int maxRetry = 10;              // 최대 5회 재시도
//
//        var allBooksResponse = aladinService.findAll();
//        var registeredBooks = allBooksResponse.getAladinBookResponseList();
//        int page = 1;
//        while (retry < maxRetry) {
//            aladinBookList = aladinService.getBooksForRecommend(AladinRequest.builder().start(page).build(), registeredBooks);
//
//            if (CollectionUtils.isEmpty(aladinBookList)) {
//                retry ++;
//                page ++;
//                continue;
//            }
//            Set<Integer> successItemIds = successList.stream()
//                    .map(AladinBook::getItemId)
//                    .collect(Collectors.toSet());
//            boolean overlapsPriorSuccess = aladinBookList.stream()
//                    .map(AladinBook::getItemId)
//                    .anyMatch(id -> id != null && successItemIds.contains(id));
//            if (overlapsPriorSuccess) {
//                retry = maxRetry;
//                continue;
//            }
//            page++;
//            retry = 0;
//            successList.addAll(aladinService.saveNewAladinBooks(AladinRecommendSaveRequest.builder().newAladinBooks(aladinBookList).build()));
//        }
//        aladinService.saveListForRedis(successList);
//        if (!CollectionUtils.isEmpty(successList)) {
//            String itemIds = successList.stream()
//                    .map(book -> String.valueOf(book.getItemId()))
//                    .collect(Collectors.joining(","));
//            log.info("save success bookId : {}", itemIds);
//        }
    }

    private boolean isJobRunning() {
        return jobExplorer.findRunningJobExecutions("aladinRecommendJob")
                .stream()
                .anyMatch(exec -> exec.getStatus().isRunning());
    }
}
