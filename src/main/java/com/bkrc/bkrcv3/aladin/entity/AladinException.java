package com.bkrc.bkrcv3.aladin.entity;

import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.exception.BusinessException;

public class AladinException extends BusinessException {
    public AladinException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AladinException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
