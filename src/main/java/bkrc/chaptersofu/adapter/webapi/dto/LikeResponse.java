package bkrc.chaptersofu.adapter.webapi.dto;

import bkrc.chaptersofu.domain.like.Like;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좋아요 처리 응답")
public record LikeResponse(
        @Schema(description = "도서 ID (itemId)", example = "123456789") Integer itemId
) {
    public static LikeResponse of(Like like) {
        return new LikeResponse(like.getBook().getItemId());
    }
}
