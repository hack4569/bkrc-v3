package bkrc.chaptersofu.adapter.client.yes24.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * YES24 상품 응답 중 공통 Book으로 변환할 필드를 담는다.
 * 알라딘 모델과 섞이지 않도록 YES24 어댑터 내부 DTO로 한정한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Yes24BookItem(
        Integer sortOrder,
        Integer itemId,
        String title,
        String author,
        String goodsType,
        String adultYn,
        String publisher,
        String isbn10,
        String isbn13,
        String publishDate,
        Double starScore,
        String cover,
        String link,
        ContentDetail contentDetail
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentDetail(
            String bookIntroduction,
            String bookSummary,
            String tableOfContents
    ) {
    }
}
