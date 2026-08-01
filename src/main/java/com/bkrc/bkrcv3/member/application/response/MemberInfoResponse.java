package com.bkrc.bkrcv3.member.application.response;

import com.bkrc.bkrcv3.like.application.response.MyLikeResponse;

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
