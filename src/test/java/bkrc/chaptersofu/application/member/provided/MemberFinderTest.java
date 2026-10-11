package bkrc.chaptersofu.application.member.provided;

import bkrc.chaptersofu.adapter.webapi.CommonApiTest;
import bkrc.chaptersofu.application.member.provided.MemberFinder;
import bkrc.chaptersofu.application.member.provided.MemberRegister;
import bkrc.chaptersofu.application.member.provided.MemberRegisterRequest;
import bkrc.chaptersofu.domain.member.Member;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
@CommonApiTest
@Transactional
class MemberFinderTest {
    @Autowired
    private MemberFinder memberFinder;
    @Autowired
    private MemberRegister memberRegister;
    @Autowired
    private EntityManager entityManager;

    @Test
    void 회원_ID로_회원을_조회한다() {
        // Arrange
        Member member = memberRegister.saveMember(new MemberRegisterRequest("testuser", "password", "password"));
        entityManager.flush();
        entityManager.clear();

        Member found = memberFinder.getMemberById(member.getMemberId());
        assertEquals(member.getMemberId(), found.getMemberId());
    }

    @Test
    void 존재하지_않는_회원_ID_조회는_실패한다() {
        assertThatThrownBy(() -> memberFinder.getMemberById(9999L))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    void 로그인_ID로_회원을_조회한다() {
        // Arrange
        Member member = memberRegister.saveMember(new MemberRegisterRequest("testuser2", "password", "password"));
        entityManager.flush();
        entityManager.clear();

        Member found = memberFinder.getMemberByLoginId(member.getLoginId());
        assertEquals(member.getLoginId(), found.getLoginId());
    }

    @Test
    void 존재하지_않는_로그인_ID_조회는_실패한다() {
        assertThatThrownBy(() -> memberFinder.getMemberByLoginId("testuser"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}