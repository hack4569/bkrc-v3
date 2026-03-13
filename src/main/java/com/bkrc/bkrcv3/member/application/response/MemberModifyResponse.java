package com.bkrc.bkrcv3.member.application.response;

import com.bkrc.bkrcv3.member.entity.Member;

public record MemberModifyResponse(String loginId) {
    public static MemberModifyResponse of(Member member) {
        return new MemberModifyResponse(member.getLoginId());
    }
}
