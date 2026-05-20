package com.bkrc.bkrcv3.member.application.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원 가입 요청")
public record MemberRegisterRequest(
        @Schema(description = "로그인 ID", example = "user123") @NotBlank String loginId,
        @Schema(description = "비밀번호", example = "pass1234!")
        @NotBlank
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다")
        String password,
        @Schema(description = "비밀번호 확인", example = "pass1234!") @NotBlank String passwordCheck) {
}
