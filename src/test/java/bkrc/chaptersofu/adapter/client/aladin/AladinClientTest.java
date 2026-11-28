package bkrc.chaptersofu.adapter.client.aladin;

import bkrc.chaptersofu.domain.aladin.AladinClientException;
import bkrc.chaptersofu.domain.aladin.AladinException;
import bkrc.chaptersofu.domain.book.Book;
import bkrc.chaptersofu.domain.book.BookMall;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * 알라딘 API 호출 경로/파라미터와 응답을 공통 도서로 변환하는 과정을 검증한다.
 * AladinClient는 @PostConstruct에서 RestClient를 직접 생성하므로,
 * 초기화 코드를 실행한 뒤 MockRestServiceServer에 바인딩된 RestClient로 교체해 외부 호출을 차단한다.
 */
class AladinClientTest {
    private static final String HOST = "https://aladin.test";
    private static final String ONE_BOOK = """
            {"item":[{"itemId":1,"title":"데미안","author":"헤세","isbn13":"9781234567890"}]}
            """;

    private AladinClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        client = new AladinClient();
        ReflectionTestUtils.setField(client, "aladinHost", HOST);
        ReflectionTestUtils.setField(client, "aladinTbKey", "ttb-key");
        ReflectionTestUtils.setField(client, "connectTimeout", Duration.ofSeconds(1));
        ReflectionTestUtils.setField(client, "readTimeout", Duration.ofSeconds(1));
        client.initRestClient();

        RestClient.Builder builder = RestClient.builder().baseUrl(HOST);
        server = MockRestServiceServer.bindTo(builder).build();
        ReflectionTestUtils.setField(client, "aladinApi", builder.build());
    }

    @Test
    void 베스트셀러를_조회해_알라딘_공통_도서로_변환한다() {
        // 인증 키와 조회 유형이 쿼리 파라미터로 함께 전달되어야 한다.
        server.expect(requestTo(allOf(containsString(HOST + "/ttb/api/ItemList.aspx"),
                        containsString("ttbkey=ttb-key"), containsString("querytype=BestSeller"))))
                .andRespond(withSuccess(ONE_BOOK, MediaType.APPLICATION_JSON));

        List<Book> books = client.getBestSellerBooks(10, 1);

        assertThat(books).singleElement().satisfies(book -> {
            assertThat(book.getTitle()).isEqualTo("데미안");
            assertThat(book.getMallName()).isEqualTo(BookMall.ALADIN);
        });
        server.verify();
    }

    @Test
    void 검색어는_앞뒤_공백을_제거해_키워드_검색한다() {
        server.expect(requestTo(allOf(containsString("/ttb/api/ItemSearch.aspx"),
                        containsString("querytype=Keyword"))))
                .andRespond(withSuccess(ONE_BOOK, MediaType.APPLICATION_JSON));

        assertThat(client.searchBooks("  데미안  ")).extracting(Book::getTitle).containsExactly("데미안");
        server.verify();
    }

    @Test
    void ISBN으로_상세를_조회하고_결과가_없으면_예외가_발생한다() {
        server.expect(requestTo(containsString("/ttb/api/ItemLookUp.aspx")))
                .andRespond(withSuccess(ONE_BOOK, MediaType.APPLICATION_JSON));
        server.expect(requestTo(containsString("/ttb/api/ItemLookUp.aspx")))
                .andRespond(withSuccess("{\"item\":[]}", MediaType.APPLICATION_JSON));

        assertThat(client.getBookDetail("9781234567890").getIsbn13()).isEqualTo("9781234567890");
        assertThatThrownBy(() -> client.getBookDetail("0000000000000")).isInstanceOf(AladinException.class);
    }

    @Test
    void 외부_API_오류는_알라딘_클라이언트_예외로_감싼다() {
        server.expect(requestTo(containsString("/ttb/api/ItemList.aspx"))).andRespond(withServerError());

        // 호출부가 외부 라이브러리 예외 타입에 의존하지 않도록 도메인 예외로 변환해야 한다.
        assertThatThrownBy(() -> client.getBestSellerBooks(10, 1)).isInstanceOf(AladinClientException.class);
    }
}
