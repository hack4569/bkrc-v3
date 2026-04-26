package com.bkrc.bkrcv3.aladin.application.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Schema(description = "도서 목록 페이지 응답")
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AladinBookPageResponse {

    @Schema(description = "도서 목록")
    private List<AladinBookResponse> aladinBookResponseList;

    @Schema(description = "전체 도서 수", example = "100")
    private int count;

    public static AladinBookPageResponse of(final List<AladinBookResponse> aladinBookResponseList, final int count) {
        AladinBookPageResponse aladinBookPageResponse = new AladinBookPageResponse();
        aladinBookPageResponse.aladinBookResponseList = aladinBookResponseList;
        aladinBookPageResponse.count = count;
        return aladinBookPageResponse;
    }
}
