package bkrc.chaptersofu.application.member.provided;

import bkrc.chaptersofu.application.member.provided.*;

import bkrc.chaptersofu.adapter.webapi.CommonApiTest;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.application.member.provided.MemberModifyRequest;
import bkrc.chaptersofu.application.member.provided.MemberRegisterRequest;
import bkrc.chaptersofu.application.member.provided.MemberWithdrawRequest;
import bkrc.chaptersofu.domain.member.DuplicateMemberException;
import bkrc.chaptersofu.domain.member.Member;
import bkrc.chaptersofu.domain.member.PasswordEncoder;
import bkrc.chaptersofu.domain.member.PasswordNotEquals;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.*;

@CommonApiTest
@Transactional
class MemberRegisterTest {
    @Autowired
    private MemberFinder memberFinder;
    @Autowired
    private MemberRegister memberRegister;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void 회원을_등록한다() {
        Member newmMember = memberRegister.saveMember(new MemberRegisterRequest("testuser", "password", "password"));
        assertNotNull(newmMember);
    }

    @Test
    void 로그인_ID가_중복되면_회원_등록에_실패한다() {
        memberRegister.saveMember(new MemberRegisterRequest("duplicateUser", "password", "password"));
        entityManager.flush();
        entityManager.clear();

        DuplicateMemberException exception = catchThrowableOfType(
                DuplicateMemberException.class,
                () -> memberRegister.saveMember(
                        new MemberRegisterRequest("duplicateUser", "password", "password"))
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_ALREADY_EXISTS);
    }

    @Test
    void 비밀번호가_일치하지_않으면_회원_등록에_실패한다() {
        PasswordNotEquals exception = catchThrowableOfType(
                PasswordNotEquals.class,
                () -> memberRegister.saveMember(
                        new MemberRegisterRequest("passwordMismatchUser", "password", "differentPassword"))
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_EQUALS_PW);
    }

    @Test
    void 회원_정보를_수정한다() {
        Member member = memberRegister.saveMember(new MemberRegisterRequest("testuser", "password", "password"));
        entityManager.flush();
        entityManager.clear();

        var found = memberFinder.getMemberById(member.getMemberId());
        var modifiedMember = memberRegister.modifyMember(found.getMemberId(), new MemberModifyRequest("testuser", "password", "newpassword", "newpassword"));
        assertTrue(passwordEncoder.matches("newpassword", modifiedMember.getPassword()));
    }

    @Test
    void 회원을_탈퇴한다() {
        Member member = memberRegister.saveMember(new MemberRegisterRequest("testuser", "password", "password"));
        entityManager.flush();
        entityManager.clear();

        var found = memberFinder.getMemberById(member.getMemberId());
        memberRegister.withdrawMember(found.getMemberId(), new MemberWithdrawRequest("password"));
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> memberFinder.getMemberById(member.getMemberId()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
