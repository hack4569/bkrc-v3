package com.bkrc.bkrcv3.batch.application;

import com.bkrc.bkrcv3.aladin.application.AladinService;
import com.bkrc.bkrcv3.aladin.application.request.AladinRecommendSaveRequest;
import com.bkrc.bkrcv3.aladin.application.request.AladinRequest;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class BatchService {

    private final AladinService aladinService;

    //@SchedulerLock(name = "recommendScheduler", lockAtMostFor = "10m", lockAtLeastFor = "1m")
    @Scheduled(cron = "0 10 11 * * *", zone = "Asia/Seoul")
    public void recommendScheduler() {
            List<AladinBook> aladinBookList = new ArrayList<>();
            List<AladinBook> successList = new ArrayList<>();
            int retry = 0;
            int maxRetry = 10;              // 최대 5회 재시도

        var allBooksResponse = aladinService.findAll();
        var registeredBooks = allBooksResponse.getAladinBookResponseList();
        int page = 1;
        while (retry < maxRetry) {
            aladinBookList = aladinService.getBooksForRecommend(AladinRequest.builder().start(page).build(), registeredBooks);

            if (CollectionUtils.isEmpty(aladinBookList)) {
                retry ++;
                page ++;
                continue;
            }
            Set<Integer> successItemIds = successList.stream()
                    .map(AladinBook::getItemId)
                    .collect(Collectors.toSet());
            boolean overlapsPriorSuccess = aladinBookList.stream()
                    .map(AladinBook::getItemId)
                    .anyMatch(id -> id != null && successItemIds.contains(id));
            if (overlapsPriorSuccess) {
                retry = maxRetry;
                continue;
            }
            page++;
            retry = 0;
            successList.addAll(aladinService.saveNewAladinBooks(AladinRecommendSaveRequest.builder().newAladinBooks(aladinBookList).build()));
        }
        aladinService.saveListForRedis(successList);
        if (!CollectionUtils.isEmpty(successList)) {
            String itemIds = successList.stream()
                    .map(book -> String.valueOf(book.getItemId()))
                    .collect(Collectors.joining(","));
            log.info("save success bookId : {}", itemIds);
        }
    }
}
