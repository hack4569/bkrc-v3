package com.bkrc.bkrcv3.member.application.provided;

import com.bkrc.bkrcv3.member.application.request.MemberModifyRequest;
import com.bkrc.bkrcv3.member.application.request.MemberRegisterRequest;
import com.bkrc.bkrcv3.member.application.request.MemberWithdrawRequest;
import com.bkrc.bkrcv3.member.domain.Member;

public interface MemberRegister {
    Member saveMember(MemberRegisterRequest request);
    Member modifyMember(Long memberId, MemberModifyRequest request);
    void withdrawMember(Long memberId, MemberWithdrawRequest request);
}
