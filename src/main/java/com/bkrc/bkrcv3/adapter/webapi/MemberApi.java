package com.bkrc.bkrcv3.adapter.webapi;

import com.bkrc.bkrcv3.member.application.provided.MemberFinder;
import com.bkrc.bkrcv3.member.application.provided.MemberRegister;
import com.bkrc.bkrcv3.like.application.provided.LikeFinder;
import com.bkrc.bkrcv3.member.application.request.MemberModifyRequest;
import com.bkrc.bkrcv3.member.application.request.MemberRegisterRequest;
import com.bkrc.bkrcv3.member.application.request.MemberWithdrawRequest;
import com.bkrc.bkrcv3.adapter.webapi.dto.MemberInfoResponse;
import com.bkrc.bkrcv3.adapter.webapi.dto.MemberModifyResponse;
import com.bkrc.bkrcv3.adapter.webapi.dto.MemberRegisterResponse;
import com.bkrc.bkrcv3.adapter.webapi.dto.MyLikeResponse;
import com.bkrc.bkrcv3.adapter.webapi.dto.MyRecommendationResponse;
import com.bkrc.bkrcv3.recommendation.application.provided.RecommendationFinder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 (Member)", description = "회원 가입 및 정보 수정 API")
@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberApi {
    private final MemberRegister memberRegister;
    private final MemberFinder memberFinder;
    private final LikeFinder likeFinder;
    private final RecommendationFinder recommendationFinder;

    @Operation(summary = "회원 가입", description = "새로운 회원을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 가입 성공"),
            @ApiResponse(responseCode = "400", description = """
                    USER_ALREADY_EXISTS: 이미 등록된 사용자 입니다.
                    USER_NOT_EQUALS_PW: 비밀번호가 일치하지 않습니다.""",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/v1/member")
    public MemberRegisterResponse register(@RequestBody @Valid MemberRegisterRequest request) {
        var member = memberRegister.saveMember(request);
        var registeredMember = MemberRegisterResponse.of(member);
        return registeredMember;
    }

    @Operation(summary = "회원 정보 수정", description = "비밀번호를 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "USER_NOT_EQUALS_PW: 비밀번호가 일치하지 않습니다.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "USER_NOT_FOUND: 해당 아이디를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/v1/member/me")
    public MemberModifyResponse update(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid MemberModifyRequest request) {
        var response = memberRegister.modifyMember(memberId, request);
        return MemberModifyResponse.of(response);
    }

    @Operation(summary = "회원 정보 조회", description = "로그인 ID로 회원 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "USER_NOT_FOUND: 해당 아이디를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/v1/member/me")
    public MemberInfoResponse getMemberInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId) {
        var member = memberFinder.getMemberById(memberId);
        return MemberInfoResponse.of(
                member.getLoginId(),
                likeFinder.getMyLikes(memberId).stream().map(MyLikeResponse::of).toList(),
                recommendationFinder.getMyRecommendations(member.getMemberId()).stream()
                        .map(MyRecommendationResponse::from).toList()
        );
    }

    @Operation(summary = "회원 탈퇴", description = "현재 비밀번호를 확인 후 회원을 탈퇴합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "400", description = "USER_NOT_EQUALS_PW: 비밀번호가 일치하지 않습니다.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "USER_NOT_FOUND: 해당 아이디를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/v1/member/me")
    public void withdraw(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid MemberWithdrawRequest request) {
        memberRegister.withdrawMember(memberId, request);
    }
}
