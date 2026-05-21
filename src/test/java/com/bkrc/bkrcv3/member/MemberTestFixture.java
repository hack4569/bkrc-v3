package com.bkrc.bkrcv3.member;

import com.bkrc.bkrcv3.GeneratorForTest;
import com.bkrc.bkrcv3.member.application.request.LoginForm;
import com.bkrc.bkrcv3.member.application.request.MemberRegisterRequest;
import com.bkrc.bkrcv3.member.application.response.LoginResponse;
import org.springframework.boot.test.web.client.LocalHostUriTemplateHandler;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;

public record MemberTestFixture(TestRestTemplate client) {
    public static MemberTestFixture create(Environment environment) {
        var client = new TestRestTemplate(new RestTemplateBuilder());
        var uriTemplateHandler = new LocalHostUriTemplateHandler(environment);
        client.setUriTemplateHandler(uriTemplateHandler);
        return new MemberTestFixture(client);
    }

    public void createMember(String loginId, String password) {
        MemberRegisterRequest request = new MemberRegisterRequest(
                loginId,
                password,
                password
        );

        client.postForEntity("/v1/member", request, Void.class);
    }

    public String loginToken(String loginId, String password) {
        var loginRequest = new LoginForm(loginId, password, false);
        LoginResponse loginResponse = client.postForObject("/login", loginRequest, LoginResponse.class);
        return loginResponse.token();
    }

    public String createMemberThenLoginToken() {
        String loginId = GeneratorForTest.generateLoginId();
        String password = GeneratorForTest.generatePassword();
        createMember(loginId, password);
        return loginToken(loginId, password);
    }
}
