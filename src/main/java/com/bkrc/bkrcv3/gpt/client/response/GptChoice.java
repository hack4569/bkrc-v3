package com.bkrc.bkrcv3.gpt.client.response;

import com.bkrc.bkrcv3.gpt.client.request.GptMessage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GptChoice {
    private int index;
    private GptMessage message;

    @JsonProperty("finish_reason")
    private String finishReason;
}
