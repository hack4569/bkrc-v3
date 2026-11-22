package bkrc.chaptersofu.adapter.batch;

import bkrc.chaptersofu.application.bkrc.provided.BkrcFinder;
import bkrc.chaptersofu.application.bkrc.provided.BkrcRequest;
import bkrc.chaptersofu.domain.book.Book;
import bkrc.chaptersofu.domain.book.BookConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class BookItemReader implements ItemReader<Book> {
    private final BkrcFinder bkrcFinder;
    private int page = 1;
    private Queue<Book> buffer = new LinkedList<>();
    private Set<String> seenExternalBookSet = new HashSet<>();

    @Override
    public Book read() {
        while (buffer.isEmpty()) {
            List<Book> bookItemList = bkrcFinder.getBestSeller(
                    BkrcRequest.builder()
                            .maxResults(BookConstants.ITEM_LIST_PAGE)
                            .start(page).build()
            );

            if (CollectionUtils.isEmpty(bookItemList)) return null;

            // 이번 페이지 isbn이 전부 이미 본 것이면 → 중복 페이지 → 종료
            boolean allDuplicated = bookItemList.stream()
                    .map(this::externalBookKey)
                    .anyMatch(seenExternalBookSet::contains);

            if (allDuplicated) return null;

            // 새로운 isbn만 등록하고 buffer에 추가
            bookItemList.stream()
                    .filter(book -> seenExternalBookSet.add(externalBookKey(book))) // add()가 false면 중복
                    .forEach(buffer::add);

            page++;
        }

        return buffer.poll();
    }

    private String externalBookKey(Book book) {
        // 판매처와 itemId를 함께 사용해 서로 다른 판매처의 동일 숫자 ID를 별개 도서로 처리한다.
        return book.getMallName() + ":" + book.getItemId();
    }

}
