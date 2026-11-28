package bkrc.chaptersofu.adapter.client.yes24.mapper;

import bkrc.chaptersofu.adapter.client.yes24.dto.Yes24BookItem;
import bkrc.chaptersofu.domain.aladin.SubInfo;
import bkrc.chaptersofu.domain.book.Book;
import bkrc.chaptersofu.domain.book.BookMall;

import java.util.List;
import java.util.Objects;

/**
 * YES24 상품을 공급자에 독립적인 공통 도서 모델로 변환한다.
 * YES24에만 존재하는 필드는 도메인 모델에 억지로 노출하지 않고 현재 대응 가능한 필드만 옮긴다.
 */
public final class Yes24BookMapper {

    private Yes24BookMapper() {
        // 상태가 없는 정적 매퍼이므로 인스턴스 생성을 막는다.
    }

    public static Book toBook(Yes24BookItem source) {
        Objects.requireNonNull(source, "변환할 YES24 도서는 null일 수 없습니다.");

        Yes24BookItem.ContentDetail content = source.contentDetail();
        String introduction = content == null ? null : content.bookIntroduction();
        String summary = content == null ? null : content.bookSummary();
        String toc = content == null ? null : content.tableOfContents();

        // SubInfo를 항상 생성해 후속 추천 로직에서 null 객체 접근이 발생하지 않도록 한다.
        SubInfo subInfo = new SubInfo(List.of(), List.of(), toc);

        return new Book(
                null,
                source.itemId(),
                BookMall.YES24,
                source.title(),
                source.addOnLink().isEmpty() ? source.link() : source.addOnLink(),
                source.author(),
                source.publishDate(),
                introduction,
                source.isbn10(),
                source.isbn13(),
                source.cover(),
                null,
                null,
                source.publisher(),
                "Y".equalsIgnoreCase(source.adultYn()),
                source.starScore() == null ? null : (int) Math.round(source.starScore()),
                source.sortOrder(),
                subInfo,
                introduction,
                summary,
                toc,
                List.of()
        );
    }

    public static List<Book> toBooks(List<Yes24BookItem> sources) {
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }

        return sources.stream()
                .map(Yes24BookMapper::toBook)
                .toList();
    }
}
