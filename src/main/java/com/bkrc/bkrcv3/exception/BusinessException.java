package com.bkrc.bkrcv3.exception;

import com.bkrc.bkrcv3.common.shared.ErrorCode;
import lombok.Getter;

/**
 * 5** 에러
 */
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
