package bkrc.chaptersofu.adapter;

import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.common.shared.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
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
    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException exception, HttpServletRequest request) {
        log.warn(
                exception.getClass().getSimpleName() + " errorCode={} status={} method={} uri={} message={}",
                exception.getErrorCode().getCode(),
                exception.getErrorCode().getStatus().value(),
                request.getMethod(),
                request.getRequestURI(),
                exception.getMessage()
        );
        return getProblemDetail(
                exception.getErrorCode().getStatus(),
                exception.getErrorCode().getCode(),
                exception
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception exception, HttpServletRequest request) {
        log.error(
                "UNHANDLED_EXCEPTION method={} uri={} exceptionType={} message={}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                exception
        );
        return getProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.SERVER_ERROR.getCode(),
                exception
        );
    }

    private static ProblemDetail getProblemDetail(HttpStatus status, String errorCode, Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());

        problemDetail.setProperty("errorCode", errorCode);
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("exception", exception.getClass().getSimpleName());

        return problemDetail;
    }
}
