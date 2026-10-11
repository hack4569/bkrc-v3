package bkrc.chaptersofu.application.member.provided;

import bkrc.chaptersofu.domain.member.Member;

public interface MemberRegister {
    Member saveMember(MemberRegisterRequest request);
    Member modifyMember(Long memberId, MemberModifyRequest request);
    void withdrawMember(Long memberId, MemberWithdrawRequest request);
}
