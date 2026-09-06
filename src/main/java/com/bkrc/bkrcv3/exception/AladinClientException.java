package com.bkrc.bkrcv3.exception;

import com.bkrc.bkrcv3.common.shared.ErrorCode;

public class AladinClientException extends BusinessException {

    public AladinClientException(Throwable cause) {
        super(ErrorCode.ALADIN_CLIENT_ERROR, cause);
    }
}
