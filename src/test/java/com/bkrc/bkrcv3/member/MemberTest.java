package com.bkrc.bkrcv3.member;

import com.bkrc.bkrcv3.GeneratorForTest;
import com.bkrc.bkrcv3.api.CommonApiTest;
import com.bkrc.bkrcv3.member.application.UserService;
import com.bkrc.bkrcv3.member.application.request.MemberRegisterRequest;
import com.bkrc.bkrcv3.member.entity.PasswordEncoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@CommonApiTest
public class MemberTest {
    @Autowired
    UserService userService;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void 유효한_요청으로_회원가입_성공(
            @Autowired TestRestTemplate client
    ) {
        String pw = GeneratorForTest.generatePassword();
        MemberRegisterRequest request = new MemberRegisterRequest(
                GeneratorForTest.generateLoginId(),
                pw,
                pw
        );

        ResponseEntity<Void> response = client.postForEntity("/v1/member", request, Void.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void 비밀번호가_bcrypt로_암호화되어_저장(){
        String pw = GeneratorForTest.generatePassword();
        MemberRegisterRequest request = new MemberRegisterRequest(
                GeneratorForTest.generateLoginId(),
                pw,
                pw
        );

        var member = userService.saveMember(request);

        assertThat(passwordEncoder.matches(pw, member.getPassword())).isTrue();
    }

    @Test
    void 비밀번호_불일치_시_400_bad_request_코드_반환(
            @Autowired TestRestTemplate client
    ) {
        String pw = GeneratorForTest.generatePassword();
        MemberRegisterRequest request = new MemberRegisterRequest(
                GeneratorForTest.generateLoginId(),
                pw,
                pw + "mismatch"
        );

        ResponseEntity<Void> response = client.postForEntity("/v1/member", request, Void.class);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "short",
            "longlonglonglonglonglonglonglonglonglong"
    })
    void 비밀번호_값_올바르지_않으면_400(
            String pw,
            @Autowired TestRestTemplate client
    ) {
        MemberRegisterRequest request = new MemberRegisterRequest(
                GeneratorForTest.generateLoginId(),
                pw,
                pw
        );

        ResponseEntity<Void> response = client.postForEntity("/v1/member", request, Void.class);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void 중복_아이디_가입_시_409_conflict_코드_반환(
            @Autowired TestRestTemplate client
    ) {
        String loginId = GeneratorForTest.generateLoginId();
        String pw = GeneratorForTest.generatePassword();
        MemberRegisterRequest request = new MemberRegisterRequest(
                loginId,
                pw,
                pw
        );

        userService.saveMember(request);

        ResponseEntity<Void> response = client.postForEntity("/v1/member", request, Void.class);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void 아이디_빈값(
            @Autowired TestRestTemplate client
    ) {
        String pw = GeneratorForTest.generatePassword();
        MemberRegisterRequest request = new MemberRegisterRequest(
                "",
                pw,
                pw
        );

        ResponseEntity<Void> response = client.postForEntity("/v1/member", request, Void.class);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }
}
