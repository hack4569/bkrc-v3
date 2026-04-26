package com.bkrc.bkrcv3.member.application.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;


@Schema(description = "로그인 요청")
@Data
public class LoginForm {
    @Schema(description = "로그인 ID", example = "user123")
    @NotEmpty
    private String loginId;

    @Schema(description = "비밀번호", example = "pass1234!")
    @NotEmpty
    private String password;

    @Schema(description = "자동 로그인 여부", example = "false")
    private boolean autoLogin;
}
