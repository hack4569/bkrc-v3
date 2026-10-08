package com.bkrc.bkrcv3.common.shared;

import com.bkrc.bkrcv3.exception.BusinessException;

public class SnowflakeException extends BusinessException {
    public SnowflakeException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SnowflakeException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
