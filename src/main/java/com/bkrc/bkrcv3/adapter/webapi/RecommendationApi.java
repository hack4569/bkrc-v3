package com.bkrc.bkrcv3.adapter.webapi;

import com.bkrc.bkrcv3.adapter.webapi.dto.MyRecommendationResponse;
import com.bkrc.bkrcv3.recommendation.application.provided.RecommendationFinder;
import com.bkrc.bkrcv3.recommendation.application.provided.RecommendationRegister;
import com.bkrc.bkrcv3.recommendation.application.request.CreateRecommendationRequest;
import com.bkrc.bkrcv3.recommendation.application.request.UpdateRecommendationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RecommendationApi {
    private final RecommendationFinder recommendationFinder;
    private final RecommendationRegister recommendationRegister;

    @PostMapping("/v1/aladin/books/recommend/user")
    public MyRecommendationResponse create(@AuthenticationPrincipal Long memberId,
                                           @RequestBody @Valid CreateRecommendationRequest request) {
        return MyRecommendationResponse.from(recommendationRegister.create(memberId, request));
    }

    @GetMapping("/v1/aladin/books/recommend/{itemId}")
    public MyRecommendationResponse get(@AuthenticationPrincipal Long memberId,
                                        @PathVariable Integer itemId) {
        return MyRecommendationResponse.from(recommendationFinder.get(memberId, itemId));
    }

    @PutMapping("/v1/aladin/books/recommend/{itemId}")
    public MyRecommendationResponse update(@AuthenticationPrincipal Long memberId,
                                           @PathVariable Integer itemId,
                                           @RequestBody @Valid UpdateRecommendationRequest request) {
        return MyRecommendationResponse.from(
                recommendationRegister.update(memberId, itemId, request.recommendation()));
    }
}
