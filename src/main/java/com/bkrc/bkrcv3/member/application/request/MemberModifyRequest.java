package com.bkrc.bkrcv3.member.application.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원 정보 수정 요청")
public record MemberModifyRequest(
        @Schema(description = "로그인 ID", example = "user123") @NotBlank String loginId,
        @Schema(description = "현재 비밀번호", example = "oldPass1!") @NotBlank String originPassword,
        @Schema(description = "새 비밀번호", example = "newPass2@") @NotBlank String newPassword,
        @Schema(description = "새 비밀번호 확인", example = "newPass2@") @NotBlank String newPasswordCheck) {
}
