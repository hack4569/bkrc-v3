package com.bkrc.bkrcv3.member.application.response;

import com.bkrc.bkrcv3.member.entity.Member;

public record LoginResponse(String token, String loginId) {
    public static LoginResponse of(String token, Member member) {
        return new LoginResponse(token, member.getLoginId());
    }
}
