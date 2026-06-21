package com.bkrc.bkrcv3.batch;

import com.bkrc.bkrcv3.aladin.application.AladinService;
import com.bkrc.bkrcv3.aladin.application.request.AladinRequest;
import com.bkrc.bkrcv3.aladin.client.AladinClient;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.aladin.entity.AladinConstants;
import com.bkrc.bkrcv3.aladin.entity.QueryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class AladinApiItemReader implements ItemReader<AladinBook> {
    private final AladinService aladinService;
    private int page = 1;
    private Queue<AladinBook> buffer = new LinkedList<>();
    private Set<String> seenIsbnSet = new HashSet<>();

    @Override
    public AladinBook read() {
        while (buffer.isEmpty()) {
            List<AladinBook> aladinItemList = aladinService.getAladinItemList(
                    AladinRequest.builder()
                            .querytype(QueryType.BEST_SELLER.getQueryType())
                            .maxResults(AladinConstants.ITEM_LIST_PAGE)
                            .start(page).build()
            );

            if (CollectionUtils.isEmpty(aladinItemList)) return null;

            // 이번 페이지 isbn이 전부 이미 본 것이면 → 중복 페이지 → 종료
            boolean allDuplicated = aladinItemList.stream()
                    .map(AladinBook::getIsbn13)
                    .allMatch(seenIsbnSet::contains);

            if (allDuplicated) return null;

            // 새로운 isbn만 등록하고 buffer에 추가
            aladinItemList.stream()
                    .filter(book -> seenIsbnSet.add(book.getIsbn13())) // add()가 false면 중복
                    .forEach(buffer::add);

            page++;
        }

        return buffer.poll();
    }

}
