package com.bkrc.bkrcv3.gpt;

import com.bkrc.bkrcv3.adapter.gpt.GptClient;
import com.bkrc.bkrcv3.adapter.gpt.request.GptMessage;
import com.bkrc.bkrcv3.adapter.gpt.request.GptRequest;
import com.bkrc.bkrcv3.adapter.gpt.response.GptResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GptClientTest {
//    @Autowired
//    private GptClient gptClient;
//
//    @Test
//    void gptApiTest() {
//        var msgs = List.of(
//                new GptMessage("system", "너는 누군가의 작은 불씨를 살리는 책을 추천하는 역할이야. 그리고 책속의 3가지 이하의 명언이나 좋은 내용의 문장을 3가지 이하 소개하는 책임이 있어"),
//                new GptMessage("user", "참을 수 없는 존재의 가벼움"),
//                new GptMessage("assistant", "{'result':['사랑은 힘을 빼는 것이다.','한 번은 아무것도 아니다. 한 번뿐인 것은 전혀 있었던 것이 아니다.']}"),
//                new GptMessage("user", "노인과 바다"),
//                new GptMessage("assistant", "{'result':['인간은 패배하도록 만들어지지 않았다. 인간은 파괴될 수는 있어도 패배하지는 않는다.','지금은 생각할 때가 아니다. 지금은 해야 할 때다']}"),
//                new GptMessage("user", "사랑의 기술")
//        );
//        GptRequest gptRequest = GptRequest.create(msgs);
//        GptResponse response = gptClient.getChatResponse(gptRequest);
//        System.out.println(response.getChoices());
//    }
}