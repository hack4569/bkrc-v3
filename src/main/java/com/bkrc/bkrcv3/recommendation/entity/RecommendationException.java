package com.bkrc.bkrcv3.recommendation.entity;

import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.exception.BusinessException;

public class RecommendationException extends BusinessException {
    public RecommendationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public RecommendationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
