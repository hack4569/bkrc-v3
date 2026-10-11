package bkrc.chaptersofu.adapter.security;

import bkrc.chaptersofu.GeneratorForTest;
import bkrc.chaptersofu.adapter.webapi.CommonApiTest;
import bkrc.chaptersofu.application.member.provided.LoginForm;
import bkrc.chaptersofu.application.member.provided.MemberRegisterRequest;
import bkrc.chaptersofu.adapter.security.dto.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static bkrc.chaptersofu.adapter.security.JwtAssertions.conformsToJwtFormat;
import static org.assertj.core.api.Assertions.assertThat;

@CommonApiTest
public class LoginTest {

    @Test
    void 로그인시_토큰반환(
            @Autowired TestRestTemplate client
            ) {
        //Arrange
        String loginId = GeneratorForTest.generateLoginId();
        String pw = GeneratorForTest.generatePassword();
        MemberRegisterRequest request = new MemberRegisterRequest(
                loginId,
                pw,
                pw
        );
        ResponseEntity<Void> response = client.postForEntity("/v1/member", request, Void.class);

        //Act
        var loginRequest = new LoginForm(loginId, pw, false);
        LoginResponse loginResponse = client.postForObject("/login", loginRequest, LoginResponse.class);

        //Assert
        assertThat(loginResponse.token()).isNotNull();
    }
    @Test
    void 존재하지_않는_로그인_아이디가_사용되면_401_상태코드를_반환한다(
            @Autowired TestRestTemplate client
    ) {
        //Arrange
        String loginId = GeneratorForTest.generateLoginId();
        String pw = GeneratorForTest.generatePassword();
        var loginRequest = new LoginForm(loginId, pw, false);

        //Act
        ResponseEntity<LoginResponse> response = client.postForEntity("/login", loginRequest, LoginResponse.class);

        //Assert
        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void 반한된_토큰이_JWT_형식을_따른다(
            @Autowired TestRestTemplate client
    ) {
        //Arrange
        String loginId = GeneratorForTest.generateLoginId();
        String pw = GeneratorForTest.generatePassword();
        MemberRegisterRequest request = new MemberRegisterRequest(
                loginId,
                pw,
                pw
        );
        client.postForEntity("/v1/member", request, Void.class);

        //Act
        var loginRequest = new LoginForm(loginId, pw, false);
        LoginResponse loginResponse = client.postForObject("/login", loginRequest, LoginResponse.class);

        //Assert
        assertThat(loginResponse.token()).satisfies(conformsToJwtFormat());
    }
}
