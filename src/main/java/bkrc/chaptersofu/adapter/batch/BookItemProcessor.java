package bkrc.chaptersofu.adapter.batch;

import bkrc.chaptersofu.application.aladin.AladinBookRepository;
import bkrc.chaptersofu.application.aladin.provided.AladinFinder;
import bkrc.chaptersofu.application.aladin.CategoryService;
import bkrc.chaptersofu.application.bkrc.BookRepository;
import bkrc.chaptersofu.application.bkrc.provided.BkrcFinder;
import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.aladin.Category;
import bkrc.chaptersofu.domain.book.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class BookItemProcessor implements ItemProcessor<Book, Book>, ItemStream {

    private final BkrcFinder bkrcFinder;
    private final BookRepository bookRepository;
    private final CategoryService categoryService;
    private Set<Integer> allowedCategoryIds; // 캐싱
    private Set<String> registeredIsbn13s;
    // Step 시작 시 한 번만 조회
    @Override
    public void open(ExecutionContext executionContext) {
        this.registeredIsbn13s = bookRepository.findAllIsbn13s();
        this.allowedCategoryIds = categoryService.findAcceptedCategories().stream()
                .map(Category::getCid)
                .collect(Collectors.toSet());
    }

    @Override
    public Book process(Book item) {
        //if (registeredIsbn13s.contains(item.getIsbn13())) return null;
        //if (!item.isInAllowedCategories(this.allowedCategoryIds)) return null;
        // 3. 1년 이내 출판된 책이 아니면 skip
        if (!item.wasPublishedBeforeOneYearAgo(LocalDate.now())) return null;

        // 알라딘 API로 상세 정보 보강
        Book detail = bkrcFinder.settingBookDetail(item.getIsbn13());
        return detail;
    }
}
