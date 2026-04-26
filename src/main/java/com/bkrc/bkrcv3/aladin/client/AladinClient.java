package com.bkrc.bkrcv3.aladin.client;

import com.bkrc.bkrcv3.aladin.application.request.AladinRequest;
import com.bkrc.bkrcv3.aladin.application.response.AladinResponse;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.aladin.entity.AladinClientException;
import com.bkrc.bkrcv3.aladin.entity.AladinConstants;
import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class AladinClient {
    private RestClient aladinApi;
    @Value("${aladin.host}")
    private String aladinHost;
    @Value("${aladin.ttbkey}")
    private String aladinTbKey;
    private AladinRequest aladinRequest;

    @PostConstruct
    void initRestClient() {
        aladinApi = RestClient.create(aladinHost);
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
            throw new AladinClientException(ErrorCode.ALADIN_CLIENT_ERROR);
        }
    }

    @Async("aladinTaskExecutor")
    public CompletableFuture<AladinBook> bookDetailAsync(String isbn13) {
        AladinBook book = this.bookDetail(AladinRequest.create(isbn13));
        return CompletableFuture.completedFuture(book);
    }

    //책 상세 조회
    public AladinBook bookDetail(AladinRequest aladinRequest) {
        var aladinBooks = this.getApi(AladinConstants.ITEM_LOOKUP, aladinRequest).getItem();
        if (aladinBooks.isEmpty()) throw new BusinessException("데이터가 없습니다.", HttpStatus.SERVICE_UNAVAILABLE);

        var aladinbook = aladinBooks.get(0);

        return aladinbook;
    }
}
