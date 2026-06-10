package com.bkrc.bkrcv3.member.application.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원 탈퇴 요청")
public record MemberWithdrawRequest(
        @Schema(description = "현재 비밀번호", example = "myPass1!") @NotBlank String password) {
}