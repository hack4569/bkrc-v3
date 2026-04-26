package com.bkrc.bkrcv3.like.application;

import com.bkrc.bkrcv3.common.shared.Snowflake;
import com.bkrc.bkrcv3.like.application.response.LikeResponse;
import com.bkrc.bkrcv3.like.entity.Like;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "좋아요 (Like)", description = "도서 좋아요 / 좋아요 취소 API")
@Slf4j
@RestController
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;
    private Snowflake snowflake = new Snowflake();

    @Operation(summary = "도서 좋아요 / 취소",
            description = "도서에 좋아요를 누르거나, 이미 좋아요 상태라면 취소합니다. JWT 인증이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 처리 성공"),
            @ApiResponse(responseCode = "400", description = "LIKE_ALREADY_EXISTS: 이미 좋아요 처리 되었습니다.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/v1/like/{itemId}")
    public LikeResponse likeAction(
            @Parameter(hidden = true) @AuthenticationPrincipal String loginId,
            @Parameter(description = "도서 ID (itemId)", required = true, example = "1") @PathVariable("itemId") int itemId) {

        Like likeResult = likeService.like(Like.create(snowflake.nextId(), itemId, loginId));
        return LikeResponse.from(likeResult);
    }
}
