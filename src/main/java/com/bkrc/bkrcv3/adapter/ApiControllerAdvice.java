package com.bkrc.bkrcv3.adapter;

import com.bkrc.bkrcv3.aladin.entity.AladinClientException;
import com.bkrc.bkrcv3.aladin.entity.AladinRecommendException;
import com.bkrc.bkrcv3.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
public class ApiControllerAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler({AladinRecommendException.class, AladinClientException.class})
    public ProblemDetail handleException(AladinRecommendException e) {
        log.error("[ERROR Class : {}] " + e.getClass() + " ERROR MSG : {}", e.getMessage());
        return getProblemDetail(e.getErrorCode().getStatus(), e);
    }

    /**
     * 4** 에러
     * @param userException
     * @return
     */
    @ExceptionHandler(UserException.class)
    public ProblemDetail handleException(UserException userException) {
        log.error("[4** ERROR] " + userException.getClass() + " ERROR MSG : {}", userException.getMessage());
        return getProblemDetail(userException.getStatus(), userException);
    }

    /**
     * 5** 에러
     * @param businessException
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleException(BusinessException businessException) {
        log.error("[5** ERROR] " + businessException.getClass() + " ERROR MSG : {}", businessException.getMessage(), businessException);
        return getProblemDetail(businessException.getStatus(), businessException);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception exception) {
        return getProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    private static ProblemDetail getProblemDetail(HttpStatus status, Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());

        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("exception", exception.getClass().getSimpleName());

        return problemDetail;
    }
}
