package bkrc.chaptersofu.adapter.client.gpt.dto;

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
