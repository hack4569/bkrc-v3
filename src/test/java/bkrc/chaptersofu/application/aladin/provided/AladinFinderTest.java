package bkrc.chaptersofu.application.aladin.provided;

import bkrc.chaptersofu.application.aladin.AladinService;
import bkrc.chaptersofu.application.aladin.AladinBookRepository;
import bkrc.chaptersofu.application.aladin.cache.AladinBookCache;
import bkrc.chaptersofu.application.required.Ai;
import bkrc.chaptersofu.application.required.BookInfo;
import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.aladin.SubInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AladinFinderTest {
    @Mock BookInfo bookInfo;
    @Mock Ai ai;
    @Mock AladinBookRepository aladinBookRepository;
    @Mock AladinBookCache aladinBookCache;

    private AladinFinder aladinFinder;

    @BeforeEach
    void setUp() {
        aladinFinder = new AladinService(bookInfo, ai, aladinBookRepository, aladinBookCache);
    }

    @Test
    void 알라딘_목록_조회_요청값을_외부_도서_포트에_전달한다() {
        AladinRequest request = AladinRequest.builder()
                .querytype("Bestseller")
                .maxResults(20)
                .start(2)
                .build();
        List<AladinBook> books = List.of(mock(AladinBook.class));
        when(bookInfo.getBooks("Bestseller", 20, 2)).thenReturn(books);

        assertThat(aladinFinder.getAladinItemList(request)).isSameAs(books);
    }

    @Test
    void 검색어로_알라딘_도서를_검색한다() {
        List<AladinBook> books = List.of(mock(AladinBook.class));
        when(bookInfo.searchBooks("클린 코드")).thenReturn(books);

        assertThat(aladinFinder.searchBooks("클린 코드")).isSameAs(books);
    }

    @Test
    void 검색_결과가_null이면_빈_목록을_반환한다() {
        when(bookInfo.searchBooks("없는 책")).thenReturn(null);

        assertThat(aladinFinder.searchBooks("없는 책")).isEmpty();
    }

    @Test
    void 전체_도서가_캐시에_있으면_DB를_조회하지_않는다() {
        List<AladinBook> cached = List.of(mock(AladinBook.class));
        when(aladinBookCache.find()).thenReturn(Optional.of(cached));

        assertThat(aladinFinder.findAll()).isSameAs(cached);
        verifyNoInteractions(aladinBookRepository);
    }

    @Test
    void 전체_도서_캐시가_없으면_DB_결과를_캐시에_저장한다() {
        List<AladinBook> books = List.of(mock(AladinBook.class));
        when(aladinBookCache.find()).thenReturn(Optional.empty());
        when(aladinBookRepository.findAllWithBookComments()).thenReturn(books);

        assertThat(aladinFinder.findAll()).isSameAs(books);
        verify(aladinBookCache).save(books);
    }

    @Test
    void 도서_ID로_알라딘_도서를_조회한다() {
        AladinBook book = mock(AladinBook.class);
        when(aladinBookRepository.findById(10)).thenReturn(Optional.of(book));

        assertThat(aladinFinder.getAladinBook(10)).isSameAs(book);
    }

    @Test
    void 도서_ID가_없으면_null을_반환한다() {
        when(aladinBookRepository.findById(10)).thenReturn(Optional.empty());

        assertThat(aladinFinder.getAladinBook(10)).isNull();
    }

    @Test
    void ISBN으로_상세_도서를_조회하고_AI_코멘트를_설정한다() {
        AladinBook detail = mock(AladinBook.class);
        SubInfo subInfo = mock(SubInfo.class);
        when(bookInfo.getBookDetail("9781234567890")).thenReturn(detail);
        when(detail.getSubInfo()).thenReturn(subInfo);
        when(detail.getTitle()).thenReturn("테스트 도서");
        when(ai.getRecommend("테스트 도서")).thenReturn(List.of());

        assertThat(aladinFinder.settingAladinDetail("9781234567890")).isSameAs(detail);
        verify(detail).settingBookCommentList(List.of());
    }

    @Test
    void 도서_목록을_Redis에_저장한다() {
        List<AladinBook> books = List.of(mock(AladinBook.class));

        aladinFinder.saveListForRedis(books);

        verify(aladinBookCache).save(books);
    }
}
