package com.bkrc.bkrcv3.batch;

import com.bkrc.bkrcv3.aladin.application.CategoryRepository;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.aladin.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DiverseBookOrderer {

    // category.depth1을 서비스에서 사용하는 대표 장르로 변환한다.
    private final RecommendGenreResolver genreResolver;

    // aladin_book.category_id와 연결할 category.cid/depth1 정보를 조회한다.
    private final CategoryRepository categoryRepository;

    /**
     * 전체 도서를 대표 장르별로 한 권씩 번갈아 나오도록 재정렬한다.
     *
     * <p>예를 들어 입력이 {@code [소설1, 소설2, 경제1, 역사1]}이라면
     * 결과는 {@code [소설1, 경제1, 역사1, 소설2]}가 된다.</p>
     *
     * <p>책을 필터링하거나 복제하지 않으므로 입력받은 모든 책은 결과에 정확히 한 번 포함된다.</p>
     */
    public List<AladinBook> order(List<AladinBook> books) {
        // 빈 입력이면 category 테이블을 불필요하게 조회하지 않고 즉시 종료한다.
        if (CollectionUtils.isEmpty(books)) {
            return List.of();
        }

        /*
         * category 테이블을 한 번만 조회하여 아래 형태의 인메모리 맵을 만든다.
         *
         * key   : category.cid
         * value : category.depth1을 변환한 대표 장르
         *
         * 이후 각 책을 분류할 때 DB를 다시 조회하지 않으므로 N+1 쿼리가 발생하지 않는다.
         * 병합 함수 (first, ignored) -> first는 중복 cid가 들어오는 예외적인 상황에서
         * 먼저 조회된 장르를 유지하기 위한 방어 코드다.
         */
        Map<Integer, RecommendGenre> genreByCategoryId = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(
                        Category::getCid,
                        genreResolver::resolve,
                        (first, ignored) -> first
                ));

        /*
         * 대표 장르별 책 대기열을 만든다.
         *
         * EnumMap은 키가 enum으로 한정된 경우 일반 HashMap보다 구조가 단순하고 효율적이다.
         * Deque는 각 장르의 맨 앞 책을 removeFirst()로 O(1)에 꺼내기 위해 사용한다.
         */
        Map<RecommendGenre, Deque<AladinBook>> booksByGenre = new EnumMap<>(RecommendGenre.class);
        for (AladinBook book : books) {
            /*
             * aladin_book.category_id로 위에서 만든 cid 맵을 조회한다.
             * category_id가 null이거나 category 테이블에 대응하는 cid가 없으면 ETC로 분류하여
             * 해당 책이 추천 캐시에서 누락되지 않게 한다.
             */
            RecommendGenre genre = genreByCategoryId.getOrDefault(book.getCategoryId(), RecommendGenre.ETC);

            // 장르 대기열이 아직 없으면 생성하고, 기존 입력 순서를 유지하며 맨 뒤에 책을 넣는다.
            booksByGenre.computeIfAbsent(genre, ignored -> new ArrayDeque<>()).addLast(book);
        }

        // 입력과 같은 크기로 미리 할당하여 ArrayList가 확장되며 배열을 복사하는 횟수를 줄인다.
        List<AladinBook> ordered = new ArrayList<>(books.size());

        // 이번 한 바퀴에서 책이 한 권이라도 추가됐는지를 나타내는 반복 종료 조건이다.
        boolean bookAdded;
        do {
            // 매 라운드 시작 시 false로 초기화한다. 모든 장르가 비어 있으면 끝까지 false로 남는다.
            bookAdded = false;

            /*
             * enum에 선언된 대표 장르 순서대로 한 번씩 방문한다.
             * 각 장르에서 최대 한 권만 꺼내므로 특정 장르의 책이 앞부분에 몰리지 않는다.
             */
            for (RecommendGenre genre : RecommendGenre.values()) {
                Deque<AladinBook> genreBooks = booksByGenre.get(genre);

                // 해당 장르가 없거나 이미 모두 소진됐다면 다음 장르로 넘어간다.
                if (genreBooks != null && !genreBooks.isEmpty()) {
                    // 장르 대기열의 첫 책을 제거하면서 최종 결과의 맨 뒤에 추가한다.
                    ordered.add(genreBooks.removeFirst());

                    // 책이 추가됐으므로 아직 남은 책이 있는지 확인하기 위해 다음 라운드를 수행한다.
                    bookAdded = true;
                }
            }
            // 어느 장르에서도 책이 추가되지 않은 첫 라운드에서 반복을 종료한다.
        } while (bookAdded);

        // 장르 순서만 교차 배치되고, 원본의 모든 책이 한 번씩 포함된 목록을 반환한다.
        return ordered;
    }
}
