package bkrc.chaptersofu.application.aladin.provided;

import bkrc.chaptersofu.domain.aladin.AladinBook;

import java.util.List;

public interface AladinFinder {
    List<AladinBook> getAladinItemList(AladinRequest request);
    List<AladinBook> searchBooks(String query);
    List<AladinBook> findAll();
    AladinBook getAladinBook(Integer itemId);
    AladinBook settingAladinDetail(String isbn13);
    void saveListForRedis(List<AladinBook> books);
}
