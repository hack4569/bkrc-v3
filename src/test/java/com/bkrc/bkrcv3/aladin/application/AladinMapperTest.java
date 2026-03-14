package com.bkrc.bkrcv3.aladin.application;

import com.bkrc.bkrcv3.aladin.application.response.AladinBookResponse;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.aladin.entity.BookComment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AladinMapper")
class AladinMapperTest {

    private final AladinMapper mapper = new AladinMapper();

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("AladinBook이 null이면 null 반환")
        void returnsNullWhenBookIsNull() {
            assertThat(mapper.toResponse(null)).isNull();
        }

        @Test
        @DisplayName("AladinBook을 AladinBookResponse로 변환한다")
        void mapsBookToResponse() {
            AladinBook book = new AladinBook();
            book.setItemId(12345);
            book.setTitle("테스트 책");
            book.setAuthor("저자");
            book.setIsbn13("9788966260000");
            book.setCategoryId(101);
            book.setCategoryName("소설");

            AladinBookResponse response = mapper.toResponse(book);

            assertThat(response).isNotNull();
            assertThat(response.getItemId()).isEqualTo(12345);
            assertThat(response.getTitle()).isEqualTo("테스트 책");
            assertThat(response.getAuthor()).isEqualTo("저자");
            assertThat(response.getIsbn13()).isEqualTo("9788966260000");
            assertThat(response.getCategoryId()).isEqualTo(101);
            assertThat(response.getCategoryName()).isEqualTo("소설");
        }

        @Test
        @DisplayName("bookCommentList가 있으면 응답에도 포함된다")
        void includesBookCommentList() {
            AladinBook book = new AladinBook();
            book.setItemId(1);
            book.setBookCommentList(List.of(
                    BookComment.create("코멘트1", "description"),
                    BookComment.create("코멘트2", "toc")
            ));

            AladinBookResponse response = mapper.toResponse(book);

            assertThat(response.getBookCommentList()).hasSize(2);
            assertThat(response.getBookCommentList().get(0).getType()).isEqualTo("description");
            assertThat(response.getBookCommentList().get(1).getType()).isEqualTo("toc");
        }
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("AladinBookResponse가 null이면 null 반환")
        void returnsNullWhenResponseIsNull() {
            assertThat(mapper.toEntity(null)).isNull();
        }

        @Test
        @DisplayName("AladinBookResponse를 AladinBook 엔티티로 변환한다")
        void mapsResponseToEntity() {
            AladinBookResponse response = new AladinBookResponse();
            response.setItemId(99999);
            response.setTitle("엔티티 변환 테스트");
            response.setAuthor("작가");
            response.setPubDate("2024-01-15");
            response.setCategoryId(200);

            AladinBook book = mapper.toEntity(response);

            assertThat(book).isNotNull();
            assertThat(book.getItemId()).isEqualTo(99999);
            assertThat(book.getTitle()).isEqualTo("엔티티 변환 테스트");
            assertThat(book.getAuthor()).isEqualTo("작가");
            assertThat(book.getPubDate()).isEqualTo("2024-01-15");
            assertThat(book.getCategoryId()).isEqualTo(200);
        }

        @Test
        @DisplayName("round-trip: entity -> response -> entity 시 값이 보존된다")
        void roundTrip_preservesData() {
            AladinBook original = new AladinBook();
            original.setItemId(111);
            original.setTitle("라운드트립");
            original.setIsbn13("9788900000000");

            AladinBookResponse response = mapper.toResponse(original);
            AladinBook restored = mapper.toEntity(response);

            assertThat(restored.getItemId()).isEqualTo(original.getItemId());
            assertThat(restored.getTitle()).isEqualTo(original.getTitle());
            assertThat(restored.getIsbn13()).isEqualTo(original.getIsbn13());
        }
    }
}
