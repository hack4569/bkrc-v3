package bkrc.chaptersofu.adapter.webapi.dto;

import bkrc.chaptersofu.domain.aladin.BookComment;
import bkrc.chaptersofu.domain.aladin.SubInfo;
import bkrc.chaptersofu.domain.book.Book;
import bkrc.chaptersofu.domain.book.BookMall;

import java.util.List;

/**
 * 도서 공급자와 무관하게 공통으로 제공할 수 있는 도서 응답이다.
 * 엔티티를 직접 노출하지 않아 지연 로딩 필드와 내부 연관관계가 JSON 응답에 섞이는 것을 방지한다.
 */
public record BookResponse(
        Long bookId,
        Integer itemId,
        BookMall mallName,
        String title,
        String link,
        String author,
        String pubDate,
        String description,
        String isbn,
        String isbn13,
        String cover,
        Integer categoryId,
        String categoryName,
        String publisher,
        Boolean adult,
        Integer customerReviewRank,
        Integer bestRank,
        SubInfo subInfo,
        String fullDescription,
        String fullDescription2,
        String toc,
        List<BookComment> bookCommentList
) {

    public static BookResponse from(Book book) {
        // 공통 도서 엔티티의 필드만 복사해 알라딘과 YES24 모두 동일한 응답 계약을 사용한다.
        return new BookResponse(
                book.getBookId(),
                book.getItemId(),
                book.getMallName(),
                book.getTitle(),
                book.getLink(),
                book.getAuthor(),
                book.getPubDate(),
                book.getDescription(),
                book.getIsbn(),
                book.getIsbn13(),
                book.getCover(),
                book.getCategoryId(),
                book.getCategoryName(),
                book.getPublisher(),
                book.getAdult(),
                book.getCustomerReviewRank(),
                book.getBestRank(),
                book.getSubInfo(),
                book.getFullDescription(),
                book.getFullDescription2(),
                book.getToc(),
                book.getBookCommentList()
        );
    }
}
