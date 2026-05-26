package com.bkrc.bkrcv3.like;

import com.bkrc.bkrcv3.like.application.response.LikeResponse;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public record LikeTestFixture(TestRestTemplate client) {
     public ResponseEntity<LikeResponse> like(String token, Integer itemId) {
         HttpHeaders headers = new HttpHeaders();
         headers.set("Authorization", "Bearer " + token);
         return client.exchange(
                 "/v1/like/"+itemId,
                 HttpMethod.POST,
                 new HttpEntity<>(headers),
                 LikeResponse.class
         );
     }

    public ResponseEntity<Void> unlike(String token, Integer itemId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return client.exchange(
                "/v1/like/cancel/"+itemId,
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Void.class
        );
    }
}
