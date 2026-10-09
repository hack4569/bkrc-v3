package com.bkrc.bkrcv3.recommendation.application.provided;

import com.bkrc.bkrcv3.recommendation.application.request.CreateRecommendationRequest;
import com.bkrc.bkrcv3.recommendation.domain.BookRecommendation;

public interface RecommendationRegister {
    BookRecommendation create(Long memberId, CreateRecommendationRequest request);
    BookRecommendation update(Long memberId, Integer itemId, String content);
}
