package com.bkrc.bkrcv3.member.application.response;

import com.bkrc.bkrcv3.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 정보 수정 응답")
public record MemberModifyResponse(
        @Schema(description = "수정된 로그인 ID", example = "user123") String loginId) {
    public static MemberModifyResponse of(Member member) {
        return new MemberModifyResponse(member.getLoginId());
    }
}
