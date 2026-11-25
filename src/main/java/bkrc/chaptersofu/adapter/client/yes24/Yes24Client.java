package bkrc.chaptersofu.adapter.client.yes24;

import bkrc.chaptersofu.adapter.client.ExternalApiLoggingInterceptor;
import bkrc.chaptersofu.adapter.client.yes24.dto.Yes24BookItem;
import bkrc.chaptersofu.adapter.client.yes24.dto.Yes24Response;
import bkrc.chaptersofu.adapter.client.yes24.mapper.Yes24BookMapper;
import bkrc.chaptersofu.application.required.BookInfo;
import bkrc.chaptersofu.domain.book.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

/**
 * YES24 Open API를 호출해 외부 상품 정보를 공통 도서 모델로 제공한다.
 * 공급자가 YES24로 설정된 경우에만 빈으로 등록해 다른 BookInfo 구현체와의 충돌을 방지한다.
 */
@Component
@ConditionalOnProperty(name = "book-info.provider", havingValue = "yes24")
public class Yes24Client implements BookInfo {

    private static final String STEADY_BESTSELLER_PATH = "/v1/category/bestsellerSteady";
    private static final String ITEM_DETAIL_PATH = "/v1/goods/itemDetail";
    private static final String ITEM_LIST_PATH = "/v1/goods/itemList";
    private static final int SEARCH_PAGE_SIZE = 10;
    private static final String NO_SEARCH_RESULT_ERROR_CODE = "SEARCH_001";
    private static final int MIN_PAGE_SIZE = 1;
    private static final int MAX_PAGE_SIZE = 100;

    private final RestClient yes24Api;
    private final String categoryId;

    @Autowired
    public Yes24Client(
            RestClient.Builder restClientBuilder,
            @Value("${yes24.host}") String host,
            @Value("${yes24.api-key}") String apiKey,
            @Value("${yes24.category-id}") String categoryId,
            @Value("${external-api.yes24.connect-timeout:2s}") Duration connectTimeout,
            @Value("${external-api.yes24.read-timeout:5s}") Duration readTimeout
    ) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        // API Key는 모든 YES24 요청에 필요하므로 클라이언트 기본 헤더로 한 번만 설정한다.
        this.yes24Api = restClientBuilder
                .baseUrl(host)
                .defaultHeader("X-Api-Key", apiKey)
                .requestFactory(requestFactory)
                .requestInterceptor(new ExternalApiLoggingInterceptor("yes24"))
                .build();
        this.categoryId = categoryId;
    }

    /**
     * HTTP 요청을 실제 네트워크와 분리해 검증할 수 있도록 구성된 RestClient를 받는다.
     * 운영 환경에서는 @Autowired가 지정된 생성자만 사용하므로 빈 생성자 선택 충돌이 발생하지 않는다.
     */
    Yes24Client(RestClient yes24Api, String categoryId) {
        this.yes24Api = yes24Api;
        this.categoryId = categoryId;
    }

    @Override
    public List<Book> getBestSellerBooks(int maxResults, int start) {
        validatePagination(maxResults, start);

        try {
            Yes24Response response = yes24Api.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(STEADY_BESTSELLER_PATH)
                            .queryParam("categoryId", categoryId)
                            .queryParam("page", start)
                            .queryParam("pageSize", maxResults)
                            .queryParam("detail", "Y")
                            .build())
                    .retrieve()
                    .body(Yes24Response.class);

            // HTTP 200이어도 업무 오류가 포함될 수 있으므로 success 값을 별도로 확인한다.
            if (response == null || !Boolean.TRUE.equals(response.success())) {
                String message = response == null ? "응답 본문이 없습니다." : response.message();
                throw new Yes24ClientException("스테디셀러 조회에 실패했습니다: " + message);
            }

            return Yes24BookMapper.toBooks(response.data() == null ? null : response.data().items());
        } catch (Yes24ClientException exception) {
            throw exception;
        } catch (Exception exception) {
            // 네트워크 오류와 역직렬화 오류를 공급자 전용 예외로 통일해 상위 계층의 처리를 단순화한다.
            throw new Yes24ClientException("스테디셀러 API 호출에 실패했습니다.", exception);
        }
    }

    private void validatePagination(int maxResults, int start) {
        // YES24 문서의 pageSize 허용 범위를 호출 전에 검증해 불필요한 외부 요청을 막는다.
        if (maxResults < MIN_PAGE_SIZE || maxResults > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("maxResults는 1에서 100 사이여야 합니다.");
        }
        if (start < 1) {
            throw new IllegalArgumentException("start는 1 이상이어야 합니다.");
        }
    }

    @Override
    public Book getBookDetail(String isbn13) {
        validateIsbn13(isbn13);

        try {
            Yes24Response response = yes24Api.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ITEM_DETAIL_PATH)
                            .queryParam("searchType", "ISBN13")
                            .queryParam("query", isbn13)
                            .queryParam("detail", "Y")
                            .build())
                    .retrieve()
                    .body(Yes24Response.class);

            // HTTP 성공 여부와 별개로 YES24 업무 응답의 성공 상태를 확인한다.
            if (response == null || !Boolean.TRUE.equals(response.success())) {
                String message = response == null ? "응답 본문이 없습니다." : response.message();
                throw new Yes24ClientException("상품 상세 조회에 실패했습니다: " + message);
            }

            List<Yes24BookItem> items = response.data() == null ? null : response.data().items();
            if (items == null || items.isEmpty()) {
                throw new Yes24ClientException("ISBN13에 해당하는 상품이 없습니다: isbn13=" + isbn13);
            }

            // ISBN13은 단일 상품 조회 조건이므로 첫 번째 결과를 공통 Book으로 변환한다.
            return Yes24BookMapper.toBook(items.get(0));
        } catch (Yes24ClientException exception) {
            throw exception;
        } catch (Exception exception) {
            // 인증, 네트워크, 역직렬화 실패를 공급자 전용 예외로 통일한다.
            throw new Yes24ClientException("상품 상세 API 호출에 실패했습니다: isbn13=" + isbn13, exception);
        }
    }

    private void validateIsbn13(String isbn13) {
        // 문서에서 요구하는 13자리 숫자 형식을 호출 전에 검증해 불필요한 외부 요청을 막는다.
        if (isbn13 == null || !isbn13.matches("\\d{13}")) {
            throw new IllegalArgumentException("isbn13은 13자리 숫자여야 합니다.");
        }
    }

    @Override
    public List<Book> searchBooks(String query) {
        validateSearchQuery(query);

        try {
            Yes24Response response = yes24Api.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ITEM_LIST_PATH)
                            .queryParam("query", query.trim())
                            .queryParam("category", "BOOK")
                            .queryParam("sort", "DEFAULT")
                            .queryParam("page", 1)
                            .queryParam("pageSize", SEARCH_PAGE_SIZE)
                            .queryParam("detail", "Y")
                            .build())
                    .retrieve()
                    // YES24는 검색 결과가 없어도 404 본문에 SEARCH_001을 담으므로 응답을 역직렬화해 구분한다.
                    .onStatus(statusCode -> statusCode.value() == 404, (request, clientResponse) -> { })
                    .body(Yes24Response.class);

            if (response != null && NO_SEARCH_RESULT_ERROR_CODE.equals(response.errorCode())) {
                return List.of();
            }

            // HTTP 200이어도 업무 오류가 포함될 수 있으므로 success 값을 별도로 확인한다.
            if (response == null || !Boolean.TRUE.equals(response.success())) {
                String message = response == null ? "응답 본문이 없습니다." : response.message();
                throw new Yes24ClientException("상품 검색에 실패했습니다: " + message);
            }

            return Yes24BookMapper.toBooks(response.data() == null ? null : response.data().items());
        } catch (Yes24ClientException exception) {
            throw exception;
        } catch (Exception exception) {
            // 인증, 네트워크, 역직렬화 실패를 공급자 전용 예외로 통일한다.
            throw new Yes24ClientException("상품 검색 API 호출에 실패했습니다: query=" + query, exception);
        }
    }

    private void validateSearchQuery(String query) {
        // 공백 검색어는 YES24의 PARAM_003 오류를 유발하므로 외부 호출 전에 명확히 거부한다.
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query는 공백일 수 없습니다.");
        }
    }
}
