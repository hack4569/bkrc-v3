package bkrc.chaptersofu.application.aladin;

import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.aladin.AladinBookResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AladinMapperTest {
    private final AladinMapper mapper = new AladinMapper();

    @Test
    void null은_null로_변환한다() {
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    void 도서_엔티티를_응답으로_변환한다() {
        AladinBookResponse source = new AladinBookResponse();
        source.setItemId(10);
        source.setTitle("테스트 도서");
        AladinBook book = AladinBook.toEntity(source);

        AladinBookResponse response = mapper.toResponse(book);

        assertThat(response.getItemId()).isEqualTo(10);
        assertThat(response.getTitle()).isEqualTo("테스트 도서");
    }
}
