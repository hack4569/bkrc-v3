package bkrc.chaptersofu.application.hotbook.provided;

import bkrc.chaptersofu.domain.book.Book;

import java.util.List;

public interface HotBookFinder {
    // 인기 도서 조회 결과가 외부 공급자 타입에 의존하지 않도록 공통 Book을 반환한다.
    List<Book> readAll(String dateStr);
}
