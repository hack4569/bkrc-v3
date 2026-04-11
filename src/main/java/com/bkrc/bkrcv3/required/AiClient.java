package com.bkrc.bkrcv3.required;

public interface AiClient<Q, R> {
    R getChatResponse(Q request);
}


