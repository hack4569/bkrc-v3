package com.bkrc.bkrcv3.batch.application;

import com.bkrc.bkrcv3.aladin.application.AladinService;
import com.bkrc.bkrcv3.aladin.application.request.AladinRecommendSaveRequest;
import com.bkrc.bkrcv3.aladin.application.request.AladinRequest;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class BatchService {

    private final AladinService aladinService;

    //@Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void recommendScheduler() {
            List<AladinBook> aladinBookList = new ArrayList<>();
            List<AladinBook> successList = new ArrayList<>();
            int retry = 0;
            int maxRetry = 20;              // 최대 5회 재시도

        var allBooksResponse = aladinService.findAll();
        var registeredBooks = allBooksResponse.getAladinBookResponseList();
        int page = 1;
        while (retry < maxRetry) {
            aladinBookList = aladinService.getBooksForRecommend(AladinRequest.builder().start(page).build(), registeredBooks);

            if (CollectionUtils.isEmpty(aladinBookList)) {
                retry ++;
                page ++;
                continue;
            } else {
                page ++;
                retry = 0;
            }
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
