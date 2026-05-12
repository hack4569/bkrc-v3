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
public class AladinApiItemReader implements ItemReader<AladinBook> {
    private final AladinService aladinService;
    private int page;
    private Queue<AladinBook> buffer = new LinkedList<>();

    @Override
    public AladinBook read() throws Exception {
        while (buffer.isEmpty()) {
            List<AladinBook> aladinItemList = aladinService.getAladinItemList(
                    AladinRequest.builder()
                            .querytype(QueryType.BEST_SELLER.getQueryType())
                            .start(page).build()
            );

            if (CollectionUtils.isEmpty(aladinItemList)) return null;
            buffer.addAll(aladinItemList);
            page++;
        }

        return buffer.poll();
    }

}
