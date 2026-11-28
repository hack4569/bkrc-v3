package bkrc.chaptersofu.application.bkrc;

import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.book.Book;

import java.util.List;

public interface BookCacheRegister {
    void saveListForRedis(List<Book> books);
}
