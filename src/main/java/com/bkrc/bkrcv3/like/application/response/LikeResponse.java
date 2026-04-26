package com.bkrc.bkrcv3.like.application.response;

import com.bkrc.bkrcv3.like.entity.Like;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "좋아요 처리 응답")
public record LikeResponse(
        @Schema(description = "도서 ID (itemId)", example = "123456789") Integer itemId,
        @Schema(description = "로그인 ID", example = "user123") String loginId) {
    public static LikeResponse from(Like like) {
        return new LikeResponse(like.getItemId(), like.getLoginId());
    }
}
