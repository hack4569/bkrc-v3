package bkrc.chaptersofu.adapter.client.aladin.mapper;

import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.book.Book;
import bkrc.chaptersofu.domain.book.BookMall;

import java.util.List;
import java.util.Objects;

/**
 * 알라딘 전용 도서 모델을 애플리케이션의 공통 도서 모델로 변환한다.
 * 외부 서비스별 변환 책임을 어댑터 안에 두어 공통 도메인이 알라딘 타입에 의존하지 않도록 한다.
 */
public final class AladinBookMapper {

    private AladinBookMapper() {
        // 상태가 없는 정적 매퍼이므로 불필요한 인스턴스 생성을 막는다.
    }

    /**
     * 알라딘 도서 한 권을 공통 도서로 변환한다.
     * 필수 변환 대상이 누락된 경우 원인을 호출 지점 가까이에서 확인할 수 있도록 즉시 실패시킨다.
     */
    public static Book toBook(AladinBook source) {
        Objects.requireNonNull(source, "변환할 알라딘 도서는 null일 수 없습니다.");

        return new Book(
                null,
                source.getItemId(),
                BookMall.ALADIN,
                source.getTitle(),
                source.getLink(),
                source.getAuthor(),
                source.getPubDate(),
                source.getDescription(),
                source.getIsbn(),
                source.getIsbn13(),
                source.getCover(),
                source.getCategoryId(),
                source.getCategoryName(),
                source.getPublisher(),
                source.getAdult(),
                source.getCustomerReviewRank(),
                source.getBestRank(),
                source.getSubInfo(),
                source.getFullDescription(),
                source.getFullDescription2(),
                source.getToc(),
                source.getBookCommentList()
        );
    }

    /**
     * 알라딘 도서 목록을 공통 도서 목록으로 변환한다.
     * 외부 API가 item 필드를 누락한 경우에도 호출부가 null 검사를 반복하지 않도록 빈 목록을 반환한다.
     */
    public static List<Book> toBooks(List<AladinBook> sources) {
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }

        return sources.stream()
                .map(AladinBookMapper::toBook)
                .toList();
    }
}
