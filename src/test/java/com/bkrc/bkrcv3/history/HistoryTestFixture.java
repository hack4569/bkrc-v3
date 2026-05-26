package com.bkrc.bkrcv3.history;

import com.bkrc.bkrcv3.member.MemberTestFixture;
import org.springframework.boot.test.web.client.LocalHostUriTemplateHandler;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public record HistoryTestFixture(TestRestTemplate client) {
    public static HistoryTestFixture create(Environment environment) {
        var client = new TestRestTemplate(new RestTemplateBuilder());
        var uriTemplateHandler = new LocalHostUriTemplateHandler(environment);
        client.setUriTemplateHandler(uriTemplateHandler);
        return new HistoryTestFixture(client);
    }

    public ResponseEntity<Void> saveHistory(String token, Integer itemId) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        return client.exchange(
                "/v1/history?itemId="+itemId,
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Void.class
        );
    }

}
