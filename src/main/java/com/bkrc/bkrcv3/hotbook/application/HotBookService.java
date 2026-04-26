package com.bkrc.bkrcv3.hotbook.application;

import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.exception.BusinessException;
import com.bkrc.bkrcv3.required.HotBookEventHandler;
import com.bkrc.bkrcv3.aladin.application.AladinService;
import com.bkrc.bkrcv3.aladin.application.response.AladinBookResponse;
import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.required.EventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotBookService {
    private final List<HotBookEventHandler> eventHandlers;
    private final HotBookCalculator hotBookCalculator;
    private final HotBookRepository hotBookRepository;
    private final AladinService aladinService;

    private static final long HOT_ARTICLE_COUNT = 10;
    private static final Duration HOT_ARTICLE_TTL = Duration.ofDays(10);

    public void handleEvent(Event<EventPayload> event) {
        HotBookEventHandler<EventPayload> eventHandler = findEventHandler(event);

        Integer bookId = eventHandler.findBookId(event);

        eventHandler.handle(event);

        long score = hotBookCalculator.calculate(bookId);
        hotBookRepository.add(
                bookId,
                LocalDateTime.now(),
                score,
                HOT_ARTICLE_COUNT,
                HOT_ARTICLE_TTL
        );
    }

    public List<AladinBookResponse> readAll(String dateStr) {
        return hotBookRepository.readAll(dateStr).stream()
                .map(aladinService::getAladinBook)
                .filter(Objects::nonNull)
                .map(AladinBookResponse::from)
                .toList();
    }

    private HotBookEventHandler findEventHandler(Event<EventPayload> event) {
        return eventHandlers.stream()
                .filter(eventHandler -> eventHandler.supports(event))
                .findAny()
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_HANDLER_NOT_FOUND));
    }
}
