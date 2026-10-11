package bkrc.chaptersofu.adapter.tests.recommendation.application.provided;

import bkrc.chaptersofu.application.recommendation.provided.*;

import bkrc.chaptersofu.adapter.tests.api.CommonApiTest;
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
    void createAndFindRecommendation() {
        var saved = recommendationRegister.create(member.getMemberId(), request(3001));
        entityManager.flush();
        entityManager.clear();

        var found = recommendationFinder.get(member.getMemberId(), 3001);

        assertThat(found.getRecommendationId()).isEqualTo(saved.getRecommendationId());
        assertThat(recommendationFinder.getMyRecommendations(member.getMemberId())).hasSize(1);
    }

    @Test
    void failUpdateWhenRecommendationIsNotEditable() {
        recommendationRegister.create(member.getMemberId(), request(3001));

        RecommendationException exception = catchThrowableOfType(
                RecommendationException.class,
                () -> recommendationRegister.update(member.getMemberId(), 3001, "수정 추천 내용"));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.RECOMMENDATION_NOT_EDITABLE);
    }

    @Test
    void failFindWhenRecommendationDoesNotExist() {
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
