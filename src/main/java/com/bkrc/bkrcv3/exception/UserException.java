package com.bkrc.bkrcv3.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 4** 에러
 */
@Getter
public class UserException extends RuntimeException {
    private HttpStatus status;

    public UserException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
