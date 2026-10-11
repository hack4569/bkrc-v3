package bkrc.chaptersofu.domain.exception;

import bkrc.chaptersofu.domain.common.shared.ErrorCode;

public class AladinClientException extends BusinessException {

    public AladinClientException(Throwable cause) {
        super(ErrorCode.ALADIN_CLIENT_ERROR, cause);
    }
}
