package com.bkrc.bkrcv3.aladin.application;

import com.bkrc.bkrcv3.aladin.application.request.AladinRecommendForUserRequest;
import com.bkrc.bkrcv3.aladin.application.request.AladinRecommendSaveRequest;
import com.bkrc.bkrcv3.aladin.application.request.AladinRequest;
import com.bkrc.bkrcv3.aladin.application.response.AladinBookPageResponse;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.history.entity.History;
import com.bkrc.bkrcv3.member.application.response.RecommendView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "도서 (Aladin)", description = "알라딘 도서 목록 및 사용자 맞춤 추천 API")
@RestController
@Slf4j
@RequiredArgsConstructor
public class AladinController {
    private final AladinService aladinService;

    @Operation(summary = "사용자 맞춤 도서 추천 조회",
            description = "로그인된 사용자의 열람 이력과 카테고리 설정을 기반으로 추천 도서 목록을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "추천 도서 목록 반환 성공")
    @GetMapping("/v1/aladin/books/recommend/user")
    public List<RecommendView> getRecommendbooksForUser(
            @Parameter(hidden = true) @AuthenticationPrincipal String loginId,
            AladinRecommendForUserRequest request) {
        List<RecommendView> recommendViewList = aladinService.getRecommendBooksForUser(loginId, request);
        return recommendViewList;
    }

    @Operation(summary = "전체 도서 목록 조회", description = "저장된 모든 도서 목록을 페이지 정보와 함께 반환합니다.")
    @ApiResponse(responseCode = "200", description = "도서 목록 반환 성공")
    @GetMapping("/v1/aladin/books")
    public AladinBookPageResponse getAllBooks() {
        return aladinService.findAll();
    }
}
