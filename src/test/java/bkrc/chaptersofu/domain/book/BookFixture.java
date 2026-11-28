package bkrc.chaptersofu.domain.book;

import bkrc.chaptersofu.domain.aladin.BookComment;
import bkrc.chaptersofu.domain.aladin.SubInfo;

import java.util.List;

/**
 * 테스트용 공통 도서 생성기.
 * Book은 22개 인자의 전체 생성자만 제공하므로 인자 순서 실수를 막기 위해 한 곳에서만 호출한다.
 * 생성자 인자 순서가 바뀌면 이 파일만 함께 수정하면 된다.
 */
public final class BookFixture {
    private BookFixture() {
    }

    public static Book book(Long bookId, String title, String author, String categoryName, String pubDate) {
        return book(bookId, title, author, categoryName, pubDate, null, null, null, null, null);
    }

    public static Book book(Long bookId, String title, String author, String categoryName, String pubDate,
                            SubInfo subInfo, String fullDescription, String fullDescription2, String toc,
                            List<BookComment> comments) {
        return new Book(bookId, 1000, BookMall.YES24, title, "https://example.com/" + bookId, author, pubDate,
                "소개", "1234567890", "9781234567890", "https://example.com/cover.jpg", 2105, categoryName,
                "출판사", false, 9, 1, subInfo, fullDescription, fullDescription2, toc, comments);
    }
}
