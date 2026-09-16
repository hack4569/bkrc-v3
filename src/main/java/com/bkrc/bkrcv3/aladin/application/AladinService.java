package com.bkrc.bkrcv3.aladin.application;

import com.bkrc.bkrcv3.aladin.application.request.AladinRequest;
import com.bkrc.bkrcv3.aladin.application.response.AladinBookPageResponse;
import com.bkrc.bkrcv3.aladin.application.response.AladinBookSearchResponse;
import com.bkrc.bkrcv3.aladin.application.response.AladinResponse;
import com.bkrc.bkrcv3.aladin.client.AladinClient;
import com.bkrc.bkrcv3.aladin.entity.*;
import com.bkrc.bkrcv3.aladin.infrastructure.cache.AladinBookCache;
import com.bkrc.bkrcv3.common.constants.RcmdConst;
import com.bkrc.bkrcv3.exception.AladinClientException;
import com.bkrc.bkrcv3.required.Ai;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AladinService {
    private final AladinClient aladinClient;
    private final Ai ai;
    private final AladinBookRepository aladinBookRepository;
    private final AladinMapper aladinMapper;
    private final AladinBookCache aladinBookCache;

    public List<AladinBook> getAladinItemList(AladinRequest aladinRequest) {
        return aladinClient.getApi(AladinConstants.ITEM_LIST, aladinRequest).getItem();
    }

    @CircuitBreaker(name = "aladinSearch", fallbackMethod = "searchBooksFallback")
    public List<AladinBookSearchResponse> searchBooks(String query) {
        AladinResponse response = aladinClient.searchBooks(query);
        if (response == null || CollectionUtils.isEmpty(response.getItem())) {
            return List.of();
        }

        return response.getItem().stream()
                .map(AladinBookSearchResponse::from)
                .toList();
    }

    public AladinBookPageResponse findAll() {
        return aladinBookCache.find()
                .orElseGet(() -> {
                    AladinBookPageResponse response = findAllFromDb();
                    aladinBookCache.save(response);
                    return response;
                });
    }

    private AladinBookPageResponse findAllFromDb() {
        var aladinBooks = aladinBookRepository.findAllWithBookComments();
        return AladinBookPageResponse.of(
                aladinBooks.stream()
                        .map(aladinMapper::toResponse)
                        .toList(),
                aladinBooks.size()
        );
    }

    public List<AladinBook> findAll(AladinBookPageResponse aladinBookPageResponse) {
        if (aladinBookPageResponse.getCount() == 0) return null;
        return aladinBookPageResponse.getAladinBookResponseList().stream()
                .map(AladinBook::toEntity)
                .toList();
    }

    public AladinBook getAladinBook(Integer itemId) {
        return aladinBookRepository.findById(itemId).orElse(null);
    }

    public AladinBook settingAladinDetail(String isbn13) {
        var aladinDetail = aladinClient.bookDetail(AladinRequest.create(isbn13));
        //코멘트 세팅
        this.filterContentsByAi(aladinDetail);
        return aladinDetail;
    }

    //ai 적용
    private void filterContentsByAi(AladinBook aladinDetail) {
        List<BookComment> bookCommentList = new ArrayList<>();

        String description = aladinDetail.getDescForRecommend();
        if (StringUtils.hasText(description)) {
            var summary = ai.summarizeDescriptions(description);
            if (StringUtils.hasText(summary.overview())) {
                bookCommentList.add(BookComment.create(summary.overview(), "description"));
            }
            if (StringUtils.hasText(summary.insight())) {
                bookCommentList.add(BookComment.create(summary.insight(), "descriptionInsight"));
            }
        }

        List<MdRecommend> mdRecommendList = aladinDetail.getSubInfo().getMdRecommendList();
        if (!ObjectUtils.isEmpty(mdRecommendList)) {
            for (MdRecommend mdRecommend : mdRecommendList) {
                bookCommentList.add(BookComment.create(ai.filteringContent(mdRecommend.getComment()), "mdRecommend"));
            }
        }

        List<String> recommendations = ai.getRecommend(aladinDetail.getTitle());
        if (!CollectionUtils.isEmpty(recommendations)) {
            bookCommentList.add(BookComment.create(String.join("<br>", recommendations), "aiRecommend"));
        }

        List<Phrase> phraseList = aladinDetail.getSubInfo().getPhraseList();
        if (!ObjectUtils.isEmpty(phraseList)) {
            for (int i = 1; i < phraseList.size(); i++) {
                String filteredPhrase = stripHtmlTags(phraseList.get(i).getPhrase());
                if (!StringUtils.hasText(filteredPhrase)) continue;

                String[] phrases = filteredPhrase.split("\\.");
                StringBuilder phraseContent = new StringBuilder();
                int phraseCount = Math.min(phrases.length, RcmdConst.paragraphSlide);
                for (int j = 0; j < phraseCount; j++) {
                    phraseContent.append(phrases[j]).append(". ");
                }
                bookCommentList.add(BookComment.create(phraseContent.toString(), "phrase"));
            }
        }

        String toc = aladinDetail.getSubInfo().getToc();
        if (StringUtils.hasText(toc)) {
            String filteredToc = toc.replaceAll("<(/)?([pP]*)(\\s[pP]*=[^>]*)?(\\s)*(/)?>", "");
            bookCommentList.add(BookComment.create(filteredToc, "toc"));
        }

        aladinDetail.settingBookCommentList(bookCommentList);
    }

    private String stripHtmlTags(String value) {
        if (!StringUtils.hasText(value)) return "";
        return value.replaceAll("<[^>]*>", "");
    }

    private List<AladinBook> showError(Throwable t) {
        log.warn("[알라딘] 요청 제한 또는 타임아웃 msg={}", t.getMessage(), t);
        return List.of();
    }
    private List<AladinBookSearchResponse> searchBooksFallback(String query, Throwable throwable) {
        log.error("[알라딘] 책 검색 Circuit Breaker fallback query={}", query, throwable);
        if (throwable instanceof AladinClientException aladinClientException) {
            throw aladinClientException;
        }
        throw new AladinClientException(throwable);
    }
    public void saveListForRedis(List<AladinBook> diverseBooks) {
        AladinBookPageResponse response = AladinBookPageResponse.of(
                diverseBooks.stream()
                        .map(aladinMapper::toResponse)
                        .toList(),
                diverseBooks.size()
        );

        aladinBookCache.save(response);
    }
}
