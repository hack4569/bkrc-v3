package bkrc.chaptersofu.adapter.client.yes24.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * YES24 목록 API 응답에서 현재 기능에 필요한 데이터만 표현한다.
 * 문서에 필드가 추가되더라도 역직렬화가 깨지지 않도록 알 수 없는 필드는 무시한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Yes24Response(
        Boolean success,
        String message,
        String errorCode,
        Data data
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(
            List<Yes24BookItem> items,
            Integer currentPage,
            Integer pageSize,
            Integer totalCount
    ) {
    }
}
