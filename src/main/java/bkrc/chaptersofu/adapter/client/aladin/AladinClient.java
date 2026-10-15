package bkrc.chaptersofu.adapter.client.aladin;

import bkrc.chaptersofu.application.aladin.provided.AladinRequest;
import bkrc.chaptersofu.adapter.client.aladin.dto.AladinResponse;
import bkrc.chaptersofu.application.required.BookInfo;
import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.aladin.AladinConstants;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.aladin.AladinClientException;
import bkrc.chaptersofu.domain.aladin.AladinException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;


@Component
@Slf4j
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
            log.error("[알라딘] 에러 메세지 파싱 에러 errorMessage={}", e.getMessage(), e);
            throw new AladinClientException(e);
        }
    }

    //책 상세 조회
    @Override
    public List<AladinBook> getBooks(String queryType, int maxResults, int start) {
        AladinRequest request = AladinRequest.builder()
                .querytype(queryType)
                .maxResults(maxResults)
                .start(start)
                .build();
        AladinResponse response = getApi(AladinConstants.ITEM_LIST, request);
        return response == null || response.getItem() == null ? List.of() : response.getItem();
    }

    @Override
    public AladinBook getBookDetail(String isbn13) {
        var aladinBooks = this.getApi(AladinConstants.ITEM_LOOKUP, AladinRequest.create(isbn13)).getItem();
        if (aladinBooks.isEmpty()) throw new AladinException(ErrorCode.ALADIN_NOT_READY);

        var aladinbook = aladinBooks.get(0);

        return aladinbook;
    }

    @Override
    public List<AladinBook> searchBooks(String query) {
        AladinRequest request = AladinRequest.builder()
                .query(query.trim())
                .querytype("Keyword")
                .searchTarget("Book")
                .maxResults(10)
                .start(1)
                .cover("MidBig")
                .build();

        AladinResponse response = this.getApi(AladinConstants.ITEM_SEARCH, request);
        return response == null || response.getItem() == null ? List.of() : response.getItem();
    }
}
