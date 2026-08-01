package com.bkrc.bkrcv3.recommendation.application;

import com.bkrc.bkrcv3.member.application.response.MyRecommendationResponse;
import com.bkrc.bkrcv3.recommendation.application.request.CreateRecommendationRequest;
import com.bkrc.bkrcv3.recommendation.application.request.UpdateRecommendationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService recommendationService;

    @PostMapping("/v1/aladin/books/recommend/user")
    public MyRecommendationResponse create(@AuthenticationPrincipal Long memberId,
                                           @RequestBody @Valid CreateRecommendationRequest request) {
        return recommendationService.create(memberId, request);
    }

    @GetMapping("/v1/aladin/books/recommend/{itemId}")
    public MyRecommendationResponse get(@AuthenticationPrincipal Long memberId,
                                        @PathVariable Integer itemId) {
        return recommendationService.get(memberId, itemId);
    }

    @PutMapping("/v1/aladin/books/recommend/{itemId}")
    public MyRecommendationResponse update(@AuthenticationPrincipal Long memberId,
                                           @PathVariable Integer itemId,
                                           @RequestBody @Valid UpdateRecommendationRequest request) {
        return recommendationService.update(memberId, itemId, request.recommendation());
    }
}
