package com.bkrc.bkrcv3.adapter.gpt.request;

import lombok.NoArgsConstructor;

public record GptMessage(String role, String content) {
}
