package com.bkrc.bkrcv3.recommendation.application.provided;

import com.bkrc.bkrcv3.recommendation.domain.BookRecommendation;

import java.util.List;

public interface RecommendationFinder {
    List<BookRecommendation> getMyRecommendations(Long memberId);
    BookRecommendation get(Long memberId, Integer itemId);
}
