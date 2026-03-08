package com.bkrc.bkrcv3.aladin.entity;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AladinException extends RuntimeException{
    private static final String DEFAULT_MESSAGE = "[알라딘 API] 연동 오류 발생";
    private final String errorMessage;
    private final HttpStatus httpStatus;

    // 기본 생성자
    public AladinException() {
        this(DEFAULT_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);  // 기본 httpStatus 설정
    }

    // 메시지만 받아서 생성
    public AladinException(String message) {
        this(DEFAULT_MESSAGE + " (" + message + ")", HttpStatus.INTERNAL_SERVER_ERROR); // 기본 httpStatus 설정
    }

    // 메시지와 원인 받아서 생성
    public AladinException(String message, Throwable cause) {
        super(DEFAULT_MESSAGE + " (" + message + ")", cause);
        this.errorMessage = DEFAULT_MESSAGE + " (" + message + ") ";
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR; // 기본 httpStatus 설정
    }

    // 메시지와 HttpStatus 받아서 생성
    public AladinException(String message, HttpStatus httpStatus) {
        super(message);
        this.errorMessage = DEFAULT_MESSAGE + " (" + message + ") ";
        this.httpStatus = httpStatus; // 받은 httpStatus 설정
    }
}
