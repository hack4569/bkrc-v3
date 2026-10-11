package bkrc.chaptersofu.application.required;

import bkrc.chaptersofu.domain.aladin.AladinBook;

import java.util.List;

public interface BookInfo {
    List<AladinBook> getBooks(String queryType, int maxResults, int start);

    AladinBook getBookDetail(String isbn13);

    List<AladinBook> searchBooks(String query);
}
