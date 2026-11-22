package bkrc.chaptersofu.application.aladin.provided;

import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.book.Book;

import java.util.List;

public interface BookRecommender {
    List<Book> getRecommendBooksForUser(Long memberId);
}
