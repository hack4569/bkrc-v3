package bkrc.chaptersofu.adapter.client.yes24;

/**
 * YES24 API 호출 또는 응답 처리 과정의 실패를 나타낸다.
 * 공급자별 장애를 구분할 수 있도록 어댑터 전용 예외로 정의한다.
 */
public class Yes24ClientException extends RuntimeException {

    public Yes24ClientException(String message) {
        super(message);
    }

    public Yes24ClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
