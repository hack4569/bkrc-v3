package com.bkrc.bkrcv3.gpt.client.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GptResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<GptChoice> choices;
}
