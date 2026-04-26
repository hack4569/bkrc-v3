package com.bkrc.bkrcv3.aladin.entity;

import com.bkrc.bkrcv3.common.shared.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AladinRecommendException extends RuntimeException {
    private final ErrorCode errorCode;

    public AladinRecommendException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
