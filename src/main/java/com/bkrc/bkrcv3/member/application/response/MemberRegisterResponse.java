package com.bkrc.bkrcv3.member.application.response;

import com.bkrc.bkrcv3.member.entity.Member;

public record MemberRegisterResponse(String loginId) {
    public static MemberRegisterResponse of(Member member) {
        return new MemberRegisterResponse(member.getLoginId());
    }
}
