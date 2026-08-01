package com.bkrc.bkrcv3.recommendation.application;

import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.common.shared.Snowflake;
import com.bkrc.bkrcv3.exception.BusinessException;
import com.bkrc.bkrcv3.member.application.MemberRepository;
import com.bkrc.bkrcv3.member.application.response.MyRecommendationResponse;
import com.bkrc.bkrcv3.recommendation.application.request.CreateRecommendationRequest;
import com.bkrc.bkrcv3.recommendation.entity.BookRecommendation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final RecommendationRepository recommendationRepository;
    private final MemberRepository memberRepository;
    private final Snowflake snowflake;

    @Transactional(readOnly = true)
    public List<MyRecommendationResponse> getMyRecommendations(Long memberId) {
        return recommendationRepository.findByMemberMemberIdOrderByCreatedDesc(memberId).stream()
                .map(MyRecommendationResponse::from).toList();
    }

    @Transactional
    public MyRecommendationResponse create(Long memberId, CreateRecommendationRequest request) {
        var member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        var saved = recommendationRepository.save(BookRecommendation.create(snowflake.nextId(), request.itemId(),
                request.cover(), request.title(), request.link(), request.recommendation(), member));
        return MyRecommendationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public MyRecommendationResponse get(Long memberId, Integer itemId) {
        return recommendationRepository.findByItemIdAndMemberMemberId(itemId, memberId)
                .map(MyRecommendationResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
    }

    @Transactional
    public MyRecommendationResponse update(Long memberId, Integer itemId, String content) {
        var value = recommendationRepository.findByItemIdAndMemberMemberId(itemId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
        if (!"N".equals(value.getApproved())) {
            throw new BusinessException(ErrorCode.RECOMMENDATION_NOT_EDITABLE);
        }
        value.updateRecommendation(content);
        return MyRecommendationResponse.from(value);
    }
}
