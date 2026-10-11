package bkrc.chaptersofu.application.aladin;

import bkrc.chaptersofu.application.aladin.provided.AladinRequest;
import bkrc.chaptersofu.application.required.BookInfo;
import bkrc.chaptersofu.domain.aladin.*;
import bkrc.chaptersofu.application.aladin.cache.AladinBookCache;
import bkrc.chaptersofu.application.aladin.provided.AladinFinder;
import bkrc.chaptersofu.domain.common.constants.RcmdConst;
import bkrc.chaptersofu.domain.exception.AladinClientException;
import bkrc.chaptersofu.application.required.Ai;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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
public class AladinService implements AladinFinder {
    private final BookInfo bookInfo;
    private final Ai ai;
    private final AladinBookRepository aladinBookRepository;
    private final AladinBookCache aladinBookCache;

    @Retry(name = "aladin")
    @CircuitBreaker(name = "aladin", fallbackMethod = "getAladinItemListFallback")
    @Override
    public List<AladinBook> getAladinItemList(AladinRequest aladinRequest) {
        return bookInfo.getBooks(
                aladinRequest.getQuerytype(),
                aladinRequest.getMaxResults(),
                aladinRequest.getStart()
        );
    }

    @Retry(name = "aladin")
    @CircuitBreaker(name = "aladin", fallbackMethod = "searchBooksFallback")
    @Override
    public List<AladinBook> searchBooks(String query) {
        List<AladinBook> books = bookInfo.searchBooks(query);
        if (CollectionUtils.isEmpty(books)) {
            return List.of();
        }

        return books;
    }

    @Override
    public List<AladinBook> findAll() {
        return aladinBookCache.find()
                .orElseGet(() -> {
                    List<AladinBook> books = findAllFromDb();
                    aladinBookCache.save(books);
                    return books;
                });
    }

    private List<AladinBook> findAllFromDb() {
        return aladinBookRepository.findAllWithBookComments();
    }

    @Override
    public AladinBook getAladinBook(Integer itemId) {
        return aladinBookRepository.findById(itemId).orElse(null);
    }

    @Retry(name = "aladin")
    @CircuitBreaker(name = "aladin", fallbackMethod = "settingAladinDetailFallback")
    @Override
    public AladinBook settingAladinDetail(String isbn13) {
        var aladinDetail = bookInfo.getBookDetail(isbn13);
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

    private List<AladinBook> getAladinItemListFallback(AladinRequest request, Throwable throwable) {
        log.warn("[알라딘] 목록 조회 fallback", throwable);
        return List.of();
    }

    private List<AladinBook> searchBooksFallback(String query, Throwable throwable) {
        log.warn("[알라딘] 책 검색 fallback query={}", query, throwable);
        return List.of();
    }

    private AladinBook settingAladinDetailFallback(String isbn13, Throwable throwable) {
        log.error("[알라딘] 상세 조회 fallback isbn13={}", isbn13, throwable);
        if (throwable instanceof AladinClientException aladinClientException) {
            throw aladinClientException;
        }
        throw new AladinClientException(throwable);
    }
    @Override
    public void saveListForRedis(List<AladinBook> diverseBooks) {
        aladinBookCache.save(diverseBooks);
    }
}
