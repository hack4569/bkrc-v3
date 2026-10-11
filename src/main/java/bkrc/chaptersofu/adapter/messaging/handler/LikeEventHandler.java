package bkrc.chaptersofu.adapter.messaging.handler;

import bkrc.chaptersofu.domain.shared.event.Event;
import bkrc.chaptersofu.domain.shared.event.EventType;
import bkrc.chaptersofu.application.event.payload.BookLikeEventPayload;
import bkrc.chaptersofu.application.required.HotBookEventHandler;
import bkrc.chaptersofu.application.like.LikeCountEventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class LikeEventHandler implements HotBookEventHandler<BookLikeEventPayload> {
    private final LikeCountEventProcessor likeCountEventProcessor;

    @Override
    public void handle(Event<BookLikeEventPayload> event) {
        BookLikeEventPayload payload = event.getPayload();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.plusDays(1).with(LocalTime.MIDNIGHT);
        var ttl = Duration.between(now, midnight);
        likeCountEventProcessor.process(payload, ttl);
    }

    @Override
    public boolean supports(Event<BookLikeEventPayload> event) {
        return event.getType() == EventType.BOOK_LIKE;
    }

    @Override
    public Integer findBookId(Event<BookLikeEventPayload> event) {
        return event.getPayload().getBookId();
    }
}
