package bkrc.chaptersofu.application.hotbook;

import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.common.shared.EventHandlerException;
import bkrc.chaptersofu.application.required.HotBookEventHandler;
import bkrc.chaptersofu.application.aladin.provided.AladinFinder;
import bkrc.chaptersofu.application.hotbook.provided.HotBookFinder;
import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.application.required.event.Event;
import bkrc.chaptersofu.application.required.event.EventPayload;
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
public class HotBookService implements HotBookFinder {
    private final List<HotBookEventHandler> eventHandlers;
    private final HotBookCalculator hotBookCalculator;
    private final HotBookRepository hotBookRepository;
    private final AladinFinder aladinFinder;

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

    @Override
    public List<AladinBook> readAll(String dateStr) {
        return hotBookRepository.readAll(dateStr).stream()
                .map(aladinFinder::getAladinBook)
                .filter(Objects::nonNull)
                .toList();
    }

    private HotBookEventHandler findEventHandler(Event<EventPayload> event) {
        return eventHandlers.stream()
                .filter(eventHandler -> eventHandler.supports(event))
                .findAny()
                .orElseThrow(() -> new EventHandlerException(ErrorCode.EVENT_HANDLER_NOT_FOUND));
    }
}
