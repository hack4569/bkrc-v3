package bkrc.chaptersofu.adapter.batch;

import bkrc.chaptersofu.application.bkrc.BookRepository;
import bkrc.chaptersofu.domain.book.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookItemWriter implements ItemWriter<Book> {
    private final BookRepository bookRepository;

    @Override
    public void write(Chunk<? extends Book> chunk) throws Exception {
        List<? extends Book> items = chunk.getItems();
        if (CollectionUtils.isEmpty(items)) {
            log.debug("[Batch] 저장할 항목 없음, skip");
            return;
        }
        // 동일 판매처와 itemId의 기존 도서는 bookId를 이어받아 신규 INSERT 대신 갱신한다.
        items.forEach(book -> bookRepository
                .findByMallNameAndItemId(book.getMallName(), book.getItemId())
                .ifPresent(existing -> book.restoreBookId(existing.getBookId())));
        bookRepository.saveAll(items);
        log.info("[Batch] chunk 저장 완료 count={}", items.size());
    }
}

