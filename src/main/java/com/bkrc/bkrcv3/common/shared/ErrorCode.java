package com.bkrc.bkrcv3.common.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    ALADIN_NOT_READY(HttpStatus.SERVICE_UNAVAILABLE, "준비중입니다."),
    ALADIN_CLIENT_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "알라딘 API 에러"),

    USER_NOT_EQUALS_PW(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
