package com.bkrc.bkrcv3.history.domain;

import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.exception.BusinessException;

public class HistoryException extends BusinessException {
    public HistoryException(ErrorCode errorCode) {
        super(errorCode);
    }

    public HistoryException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
