package com.bkrc.bkrcv3.like.application.response;

import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.like.entity.Like;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내가 좋아요한 도서 응답")
public record MyLikeResponse(
        @Schema(description = "좋아요 ID", example = "123456789") long likeId,
        @Schema(description = "도서 ID (itemId)", example = "123456789") int itemId,
        @Schema(description = "도서 제목", example = "Clean Code") String title,
        @Schema(description = "도서 커버 이미지 URL") String cover,
        @Schema(description = "저자", example = "로버트 C. 마틴") String author,
        @Schema(description = "출판사", example = "인사이트") String publisher,
        @Schema(description = "도서 상세 페이지 링크") String link
) {
    public static MyLikeResponse of(Like like, AladinBook book) {
        return new MyLikeResponse(
                like.getLikeId(),
                like.getItemId(),
                book.getTitle(),
                book.getCover(),
                book.getAuthor(),
                book.getPublisher(),
                book.getLink()
        );
    }
}
