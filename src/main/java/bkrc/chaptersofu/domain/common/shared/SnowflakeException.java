package bkrc.chaptersofu.domain.common.shared;

import bkrc.chaptersofu.domain.common.shared.BusinessException;

public class SnowflakeException extends BusinessException {
    public SnowflakeException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SnowflakeException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
