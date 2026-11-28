package bkrc.chaptersofu.application.aladin.provided;

import bkrc.chaptersofu.application.bkrc.provided.BkrcRequest;
import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.book.Book;

import java.util.List;

public interface AladinFinder {
    List<Book> getBestSellerBooks(BkrcRequest request);
    List<AladinBook> searchBooks(String query);
    AladinBook getAladinBook(Integer itemId);
    AladinBook settingAladinDetail(String isbn13);
}
