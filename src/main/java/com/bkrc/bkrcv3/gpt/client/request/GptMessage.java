package com.bkrc.bkrcv3.gpt.client.request;

import lombok.NoArgsConstructor;

public record GptMessage(String role, String content) {
}
