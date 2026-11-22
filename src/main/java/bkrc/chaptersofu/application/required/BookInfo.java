package bkrc.chaptersofu.application.required;

import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.book.Book;

import java.util.List;

public interface BookInfo {
    List<Book> getBestSellerBooks(int maxResults, int start);

    Book getBookDetail(String isbn13);

    List<Book> searchBooks(String query);
}
