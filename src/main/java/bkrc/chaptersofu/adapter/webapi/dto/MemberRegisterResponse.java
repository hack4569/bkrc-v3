package bkrc.chaptersofu.adapter.webapi.dto;

import bkrc.chaptersofu.domain.member.Member;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 가입 응답")
public record MemberRegisterResponse(
        @Schema(description = "등록된 로그인 ID", example = "user123") String loginId) {
    public static MemberRegisterResponse of(Member member) {
        return new MemberRegisterResponse(member.getLoginId());
    }
}
