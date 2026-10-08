package com.bkrc.bkrcv3.adapter.webapi;

import com.bkrc.bkrcv3.adapter.webapi.dto.LikeResponse;
import com.bkrc.bkrcv3.like.application.provided.LikeRegister;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "좋아요 (Like)", description = "도서 좋아요 / 좋아요 취소 API")
@RestController
@RequiredArgsConstructor
public class LikeApi {
    private final LikeRegister likeRegister;

    @Operation(summary = "도서 좋아요", description = "도서에 좋아요를 등록합니다. JWT 인증이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 처리 성공"),
            @ApiResponse(responseCode = "400", description = "LIKE_ALREADY_EXISTS: 이미 좋아요 처리 되었습니다.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/v1/like/{itemId}")
    public LikeResponse like(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Parameter(description = "도서 ID (itemId)", required = true, example = "1")
            @PathVariable Integer itemId) {
        return LikeResponse.of(likeRegister.like(itemId, memberId));
    }

    @Operation(summary = "도서 좋아요 취소", description = "등록된 좋아요를 취소합니다. JWT 인증이 필요합니다.")
    @PostMapping("/v1/like/cancel/{itemId}")
    public void unLike(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Parameter(description = "도서 ID (itemId)", required = true, example = "1")
            @PathVariable Integer itemId) {
        likeRegister.unLike(itemId, memberId);
    }
}
