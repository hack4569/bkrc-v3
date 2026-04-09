package com.bkrc.bkrcv3.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 5** 에러
 */
@Getter
public class BusinessException extends RuntimeException {
    private HttpStatus status;

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
