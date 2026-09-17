package com.bkrc.bkrcv3.aladin.client;

import com.bkrc.bkrcv3.aladin.application.request.AladinRequest;
import com.bkrc.bkrcv3.aladin.application.response.AladinResponse;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.aladin.entity.AladinConstants;
import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.exception.AladinClientException;
import com.bkrc.bkrcv3.exception.BusinessException;
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


@Component
@Slf4j
public class AladinClient {
    private RestClient aladinApi;
    private JsonMapper lenientMapper;
    @Value("${aladin.host}")
    private String aladinHost;
    @Value("${aladin.ttbkey}")
    private String aladinTbKey;
    @Value("${external-api.aladin.connect-timeout:2s}")
    private java.time.Duration connectTimeout;
    @Value("${external-api.aladin.read-timeout:5s}")
    private java.time.Duration readTimeout;
    private AladinRequest aladinRequest;

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

    public AladinResponse getApi(String path, AladinRequest aladinRequest) {
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
    public AladinBook bookDetail(AladinRequest aladinRequest) {
        var aladinBooks = this.getApi(AladinConstants.ITEM_LOOKUP, aladinRequest).getItem();
        if (aladinBooks.isEmpty()) throw new BusinessException(ErrorCode.ALADIN_NOT_READY);

        var aladinbook = aladinBooks.get(0);

        return aladinbook;
    }

    public AladinResponse searchBooks(String query) {
        AladinRequest request = AladinRequest.builder()
                .query(query.trim())
                .querytype("Keyword")
                .searchTarget("Book")
                .maxResults(10)
                .start(1)
                .cover("MidBig")
                .build();

        return this.getApi(AladinConstants.ITEM_SEARCH, request);
    }
}
