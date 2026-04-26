package com.bkrc.bkrcv3.member.application;

import com.bkrc.bkrcv3.member.application.request.LoginForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증 (Auth)", description = "로그인 / JWT 발급 API")
@RestController
public class LoginController {

    @Operation(
            summary = "로그인",
            description = "아이디와 비밀번호로 로그인합니다. 성공 시 응답 헤더에 JWT 토큰이 발급됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공 - 응답 헤더에 JWT 토큰 포함",
                    headers = {
                            @Header(name = "token", description = "JWT 인증 토큰", schema = @Schema(type = "string")),
                            @Header(name = "userId", description = "로그인 ID", schema = @Schema(type = "string"))
                    }
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "AUTHENTICATION_FAILED: 인증에 실패했습니다.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping("/login")
    public void login(@RequestBody LoginForm loginForm) {
        // Spring Security AuthenticationFilter가 처리
    }
}
