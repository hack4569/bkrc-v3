package com.bkrc.bkrcv3.member.application.provided;

import com.bkrc.bkrcv3.api.CommonApiTest;
import com.bkrc.bkrcv3.member.application.provided.MemberFinder;
import com.bkrc.bkrcv3.member.application.provided.MemberRegister;
import com.bkrc.bkrcv3.member.application.request.MemberRegisterRequest;
import com.bkrc.bkrcv3.member.domain.Member;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;


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
    void findMemberById() {
        // Arrange
        Member member = memberRegister.saveMember(new MemberRegisterRequest("testuser", "password", "password"));
        entityManager.flush();
        entityManager.clear();

        Member found = memberFinder.getMemberById(member.getMemberId());
        assertEquals(member.getMemberId(), found.getMemberId());
    }

    @Test
    void failFindMemberById() {
        assertThatThrownBy(() -> memberFinder.getMemberById(9999L))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    void findMemberByLoginId() {
        // Arrange
        Member member = memberRegister.saveMember(new MemberRegisterRequest("testuser2", "password", "password"));
        entityManager.flush();
        entityManager.clear();

        Member found = memberFinder.getMemberByLoginId(member.getLoginId());
        assertEquals(member.getLoginId(), found.getLoginId());
    }

    @Test
    void failFindMemberByLoginId() {
        assertThatThrownBy(() -> memberFinder.getMemberByLoginId("testuser"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}