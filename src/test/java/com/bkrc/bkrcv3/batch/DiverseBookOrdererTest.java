package com.bkrc.bkrcv3.batch;

import com.bkrc.bkrcv3.aladin.application.CategoryRepository;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.aladin.entity.Category;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DiverseBookOrdererTest {

    @Test
    void booksAreInterleavedByRepresentativeGenreWithoutLoss() {
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        when(categoryRepository.findAll()).thenReturn(List.of(
                category(10, "소설/시/희곡"),
                category(20, "경제경영"),
                category(30, "역사"),
                category(40, "에세이")
        ));
        DiverseBookOrderer orderer = new DiverseBookOrderer(
                new RecommendGenreResolver(), categoryRepository);

        AladinBook novel1 = book(1, 10);
        AladinBook novel2 = book(2, 10);
        AladinBook economy1 = book(3, 20);
        AladinBook history1 = book(4, 30);
        AladinBook essay1 = book(5, 40);

        List<AladinBook> result = orderer.order(List.of(novel1, novel2, economy1, history1, essay1));

        assertThat(result).containsExactly(novel1, economy1, history1, essay1, novel2);
    }

    private AladinBook book(int itemId, int categoryId) {
        AladinBook book = mock(AladinBook.class);
        when(book.getItemId()).thenReturn(itemId);
        when(book.getCategoryId()).thenReturn(categoryId);
        return book;
    }

    private Category category(int cid, String depth1) {
        return Category.builder().cid(cid).depth1(depth1).build();
    }
}
