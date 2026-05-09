package com.bkrc.bkrcv3.batch;

import com.bkrc.bkrcv3.aladin.application.AladinService;
import com.bkrc.bkrcv3.aladin.application.request.AladinRequest;
import com.bkrc.bkrcv3.aladin.client.AladinClient;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.aladin.entity.QueryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Slf4j
@RequiredArgsConstructor
@Component
public class AladinApiItemReader implements ItemReader<AladinBook>, ItemStream {
    private final AladinService aladinService;
    private int page;
    private int emptyCount;
    private static final int MAX_EMPTY = 20;
    private static final String PAGE_KEY = "aladin.reader.page";
    private static final String EMPTY_COUNT_KEY = "aladin.reader.emptyCount";

    private Queue<AladinBook> buffer = new LinkedList<>();
    private ExecutionContext executionContext;
    // ItemStream 구현 — Step 시작 시 호출
    @Override
    public void open(ExecutionContext executionContext) {
        this.executionContext = executionContext;
        // 재시작 시 저장된 page 복원, 최초 실행이면 1
        this.page = executionContext.getInt(PAGE_KEY, 1);
        this.emptyCount = executionContext.getInt(EMPTY_COUNT_KEY, 0);
    }


    @Override
    public void close() {}

    @Override
    public AladinBook read() throws Exception {
        while (buffer.isEmpty()) {
            if (emptyCount >= MAX_EMPTY) {
                return null;
            }
            List<AladinBook> aladinItemList = aladinService.getAladinItemList(
                    AladinRequest.builder()
                            .querytype(QueryType.BEST_SELLER.getQueryType())
                            .start(page).build()
            );
            page++;

            if (CollectionUtils.isEmpty(aladinItemList)) {
                emptyCount++;
                continue;
            }
            emptyCount = 0;
            buffer.addAll(aladinItemList);
        }
        executionContext.putInt(PAGE_KEY, page);
        return buffer.poll();
    }

}
