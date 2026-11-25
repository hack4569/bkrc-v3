package bkrc.chaptersofu.adapter.client.yes24;

import bkrc.chaptersofu.domain.book.BookMall;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class Yes24ClientTest {

    @Test
    void 스테디셀러를_조회해_공통_도서로_변환한다() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://apis.yes24.com")
                .defaultHeader("X-Api-Key", "test-api-key");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        Yes24Client client = new Yes24Client(builder.build(), "001");

        // 필수 카테고리와 페이지 파라미터 및 인증 헤더가 문서 규격대로 전달되는지 검증한다.
        server.expect(requestTo("https://apis.yes24.com/v1/category/bestsellerSteady"
                        + "?categoryId=001&page=2&pageSize=20&detail=Y"))
                .andExpect(header("X-Api-Key", "test-api-key"))
                .andRespond(withSuccess(successResponse(), MediaType.APPLICATION_JSON));

        var books = client.getBestSellerBooks(20, 2);

        assertThat(books).hasSize(1);
        assertThat(books.get(0).getItemId()).isEqualTo(12345678);
        assertThat(books.get(0).getMallName()).isEqualTo(BookMall.YES24);
        assertThat(books.get(0).getTitle()).isEqualTo("클린 코드");
        assertThat(books.get(0).getIsbn13()).isEqualTo("9788966260959");
        assertThat(books.get(0).getDescription()).isEqualTo("소프트웨어 장인 정신을 담은 도서입니다.");
        assertThat(books.get(0).getCustomerReviewRank()).isEqualTo(9);
        assertThat(books.get(0).getBestRank()).isEqualTo(1);
        assertThat(books.get(0).getSubInfo().getToc()).isEqualTo("1장 깨끗한 코드");
        server.verify();
    }

    @Test
    void 페이지_크기가_문서의_허용_범위를_벗어나면_호출하지_않는다() {
        // 잘못된 요청은 외부 API까지 보내지 않고 호출부에서 즉시 확인할 수 있어야 한다.
        Yes24Client client = new Yes24Client(RestClient.create(), "001");

        assertThatThrownBy(() -> client.getBestSellerBooks(101, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxResults는 1에서 100 사이여야 합니다.");
    }

    @Test
    void ISBN13으로_상품_상세를_조회해_공통_도서로_변환한다() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://apis.yes24.com")
                .defaultHeader("X-Api-Key", "test-api-key");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        Yes24Client client = new Yes24Client(builder.build(), "001");

        // 상세 정보가 포함되도록 ISBN13 검색 유형과 detail=Y가 전달되는지 확인한다.
        server.expect(requestTo("https://apis.yes24.com/v1/goods/itemDetail"
                        + "?searchType=ISBN13&query=9788966260959&detail=Y"))
                .andExpect(header("X-Api-Key", "test-api-key"))
                .andRespond(withSuccess(successResponse(), MediaType.APPLICATION_JSON));

        var book = client.getBookDetail("9788966260959");

        assertThat(book.getItemId()).isEqualTo(12345678);
        assertThat(book.getIsbn13()).isEqualTo("9788966260959");
        assertThat(book.getFullDescription()).isEqualTo("소프트웨어 장인 정신을 담은 도서입니다.");
        assertThat(book.getFullDescription2()).isEqualTo("깨끗한 코드 작성법");
        assertThat(book.getToc()).isEqualTo("1장 깨끗한 코드");
        server.verify();
    }

    @Test
    void 잘못된_ISBN13은_외부_API를_호출하기_전에_거부한다() {
        Yes24Client client = new Yes24Client(RestClient.create(), "001");

        // YES24의 PARAM_004 오류를 받기 전에 명확한 입력 오류를 호출자에게 제공한다.
        assertThatThrownBy(() -> client.getBookDetail("978-8966260959"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("isbn13은 13자리 숫자여야 합니다.");
    }

    @Test
    void 검색어로_도서를_조회해_공통_도서로_변환한다() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://apis.yes24.com")
                .defaultHeader("X-Api-Key", "test-api-key");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        Yes24Client client = new Yes24Client(builder.build(), "001");

        // 공통 검색 계약에 맞춰 도서만 첫 페이지에서 10건 조회하고 상세 매핑 정보도 요청한다.
        server.expect(requestTo("https://apis.yes24.com/v1/goods/itemList"
                        + "?query=%ED%81%B4%EB%A6%B0%20%EC%BD%94%EB%93%9C&category=BOOK&sort=DEFAULT"
                        + "&page=1&pageSize=10&detail=Y"))
                .andExpect(header("X-Api-Key", "test-api-key"))
                .andRespond(withSuccess(successResponse(), MediaType.APPLICATION_JSON));

        var books = client.searchBooks("  클린 코드  ");

        assertThat(books).hasSize(1);
        assertThat(books.get(0).getItemId()).isEqualTo(12345678);
        assertThat(books.get(0).getMallName()).isEqualTo(BookMall.YES24);
        assertThat(books.get(0).getDescription()).isEqualTo("소프트웨어 장인 정신을 담은 도서입니다.");
        server.verify();
    }

    @Test
    void 검색_결과가_없으면_빈_목록을_반환한다() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://apis.yes24.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        Yes24Client client = new Yes24Client(builder.build(), "001");

        // YES24가 결과 없음에 사용하는 404와 SEARCH_001 조합은 정상적인 빈 검색 결과로 취급한다.
        server.expect(requestTo("https://apis.yes24.com/v1/goods/itemList"
                        + "?query=%EC%97%86%EB%8A%94%EC%B1%85&category=BOOK&sort=DEFAULT"
                        + "&page=1&pageSize=10&detail=Y"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {
                                  "success": false,
                                  "message": "검색 결과가 없습니다.",
                                  "errorCode": "SEARCH_001"
                                }
                                """));

        assertThat(client.searchBooks("없는책")).isEmpty();
        server.verify();
    }

    @Test
    void 공백_검색어는_외부_API를_호출하기_전에_거부한다() {
        Yes24Client client = new Yes24Client(RestClient.create(), "001");

        // 명세상 필수인 검색어가 실질적으로 비어 있으면 호출자에게 즉시 입력 오류를 알린다.
        assertThatThrownBy(() -> client.searchBooks("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("query는 공백일 수 없습니다.");
    }

    private String successResponse() {
        // 문서 예시 중 변환에 필요한 필드만 사용해 테스트의 의도를 분명하게 유지한다.
        return """
                {
                  "success": true,
                  "message": "성공",
                  "data": {
                    "items": [{
                      "sortOrder": 1,
                      "itemId": 12345678,
                      "title": "클린 코드",
                      "author": "로버트 C. 마틴",
                      "goodsType": "국내도서",
                      "adultYn": "N",
                      "publisher": "인사이트",
                      "isbn10": "8966260950",
                      "isbn13": "9788966260959",
                      "publishDate": "2013-12-24",
                      "starScore": 9.4,
                      "cover": "https://image.yes24.com/goods/12345678/L",
                      "link": "https://www.yes24.com/product/goods/12345678",
                      "addOnLink": "",
                      "contentDetail": {
                        "bookIntroduction": "소프트웨어 장인 정신을 담은 도서입니다.",
                        "bookSummary": "깨끗한 코드 작성법",
                        "tableOfContents": "1장 깨끗한 코드"
                      }
                    }],
                    "currentPage": 2,
                    "pageSize": 20,
                    "totalCount": 100
                  }
                }
                """;
    }
}
