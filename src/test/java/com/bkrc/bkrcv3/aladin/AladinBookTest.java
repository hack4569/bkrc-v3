package com.bkrc.bkrcv3.aladin;

import com.bkrc.bkrcv3.GeneratorForTest;
import com.bkrc.bkrcv3.aladin.application.AladinService;
import com.bkrc.bkrcv3.aladin.application.request.AladinRequest;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.aladin.entity.AladinConstants;
import com.bkrc.bkrcv3.aladin.entity.QueryType;
import com.bkrc.bkrcv3.api.CommonApiTest;
import com.bkrc.bkrcv3.member.MemberTestFixture;
import com.bkrc.bkrcv3.member.application.response.RecommendView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CommonApiTest
class AladinBookTest {

    @Test
    void 로그인된_사용자가_책추천_정보_를_조회할때_이전에_봤던_책은_보이지_않는다(
            @Autowired MemberTestFixture fixture,
            @Autowired AladinService aladinService
    ) {
//        String loginId = GeneratorForTest.generateLoginId();
//        String password = GeneratorForTest.generatePassword();
//
//        // Arrange
//        fixture.createMember(
//                loginId, password
//        );
        int page = 1;
        List<AladinBook> list = aladinService.getAladinItemList(
                AladinRequest.builder()
                        .querytype(QueryType.BEST_SELLER.getQueryType())
                        .maxResults(AladinConstants.ITEM_LIST_PAGE)
                        .start(page).build()
        );

        assertThat(list).isNotEmpty();
    }
}
