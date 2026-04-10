package com.bkrc.bkrcv3.gpt.client.request;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GptRequest {
    @Builder.Default
    private boolean store = true;
    @Builder.Default
    private String model = "gpt-4o-mini";
    private List<GptMessage> messages;

    public static GptRequest create(final List<GptMessage> messages) {
        GptRequest request = new GptRequest();
        request.messages = messages;
        return request;
    }
}
