package com.bkrc.bkrcv3.hotbook.application;

import com.bkrc.bkrcv3.aladin.application.response.AladinBookResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "인기 도서 (HotBook)", description = "날짜별 인기 도서 조회 API")
@RestController
@Slf4j
@RequiredArgsConstructor
public class HotBookController {
    private final HotBookService hotBookService;

    @Operation(summary = "날짜별 인기 도서 조회",
            description = "특정 날짜(yyyyMMdd 형식)의 인기 도서 상위 목록을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인기 도서 목록 반환 성공"),
            @ApiResponse(responseCode = "404", description = "BOOK_NOT_FOUND: 조회되지 않는 상품번호입니다.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/v1/hot-books/books/date/{dateStr}")
    public List<AladinBookResponse> readAll(
            @Parameter(description = "조회 날짜 (yyyyMMdd 형식)", required = true, example = "20240101")
            @PathVariable("dateStr") String dateStr
    ) {
        return hotBookService.readAll(dateStr);
    }
}
