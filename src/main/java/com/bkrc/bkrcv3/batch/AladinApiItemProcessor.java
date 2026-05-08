package com.bkrc.bkrcv3.batch;

import com.bkrc.bkrcv3.aladin.application.AladinBookRepository;
import com.bkrc.bkrcv3.aladin.application.AladinService;
import com.bkrc.bkrcv3.aladin.application.CategoryService;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.aladin.entity.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class AladinApiItemProcessor implements ItemProcessor<AladinBook, AladinBook>, ItemStream {

    private final AladinService aladinService;
    private final CategoryService categoryService;
    private Set<Integer> allowedCategoryIds; // 캐싱
    // Step 시작 시 한 번만 조회
    @Override
    public void open(ExecutionContext executionContext) {
        this.allowedCategoryIds = categoryService.findAcceptedCategories().stream()
                .map(Category::getCid)
                .collect(Collectors.toSet());
    }

    @Override
    public AladinBook process(AladinBook item) {
        // 이미 DB에 있는 책이면 null → Chunk에서 skip됨
        boolean newBookFlag = ObjectUtils.isEmpty(aladinService.getAladinBook(item.getItemId()));
        if (!newBookFlag) return null;
        if (!item.isInAllowedCategories(this.allowedCategoryIds)) return null;
        // 3. 1년 이내 출판된 책이 아니면 skip
        if (!item.isPublishedAfter()) return null;

        // 알라딘 API로 상세 정보 보강
        AladinBook detail = aladinService.settingAladinDetail(item.getIsbn13());
        return detail;
    }
}