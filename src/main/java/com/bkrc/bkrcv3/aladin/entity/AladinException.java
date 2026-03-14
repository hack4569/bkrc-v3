package com.bkrc.bkrcv3.aladin.entity;

import lombok.Getter;

/**
 * 알라딘 도메인 예외. HTTP 매핑은 adapter(ControllerAdvice)에서 수행한다.
 */
@Getter
public class AladinException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "[알라딘 API] 연동 오류 발생";
    private final String errorMessage;

    public AladinException() {
        this(DEFAULT_MESSAGE);
    }

    public AladinException(String message) {
        super(DEFAULT_MESSAGE + " (" + message + ")");
        this.errorMessage = DEFAULT_MESSAGE + " (" + message + ")";
    }

    public AladinException(String message, Throwable cause) {
        super(DEFAULT_MESSAGE + " (" + message + ")", cause);
        this.errorMessage = DEFAULT_MESSAGE + " (" + message + ")";
    }
}
