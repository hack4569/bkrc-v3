package bkrc.chaptersofu.application.bkrc;

import bkrc.chaptersofu.application.bkrc.provided.BkrcRequest;
import bkrc.chaptersofu.application.required.Ai;
import bkrc.chaptersofu.application.required.BookDescriptionSummary;
import bkrc.chaptersofu.application.required.BookInfo;
import bkrc.chaptersofu.domain.aladin.BookComment;
import bkrc.chaptersofu.domain.aladin.MdRecommend;
import bkrc.chaptersofu.domain.aladin.Phrase;
import bkrc.chaptersofu.domain.aladin.SubInfo;
import bkrc.chaptersofu.domain.book.Book;
import bkrc.chaptersofu.domain.book.BookFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 도서 상세 조회 시 AI 요약/추천, 편집자 추천, 책 속 문장, 목차를 코멘트로 가공하는 규칙을 검증한다.
 */
class BkrcServiceTest {
    private BookInfo bookInfo;
    private Ai ai;
    private BookRepository bookRepository;
    private BkrcService service;

    @BeforeEach
    void setUp() {
        bookInfo = mock(BookInfo.class);
        ai = mock(Ai.class);
        bookRepository = mock(BookRepository.class);
        service = new BkrcService(bookInfo, ai, bookRepository);
    }

    @Test
    void 도서_상세에_AI와_부가정보_코멘트를_설정한다() {
        SubInfo subInfo = new SubInfo(
                // 첫 번째 문장은 대표 문구와 중복되므로 서비스에서 건너뛴다.
                List.of(new Phrase("1", "건너뛸 문장"), new Phrase("2", "<b>첫째</b>. 둘째. 셋째. 넷째"),
                        new Phrase("3", "<br/>")),
                List.of(new MdRecommend("제목", "편집자 원문", "MD")),
                "<p>1장</p><P>2장</P>");
        Book book = BookFixture.book(1L, "데미안", "헤세", "국내도서>소설", "2020-01-01",
                subInfo, "소개1", "소개2", null, null);
        when(bookInfo.getBookDetail("isbn")).thenReturn(book);
        when(ai.summarizeDescriptions("소개2")).thenReturn(new BookDescriptionSummary("개요", "통찰"));
        when(ai.filteringContent("편집자 원문")).thenReturn("편집자 요약");
        when(ai.getRecommend("데미안")).thenReturn(List.of("명언1", "명언2"));

        Book result = service.settingBookDetail("isbn");

        // HTML 태그를 제거하고 문장은 최대 3개까지만 노출해야 한다.
        assertThat(result.getBookCommentList())
                .extracting(BookComment::getType, BookComment::getComment)
                .containsExactly(
                        tuple("description", "개요"),
                        tuple("descriptionInsight", "통찰"),
                        tuple("mdRecommend", "편집자 요약"),
                        tuple("aiRecommend", "명언1<br>명언2"),
                        tuple("phrase", "첫째.  둘째.  셋째. "),
                        tuple("toc", "1장2장"));
        assertThat(result.getBookCommentList()).allSatisfy(comment -> assertThat(comment.getBook()).isSameAs(book));
    }

    @Test
    void 부가정보와_AI_결과가_없으면_코멘트를_만들지_않는다() {
        Book book = BookFixture.book(1L, "데미안", "헤세", "국내도서>소설", "2020-01-01",
                new SubInfo(null, null, null), null, null, null, null);
        when(bookInfo.getBookDetail("isbn")).thenReturn(book);
        when(ai.getRecommend(anyString())).thenReturn(List.of());

        Book result = service.settingBookDetail("isbn");

        assertThat(result.getBookCommentList()).isEmpty();
        // 소개글이 없으면 불필요한 AI 호출 비용이 발생하지 않아야 한다.
        verify(ai, never()).summarizeDescriptions(anyString());
    }

    @Test
    void 베스트셀러와_검색_조회는_도서_공급자에_위임한다() {
        Book book = BookFixture.book(1L, "데미안", "헤세", "소설", "2020-01-01");
        BkrcRequest request = BkrcRequest.builder().maxResults(10).start(2).build();
        when(bookInfo.getBestSellerBooks(10, 2)).thenReturn(List.of(book));
        when(bookInfo.searchBooks("데미안")).thenReturn(List.of(book));

        assertThat(service.getBestSeller(request)).containsExactly(book);
        assertThat(service.searchBooks("데미안")).containsExactly(book);
        // 공급자가 null을 반환해도 호출부가 NPE 없이 처리할 수 있도록 빈 목록으로 변환한다.
        assertThat(service.searchBooks("없음")).isEmpty();
    }

    @Test
    void 삭제된_도서_ID는_null을_반환한다() {
        Book book = BookFixture.book(1L, "데미안", "헤세", "소설", "2020-01-01");
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.findById(2L)).thenReturn(Optional.empty());

        assertThat(service.getBook(1L)).isSameAs(book);
        assertThat(service.getBook(2L)).isNull();
    }
}
