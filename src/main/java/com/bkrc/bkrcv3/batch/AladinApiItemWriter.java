package com.bkrc.bkrcv3.batch;

import com.bkrc.bkrcv3.aladin.application.AladinBookRepository;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class AladinApiItemWriter implements ItemWriter<AladinBook> {
    private final AladinBookRepository aladinBookRepository;

    @Override
    public void write(Chunk<? extends AladinBook> chunk) throws Exception {
        List<? extends AladinBook> items = chunk.getItems();
        aladinBookRepository.saveAll(items);
        log.info("[Batch] chunk 저장 완료 count={}", items.size());
    }
}

