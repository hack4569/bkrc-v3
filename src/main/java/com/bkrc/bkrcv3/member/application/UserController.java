package com.bkrc.bkrcv3.member.application;

import com.bkrc.bkrcv3.member.application.request.MemberModifyRequest;
import com.bkrc.bkrcv3.member.application.request.MemberRegisterRequest;
import com.bkrc.bkrcv3.member.application.response.MemberModifyResponse;
import com.bkrc.bkrcv3.member.application.response.MemberRegisterResponse;
import com.bkrc.bkrcv3.member.entity.PasswordEncoder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 (Member)", description = "회원 가입 및 정보 수정 API")
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserServiceImpl userService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "회원 가입", description = "새로운 회원을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 가입 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 검사 실패")
    })
    @PostMapping("/v1/member")
    public MemberRegisterResponse register(@RequestBody @Valid MemberRegisterRequest request) {
        var member = userService.saveMember(request);
        var registeredMember = MemberRegisterResponse.of(member);
        return registeredMember;
    }

    @Operation(summary = "회원 정보 수정", description = "비밀번호를 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 검사 실패")
    })
    @PutMapping("/v1/member/{loginId}")
    public MemberModifyResponse update(
            @Parameter(description = "로그인 ID", required = true, example = "user123") @PathVariable String loginId,
            @RequestBody @Valid MemberModifyRequest request) {
        var response = userService.modifyMember(loginId, request);
        return MemberModifyResponse.of(response);
    }
}
