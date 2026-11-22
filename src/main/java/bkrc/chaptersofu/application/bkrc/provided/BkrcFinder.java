package bkrc.chaptersofu.application.bkrc.provided;

import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.book.Book;

import java.util.List;

public interface BkrcFinder {
    List<Book> getBestSeller(BkrcRequest request);
    List<Book> searchBooks(String query);
    Book getBook(Long bookId);
    Book settingBookDetail(String isbn13);
}
