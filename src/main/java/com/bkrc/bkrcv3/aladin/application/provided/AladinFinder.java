package com.bkrc.bkrcv3.aladin.application.provided;

import com.bkrc.bkrcv3.aladin.application.request.AladinRequest;
import com.bkrc.bkrcv3.aladin.application.response.AladinBookPageResponse;
import com.bkrc.bkrcv3.aladin.application.response.AladinBookSearchResponse;
import com.bkrc.bkrcv3.aladin.domain.AladinBook;

import java.util.List;

public interface AladinFinder {
    List<AladinBook> getAladinItemList(AladinRequest request);
    List<AladinBook> searchBooks(String query);
    AladinBookPageResponse findAll();
    AladinBook getAladinBook(Integer itemId);
    AladinBook settingAladinDetail(String isbn13);
    void saveListForRedis(List<AladinBook> books);
}
