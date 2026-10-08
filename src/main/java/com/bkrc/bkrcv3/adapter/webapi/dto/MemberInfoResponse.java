package com.bkrc.bkrcv3.adapter.webapi.dto;

import com.bkrc.bkrcv3.member.application.response.MyRecommendationResponse;

import java.util.List;

public record MemberInfoResponse(
        String loginId,
        List<MyLikeResponse> likedBooks,
        List<MyRecommendationResponse> recommendedBooks
) {
    public static MemberInfoResponse of(String loginId, List<MyLikeResponse> likedBooks,
                                        List<MyRecommendationResponse> recommendedBooks) {
        return new MemberInfoResponse(loginId, likedBooks, recommendedBooks);
    }
}
