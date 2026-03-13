package com.bkrc.bkrcv3.member.application.request;

import jakarta.validation.constraints.NotBlank;

public record MemberModifyRequest(
        @NotBlank String loginId,
        @NotBlank String originPassword,
        @NotBlank String newPassword,
        @NotBlank String newPasswordCheck) {
}
