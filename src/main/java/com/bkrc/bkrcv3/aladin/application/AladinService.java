package com.bkrc.bkrcv3.aladin.application;

import com.bkrc.bkrcv3.aladin.application.request.AladinRecommendForUserRequest;
import com.bkrc.bkrcv3.aladin.application.request.AladinRecommendSaveRequest;
import com.bkrc.bkrcv3.aladin.application.request.AladinRequest;
import com.bkrc.bkrcv3.aladin.application.response.AladinBookPageResponse;
import com.bkrc.bkrcv3.aladin.application.response.AladinBookResponse;
import com.bkrc.bkrcv3.aladin.application.response.AladinResponse;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.aladin.entity.AladinConstants;
import com.bkrc.bkrcv3.aladin.entity.AladinException;
import com.bkrc.bkrcv3.aladin.entity.Category;
import com.bkrc.bkrcv3.common.constants.RcmdConst;
import com.bkrc.bkrcv3.history.application.HistoryService;
import com.bkrc.bkrcv3.history.entity.History;
import com.bkrc.bkrcv3.member.application.UserServiceImpl;
import com.bkrc.bkrcv3.member.application.response.RecommendView;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AladinService {

    private static final String CACHE_KEY_ALL_BOOKS = "aladin:books:all";
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private final UserServiceImpl userServiceImpl;

    private RestClient aladinApi;
    private final AladinBookRepository aladinBookRepository;
    private final BookCommentRepository bookCommentRepository;
    private final HistoryService historyService;
    private final CategoryService categoryService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AladinMapper aladinMapper;

    @Value("${aladin.host}")
    private String aladinHost;
    @Value("${aladin.ttbkey}")
    private String aladinTbKey;
    private AladinRequest aladinRequest;

    @PostConstruct
    void initRestClient() {
        aladinApi = RestClient.create(aladinHost);
    }

    @RateLimiter(name = "aladin", fallbackMethod = "getApiFallback")
    public List<AladinBook> getBooksForRecommend(AladinRequest aladinRequest, List<AladinBookResponse> registeredBooks) {
        this.aladinRequest = aladinRequest;

        Set<Integer> registeredBookItemIds;
        if (!CollectionUtils.isEmpty(registeredBooks)) {
            registeredBookItemIds = registeredBooks.stream().map(AladinBookResponse::getItemId).collect(Collectors.toSet());
        } else {
            registeredBookItemIds = new HashSet<>();
        }
        var aladinBooks = this.getApi(AladinConstants.ITEM_LIST, aladinRequest).getItem();
        if (ObjectUtils.isEmpty(aladinBooks)) throw new AladinException("상품조회시 데이터가 없습니다.");
        var newAladinBooks = aladinBooks.stream().filter(i -> !registeredBookItemIds.contains(i.getItemId())).toList();
        Set<Integer> allowedCategoryIds = categoryService.findAcceptedCategories().stream()
                .map(Category::getCid)
                .collect(Collectors.toCollection(HashSet::new));
        int anchorYyyyMMdd = anchorDateYyyyMMdd();
        List<AladinBook> filtered = newAladinBooks.stream()
                .filter(book -> book.isInAllowedCategories(allowedCategoryIds))
                .filter(book -> book.isPublishedAfter(anchorYyyyMMdd))
                .toList();
        return filtered;

    }

    public AladinBookPageResponse findAll() {
        try {
            String cached = redisTemplate.opsForValue().get(CACHE_KEY_ALL_BOOKS);
            var result = objectMapper.readValue(cached, AladinBookPageResponse.class);
            if (result.getCount() > 0) return result;
        } catch (Exception e) {
            log.warn("[알라딘] 캐시 조회 실패, DB에서 조회합니다. key={}", CACHE_KEY_ALL_BOOKS, e);
        }
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
                .map(aladinMapper::toEntity)
                .toList();
    }

    private AladinBookPageResponse findAllFromDb() {
        var aladinBooks = aladinBookRepository.findAllWithBookComments();
        return AladinBookPageResponse.of(
                aladinBooks.stream().map(aladinMapper::toResponse).toList(),
                aladinBooks.size());
    }

    private void evictAllBooksCache() {
        try {
            redisTemplate.delete(CACHE_KEY_ALL_BOOKS);
            log.debug("[알라딘] 전체 책 목록 캐시 삭제. key={}", CACHE_KEY_ALL_BOOKS);
        } catch (Exception e) {
            log.warn("[알라딘] 캐시 삭제 실패. key={}", CACHE_KEY_ALL_BOOKS, e);
        }
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
            throw new AladinException("파싱에러");
        }
    }

    // fallback: 제한 걸렸거나 대기 시간 초과 시 호출 (선택)
    private AladinResponse getApiFallback(String path, AladinRequest aladinRequest, Exception e) {
        log.warn("[알라딘] 요청 제한 또는 타임아웃 path={}", path, e);
        throw new AladinException("일시적으로 요청이 제한되었습니다.");
    }

    public List<AladinBook> saveNewAladinBooks(AladinRecommendSaveRequest request) {
        var aladinBooks = request.newAladinBooks();
        if (!CollectionUtils.isEmpty(aladinBooks)) {
            List<AladinBook> aladinDetailList = new ArrayList<>();
            aladinBooks.forEach( aladinBook -> {
                var aladinDetail = this.bookDetail(AladinRequest.create(aladinBook.getIsbn13()));
                aladinDetailList.add(aladinDetail);
            });
            List<AladinBook> saved = aladinBookRepository.saveAll(aladinDetailList);
            //evictAllBooksCache();
            return saved;
        }
        return List.of();
    }

    //책 상세 조회
    @RateLimiter(name = "aladin", fallbackMethod = "getApiFallback")
    public AladinBook bookDetail(AladinRequest aladinRequest) {
        var aladinBooks = this.getApi(AladinConstants.ITEM_LOOKUP, aladinRequest).getItem();
        if (aladinBooks.isEmpty()) throw new AladinException("상품조회시 데이터가 없습니다.");

        var aladinbook = aladinBooks.get(0);
        //코멘트 세팅
        aladinbook.settingBookCommentList();
        return aladinbook;
    }

    /** 기준일(1년 전) yyyyMMdd */
    private int anchorDateYyyyMMdd() {
        int result = Integer.parseInt(
                LocalDate.now()
                        .minusYears(1)
                        .format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        );
        return result;
    }

    public List<RecommendView> getRecommendBooksForUser(@Nullable String loginId, AladinRecommendForUserRequest request) {
        if (!StringUtils.isEmpty(loginId)) {
            request.setHistories(historyService.getHistoryByLoginId(loginId));
        }

        var aladinBooks = this.findAll();
        if (ObjectUtils.isEmpty(aladinBooks)) throw new AladinException("더이상 추천드릴 책이 없습니다.");

        var aladinDomainList = this.findAll(aladinBooks);
        var filteredBooks = this.filterForUser(aladinDomainList, request.getHistories());

        if (ObjectUtils.isEmpty(filteredBooks)) {
            historyService.deleteHistoryByLoginId(loginId);
            return this.getRecommendBooksForUser(loginId, request);
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
                .filter(this.historyFilter(historyList))
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

    private Predicate historyFilter(List<History> histories) {
        // 히스토리에 없는 책을 필터링하는 Predicate
        if (ObjectUtils.isEmpty(histories)) return book -> true;

        // 히스토리에 없는 책을 필터링하는 Predicate
        Predicate<AladinBook> historyFilter = book -> {

            return histories.stream().noneMatch(history ->
                    book.getItemId() == history.getItemId() &&
                            LocalDate.now().isEqual(history.getCreated().toLocalDate())
            );
        };
        return historyFilter;
    }
}
