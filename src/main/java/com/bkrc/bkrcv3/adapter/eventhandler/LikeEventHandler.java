package com.bkrc.bkrcv3.adapter.eventhandler;

import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.common.event.EventType;
import com.bkrc.bkrcv3.adapter.payload.BookLikeEventPayload;
import com.bkrc.bkrcv3.required.HotBookEventHandler;
import com.bkrc.bkrcv3.like.application.LikeService;
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
    private final LikeService likeService;

    @Override
    public void handle(Event<BookLikeEventPayload> event) {
        BookLikeEventPayload payload = event.getPayload();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.plusDays(1).with(LocalTime.MIDNIGHT);
        var ttl = Duration.between(now, midnight);
        likeService.createOrUpdate(
            payload.getBookId(),
            payload.getBookLikeCount(),
            ttl);
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
