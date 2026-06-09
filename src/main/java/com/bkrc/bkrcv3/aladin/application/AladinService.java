package com.bkrc.bkrcv3.aladin.application;

import com.bkrc.bkrcv3.aladin.application.request.AladinRecommendForUserRequest;
import com.bkrc.bkrcv3.aladin.application.request.AladinRecommendSaveRequest;
import com.bkrc.bkrcv3.aladin.application.request.AladinRequest;
import com.bkrc.bkrcv3.aladin.application.response.AladinBookPageResponse;
import com.bkrc.bkrcv3.aladin.application.response.AladinBookResponse;
import com.bkrc.bkrcv3.aladin.client.AladinClient;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.aladin.entity.AladinConstants;
import com.bkrc.bkrcv3.aladin.entity.Category;
import com.bkrc.bkrcv3.common.constants.RcmdConst;
import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.exception.BusinessException;
import com.bkrc.bkrcv3.history.application.HistoryService;
import com.bkrc.bkrcv3.history.entity.History;
import com.bkrc.bkrcv3.member.application.response.RecommendView;
import com.bkrc.bkrcv3.required.Ai;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.micrometer.core.instrument.Counter;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AladinService {

    private static final String CACHE_KEY_ALL_BOOKS = "aladin:books:all";
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private final AladinClient aladinClient;
    private final Ai ai;
    private final AladinBookRepository aladinBookRepository;
    private final HistoryService historyService;
    private final CategoryService categoryService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AladinMapper aladinMapper;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;


    @RateLimiter(name = "aladin", fallbackMethod = "getApiFallback")
    public List<AladinBook> getBooksForRecommend(AladinRequest aladinRequest, List<AladinBookResponse> registeredBooks)     {

        Set<Integer> registeredBookItemIds;
        if (!CollectionUtils.isEmpty(registeredBooks)) {
            registeredBookItemIds = registeredBooks.stream().map(AladinBookResponse::getItemId).collect(Collectors.toSet());
        } else {
            registeredBookItemIds = new HashSet<>();
        }
        var aladinBooks = aladinClient.getApi(AladinConstants.ITEM_LIST, aladinRequest).getItem();
        if (ObjectUtils.isEmpty(aladinBooks)) return List.of();
        var newAladinBooks = aladinBooks.stream().filter(i -> !registeredBookItemIds.contains(i.getItemId())).toList();
        Set<Integer> allowedCategoryIds = categoryService.findAcceptedCategories().stream()
                .map(Category::getCid)
                .collect(Collectors.toCollection(HashSet::new));
        List<AladinBook> filtered = newAladinBooks.stream()
                .filter(book -> book.isInAllowedCategories(allowedCategoryIds))
                .filter(book -> book.publishDateFilter())
                .toList();
        return filtered;

    }

    public List<AladinBook> getAladinItemList(AladinRequest aladinRequest) {
        return aladinClient.getApi(AladinConstants.ITEM_LIST, aladinRequest).getItem();
    }

    public AladinBookPageResponse findAll() {
        try {
            String cached = redisTemplate.opsForValue().get(CACHE_KEY_ALL_BOOKS);
            var result = objectMapper.readValue(cached, AladinBookPageResponse.class);
            cacheHitCounter.increment();
            if (result.getCount() > 0) return result;
        } catch (Exception e) {
            log.warn("[알라딘] 캐시 조회 실패, DB에서 조회합니다. key={}", CACHE_KEY_ALL_BOOKS, e);
        }

        cacheMissCounter.increment();
        var response = findAllFromDb();

        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(CACHE_KEY_ALL_BOOKS, json, CACHE_TTL);
        } catch (Exception e) {
            log.warn("[알라딘] 캐시 저장 실패. key={}", CACHE_KEY_ALL_BOOKS, e);
        }
        return response;
    }

    public List<AladinBook> findAll(AladinBookPageResponse aladinBookPageResponse) {
        if (aladinBookPageResponse.getCount() == 0) return null;
        return aladinBookPageResponse.getAladinBookResponseList().stream()
                .map(AladinBook::toEntity)
                .toList();
    }

    public AladinBookPageResponse findAllFromDb() {
        var aladinBooks = aladinBookRepository.findAllWithBookComments();
        return AladinBookPageResponse.of(
                aladinBooks.stream().map(aladinMapper::toResponse).toList(),
                aladinBooks.size());
    }

    @RateLimiter(name = "aladin", fallbackMethod = "getApiFallback")
    public List<AladinBook> saveNewAladinBooks(AladinRecommendSaveRequest request) {
        var aladinBooks = request.newAladinBooks();
        List<AladinBook> aladinDetailList = new ArrayList<>();
        if (CollectionUtils.isEmpty(aladinBooks)) return List.of();
            //순차처리
            aladinBooks.forEach( aladinBook -> {
                aladinDetailList.add(settingAladinDetail(aladinBook.getIsbn13()));
            });
//            List<CompletableFuture<AladinBook>> futures = aladinBooks.stream().map(book -> aladinClient.bookDetailAsync(book.getIsbn13())).toList();
//            List<AladinBook> aladinDetailList = futures.stream()
//                    //.map(CompletableFuture::join)
//                    .map(future -> {
//                        try {
//                            return future.join();
//                        } catch (Exception e) {
//                            log.warn("책 상세 조회 실패 msg = {}", e.getMessage());
//                            return null; // 실패한 건은 null로 처리
//                        }
//                    })
//                    .filter(Objects::nonNull).toList();
            List<AladinBook> saved = aladinBookRepository.saveAll(aladinDetailList);
            return saved;
    }

    public AladinBook settingAladinDetail(String isbn13) {
        var aladinDetail = aladinClient.bookDetail(AladinRequest.create(isbn13));
        //코멘트 세팅
        aladinDetail.settingBookCommentList(ai);
        return aladinDetail;
    }

    public AladinBook getAladinBook(Integer itemId) {
        return aladinBookRepository.findById(itemId).orElse(null);
    }

    public List<RecommendView> getRecommendBooksForUser(@Nullable Long memberId, AladinRecommendForUserRequest request) {
        if (memberId != null) {
            request.setHistories(historyService.getHistoryByMemberId(memberId));
        }

        var aladinBooks = this.findAll();
        if (ObjectUtils.isEmpty(aladinBooks)) throw new BusinessException(ErrorCode.ALADIN_NOT_FOUND);

        var aladinBookList = this.findAll(aladinBooks);
        var filteredBooks = this.filterForUser(aladinBookList, request.getHistories());

        if (ObjectUtils.isEmpty(filteredBooks)) {
            historyService.deleteHistoryByMemberId(memberId);
            return this.getRecommendBooksForUser(memberId, request);
        }
        return this.showUserData(filteredBooks);
    }

    private List<RecommendView> showUserData(List<AladinBook> aladinBooks) {

        List<RecommendView> slideRecommendList = new ArrayList<>();
        for (int i = 0; i < RcmdConst.SHOW_BOOKS_COUNT; i++) {
            var book = aladinBooks.get(i);
            RecommendView recommendDto = RecommendView.builder()
                    .itemId(book.getItemId())
                    .title(book.getTitle())
                    .link(book.getLink())
                    .cover(book.getCover())
                    .recommendCommentList(book.getBookCommentList())
                    .author(book.getAuthor())
                    .categoryName(book.getCategoryName())
                    .build();
            slideRecommendList.add(recommendDto);
        }
        return slideRecommendList;

    }

    public List<AladinBook> filterForUser(List<AladinBook> aladinBooks, List<History> historyList) {
        if (ObjectUtils.isEmpty(aladinBooks)) return null;
        aladinBooks = aladinBooks.stream()
                .filter(AladinBook.historyFilter(historyList))
                .toList();
        return aladinBooks;
    }

    public void saveListForRedis(List<AladinBook> successList) {
        try {
            var response = AladinBookPageResponse.of(successList.stream().map(aladinMapper::toResponse).toList(), successList.size());
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(CACHE_KEY_ALL_BOOKS, json, CACHE_TTL);
        } catch (Exception e) {
            log.warn("[알라딘] 캐시 저장 실패. key={}", CACHE_KEY_ALL_BOOKS, e);
        }
    }

    // fallback: 제한 걸렸거나 대기 시간 초과 시 호출 (선택)
    private List<AladinBook> getApiFallback(AladinRequest aladinRequest, List<AladinBookResponse> registeredBooks, Throwable t) {
        return showError(t);
    }

    private List<AladinBook> getApiFallback(AladinRecommendSaveRequest request, Throwable t) {
        return showError(t);
    }

    private List<AladinBook> showError(Throwable t) {
        log.warn("[알라딘] 요청 제한 또는 타임아웃 msg={}", t.getMessage(), t);
        return List.of();
    }

    public List<AladinBook> getAladinBooksByItemIds(Collection<Integer> itemIds) {
        if (CollectionUtils.isEmpty(itemIds)) return List.of();
        return aladinBookRepository.findAllByItemIdIn(itemIds);
    }
}
