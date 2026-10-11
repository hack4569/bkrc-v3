package bkrc.chaptersofu.domain.aladin;

import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.common.shared.BusinessException;

public class AladinException extends BusinessException {
    public AladinException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AladinException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
