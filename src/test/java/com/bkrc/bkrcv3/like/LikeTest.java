package com.bkrc.bkrcv3.like;

import com.bkrc.bkrcv3.GeneratorForTest;
import com.bkrc.bkrcv3.aladin.AladinBookTestFixture;
import com.bkrc.bkrcv3.api.CommonApiTest;
import com.bkrc.bkrcv3.common.shared.Snowflake;
import com.bkrc.bkrcv3.like.application.response.LikeResponse;
import com.bkrc.bkrcv3.like.entity.Like;
import com.bkrc.bkrcv3.member.MemberTestFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@CommonApiTest
public class LikeTest {
    @Autowired
    MemberTestFixture memberFixture;
    @Autowired
    AladinBookTestFixture aladinBookTestFixture;
    @Autowired
    LikeTestFixture likeTestFixture;

//    @Test
//    void like에_성공적으로_등록한_경우_200() {
//        //Arrange
//        String token = memberFixture.createMemberThenLoginToken();
//        var aladinBookList = aladinBookTestFixture.getAladinBooksForTest();
//        Integer itemId = GeneratorForTest.generateItemId();
//        //Act
//        var response = likeTestFixture.like(token, itemId);
//
//        //Assert
//        assertThat(response.getStatusCode().value()).isEqualTo(200);
//        assertThat(response.getBody().likeCount()).isNotEqualTo(0);
//    }
//
//    @Test
//    void like_해제_200() {
//        //Arrange
//        String token = memberFixture.createMemberThenLoginToken();
//        Integer itemId = GeneratorForTest.generateItemId();
//
//        //Act
//        likeTestFixture.like(token, itemId);
//        var unlikeRes = likeTestFixture.unlike(token, itemId);
//
//        //Assert
//        assertThat(unlikeRes.getStatusCode().value()).isEqualTo(200);
//    }
}
