package com.bkrc.bkrcv3.like.application.response;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "좋아요 처리 응답")
public record LikeResponse(
        @Schema(description = "도서 ID (itemId)", example = "123456789") Integer itemId
        ) {
    public static LikeResponse from(int itemId) {
        return new LikeResponse(itemId);
    }
}
