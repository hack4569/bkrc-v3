package com.bkrc.bkrcv3.ai;

public interface AiClient<Q, R> {
    R getChatResponse(Q request);
}


