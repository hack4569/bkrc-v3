package bkrc.chaptersofu.application.required;

public interface AiClient<Q, R> {
    R getChatResponse(Q request);
}


