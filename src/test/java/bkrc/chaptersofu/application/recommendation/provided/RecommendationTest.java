package bkrc.chaptersofu.application.recommendation.provided;

import bkrc.chaptersofu.application.recommendation.provided.*;

import bkrc.chaptersofu.adapter.webapi.CommonApiTest;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.application.member.provided.MemberRegister;
import bkrc.chaptersofu.application.member.provided.MemberRegisterRequest;
import bkrc.chaptersofu.domain.member.Member;
import bkrc.chaptersofu.application.recommendation.provided.CreateRecommendationRequest;
import bkrc.chaptersofu.domain.recommendation.RecommendationException;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@CommonApiTest
@Transactional
class RecommendationTest {
    @Autowired RecommendationFinder recommendationFinder;
    @Autowired RecommendationRegister recommendationRegister;
    @Autowired MemberRegister memberRegister;
    @Autowired EntityManager entityManager;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRegister.saveMember(
                new MemberRegisterRequest("recommendationUser", "password", "password"));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void 추천을_생성하고_조회한다() {
        var saved = recommendationRegister.create(member.getMemberId(), request(3001));
        entityManager.flush();
        entityManager.clear();

        var found = recommendationFinder.get(member.getMemberId(), 3001);

        assertThat(found.getRecommendationId()).isEqualTo(saved.getRecommendationId());
        assertThat(recommendationFinder.getMyRecommendations(member.getMemberId())).hasSize(1);
    }

    @Test
    void 수정할_수_없는_추천_수정은_실패한다() {
        recommendationRegister.create(member.getMemberId(), request(3001));

        RecommendationException exception = catchThrowableOfType(
                RecommendationException.class,
                () -> recommendationRegister.update(member.getMemberId(), 3001, "수정 추천 내용"));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.RECOMMENDATION_NOT_EDITABLE);
    }

    @Test
    void 존재하지_않는_추천_조회는_실패한다() {
        RecommendationException exception = catchThrowableOfType(
                RecommendationException.class,
                () -> recommendationFinder.get(member.getMemberId(), 9999));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BOOK_NOT_FOUND);
    }

    private CreateRecommendationRequest request(Integer itemId) {
        return new CreateRecommendationRequest(itemId, "cover", "title", "https://example.com/" + itemId,
                "추천 내용");
    }
}
