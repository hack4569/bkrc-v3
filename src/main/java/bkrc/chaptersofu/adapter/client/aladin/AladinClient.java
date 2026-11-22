package bkrc.chaptersofu.adapter.client.aladin;

import bkrc.chaptersofu.application.aladin.provided.AladinRequest;
import bkrc.chaptersofu.adapter.client.ExternalApiLoggingInterceptor;
import bkrc.chaptersofu.adapter.client.aladin.dto.AladinResponse;
import bkrc.chaptersofu.adapter.client.aladin.mapper.AladinBookMapper;
import bkrc.chaptersofu.application.required.BookInfo;
import bkrc.chaptersofu.domain.aladin.AladinConstants;
import bkrc.chaptersofu.domain.book.Book;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.aladin.AladinClientException;
import bkrc.chaptersofu.domain.aladin.AladinException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;


// 별도 공급자 설정이 없으면 기존 알라딘 연동을 기본 BookInfo 구현체로 사용한다.
@Component
@ConditionalOnProperty(name = "book-info.provider", havingValue = "aladin", matchIfMissing = true)
public class AladinClient implements BookInfo {
    private RestClient aladinApi;
    @Value("${aladin.host}")
    private String aladinHost;
    @Value("${aladin.ttbkey}")
    private String aladinTbKey;
    @Value("${external-api.aladin.connect-timeout:2s}")
    private java.time.Duration connectTimeout;
    @Value("${external-api.aladin.read-timeout:5s}")
    private java.time.Duration readTimeout;

    @PostConstruct
    void initRestClient() {
        var lenientMapper = JsonMapper.builder()
                .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
                .build();
        var converter = new MappingJackson2HttpMessageConverter(lenientMapper);
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        aladinApi = RestClient.builder()
                .baseUrl(aladinHost)
                .requestFactory(requestFactory)
                .requestInterceptor(new ExternalApiLoggingInterceptor("aladin"))
                .messageConverters(converters -> {
                    converters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
                    converters.add(converter);
                })
                .build();
    }

    private AladinResponse getApi(String path, AladinRequest aladinRequest) {
        ResponseEntity<AladinResponse> response = null;
        try{
            response = aladinApi
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("ttbkey", aladinTbKey)
                            .queryParams(aladinRequest.getApiParamMap())
                            .build()
                    )
                    .retrieve()
                    .toEntity(AladinResponse.class);
            return response.getBody();
        } catch (Exception e) {
            throw new AladinClientException(e);
        }
    }

    //책 상세 조회
    @Override
    public List<Book> getBestSellerBooks(int maxResults, int start) {
        AladinRequest request = AladinRequest.builder()
                .querytype("BestSeller")
                .maxResults(maxResults)
                .start(start)
                .build();
        AladinResponse response = getApi(AladinConstants.ITEM_LIST, request);
        // 알라딘 전용 응답 모델이 어댑터 밖으로 노출되지 않도록 공통 도서 타입으로 변환한다.
        return AladinBookMapper.toBooks(response == null ? null : response.getItem());
    }

    @Override
    public Book getBookDetail(String isbn13) {
        var aladinBooks = this.getApi(AladinConstants.ITEM_LOOKUP, AladinRequest.create(isbn13)).getItem();
        if (aladinBooks.isEmpty()) {
            throw new AladinException(ErrorCode.ALADIN_NOT_READY, "isbn13=" + isbn13);
        }

        var aladinBook = aladinBooks.get(0);

        // 상세 조회 결과도 목록 조회와 동일한 매핑 규칙을 적용한다.
        return AladinBookMapper.toBook(aladinBook);
    }

    @Override
    public List<Book> searchBooks(String query) {
        AladinRequest request = AladinRequest.builder()
                .query(query.trim())
                .querytype("Keyword")
                .searchTarget("Book")
                .maxResults(10)
                .start(1)
                .cover("MidBig")
                .build();

        AladinResponse response = this.getApi(AladinConstants.ITEM_SEARCH, request);
        // 검색 결과에도 상세 및 베스트셀러 조회와 동일한 공통 도서 변환 규칙을 적용한다.
        return AladinBookMapper.toBooks(response == null ? null : response.getItem());
    }
}
