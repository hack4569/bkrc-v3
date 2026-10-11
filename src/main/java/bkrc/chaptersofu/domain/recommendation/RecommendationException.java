package bkrc.chaptersofu.domain.recommendation;

import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.exception.BusinessException;

public class RecommendationException extends BusinessException {
    public RecommendationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public RecommendationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
