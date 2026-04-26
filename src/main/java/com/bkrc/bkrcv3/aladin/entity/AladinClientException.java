package com.bkrc.bkrcv3.aladin.entity;

import com.bkrc.bkrcv3.common.shared.ErrorCode;
import lombok.Getter;

@Getter
public class AladinClientException extends RuntimeException {
    private final ErrorCode errorCode;

    public AladinClientException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
