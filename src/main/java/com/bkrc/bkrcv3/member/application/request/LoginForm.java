package com.bkrc.bkrcv3.member.application.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;


@Data
public class LoginForm {
    @NotEmpty
    private String loginId;

    @NotEmpty
    private String password;

    private boolean autoLogin;
}
