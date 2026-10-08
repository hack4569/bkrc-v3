package com.bkrc.bkrcv3.like.entity;

import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.exception.BusinessException;

public class LikeException extends BusinessException {
    public LikeException(ErrorCode errorCode) {
        super(errorCode);
    }

    public LikeException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
