package bkrc.chaptersofu.domain.aladin;

import bkrc.chaptersofu.domain.common.shared.BusinessException;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;

public class AladinClientException extends BusinessException {

    public AladinClientException(Throwable cause) {
        super(ErrorCode.ALADIN_CLIENT_ERROR, cause);
    }
}
