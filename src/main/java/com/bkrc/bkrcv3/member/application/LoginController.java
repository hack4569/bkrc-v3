package com.bkrc.bkrcv3.member.application;

import com.bkrc.bkrcv3.member.application.request.LoginForm;
import com.bkrc.bkrcv3.member.application.response.LoginResponse;
import com.bkrc.bkrcv3.member.entity.Member;
import com.bkrc.bkrcv3.member.entity.MemberException;
import com.bkrc.bkrcv3.member.entity.PasswordEncoder;
import com.bkrc.bkrcv3.member.security.JwtProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @PostMapping("/v1/login")
    public LoginResponse login(@RequestBody @Valid LoginForm request) {

        // 회원 조회
        Member member = memberRepository.findMemberByLoginId(request.getLoginId())
                .orElseThrow(() -> new MemberException("아이디 또는 비밀번호가 올바르지 않습니다."));

        // 비밀번호 검증
        if (!passwordEncoder.checkPassword(request.getPassword(), member.getPassword())) {
            throw new MemberException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 토큰 발급
        String token = jwtProvider.generateToken(member.getLoginId());
        log.info("[로그인] 성공 loginId={}", member.getLoginId());

        return LoginResponse.of(token, member);
    }
}
