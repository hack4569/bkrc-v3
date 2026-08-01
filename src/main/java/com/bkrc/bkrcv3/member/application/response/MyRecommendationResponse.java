package com.bkrc.bkrcv3.member.application.response;

import com.bkrc.bkrcv3.recommendation.entity.BookRecommendation;
import io.swagger.v3.oas.annotations.media.Schema;

public record MyRecommendationResponse(
        @Schema(description = "도서 ID") Integer itemId,
        @Schema(description = "도서 커버 이미지 URL") String cover,
        @Schema(description = "도서 제목") String title,
        @Schema(description = "도서 상세 페이지 링크") String link,
        @Schema(description = "추천 승인 상태 (Y: 승인, N: 미승인, W: 승인대기)",
                allowableValues = {"Y", "N", "W"}) String approved,
        @Schema(description = "추천 내용") String recommendation
) {
    public static MyRecommendationResponse from(BookRecommendation value) {
        return new MyRecommendationResponse(value.getItemId(), value.getCover(), value.getTitle(),
                value.getLink(), value.getApproved(), value.getRecommendation());
    }
}
