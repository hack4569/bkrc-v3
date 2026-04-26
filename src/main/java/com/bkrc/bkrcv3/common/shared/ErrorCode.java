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
    ALADIN_NOT_FOUND(HttpStatus.NOT_FOUND, "책 정보를 찾을 수 없습니다."),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "조회되지 않는 상품번호입니다."),

    USER_NOT_EQUALS_PW(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 등록된 사용자 입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 아이디를 찾을 수 없습니다."),

    LIKE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 좋아요 처리 되었습니다."),

    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),
    INVALID_TIME(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid Time"),
    EVENT_HANDLER_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "이벤트 핸들러를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
