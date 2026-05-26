package com.bkrc.bkrcv3.history;

import com.bkrc.bkrcv3.GeneratorForTest;
import com.bkrc.bkrcv3.api.CommonApiTest;
import com.bkrc.bkrcv3.member.MemberTestFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@CommonApiTest
public class HistoryTest {
    @Autowired
    MemberTestFixture memberFixture;

    @Autowired
    HistoryTestFixture historyTestFixture;

    @Test
    void 인증된_사용자가_유효한_itemId로_이력_저장_성공() {
        String token = memberFixture.createMemberThenLoginToken();

        var response = historyTestFixture.saveHistory(token, GeneratorForTest.generateItemId());

        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void 이미_존재하는_경우_409(
            @Autowired TestRestTemplate client
    ) {
        //Arrange
        String token = memberFixture.createMemberThenLoginToken();
        int itemId = GeneratorForTest.generateItemId();
        //Act
        var response = historyTestFixture.saveHistory(token, itemId);
        var response2 = historyTestFixture.saveHistory(token, itemId);
        //Assert
        assertThat(response2.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void save메소드_호출시_item_id_login_id_없으면_4_xx에러(
        @Autowired TestRestTemplate client
    ) {
        //Arrange
        String token = memberFixture.createMemberThenLoginToken();
        Integer itemId = null;
        //Act
        var response = historyTestFixture.saveHistory(token, itemId);
        //Assert
        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }
}
