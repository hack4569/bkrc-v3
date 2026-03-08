package com.bkrc.bkrcv3.member.application.request;

import jakarta.validation.constraints.NotBlank;

public record MemberRegisterRequest(
        @NotBlank String loginId,
        @NotBlank String password,
        @NotBlank String passwordCheck) {
}
