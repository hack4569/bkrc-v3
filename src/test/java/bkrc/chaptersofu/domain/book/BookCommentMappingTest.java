package bkrc.chaptersofu.domain.book;

import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.aladin.BookComment;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookCommentMappingTest {

    @Test
    void 도서와_코멘트의_JPA_연관관계가_정상적으로_구성된다() {
        var registry = new StandardServiceRegistryBuilder()
                // 실제 DB를 변경하지 않고 매핑만 검증하기 위해 메모리 H2 데이터베이스를 사용한다.
                .applySetting("jakarta.persistence.jdbc.driver", "org.h2.Driver")
                .applySetting("jakarta.persistence.jdbc.url", "jdbc:h2:mem:book-comment-mapping;DB_CLOSE_DELAY=-1")
                .applySetting("hibernate.hbm2ddl.auto", "create-drop")
                .build();

        try {
            // mappedBy 양쪽 엔티티를 함께 등록해 Hibernate가 연관관계 메타데이터를 검증하도록 한다.
            var metadata = new MetadataSources(registry)
                    .addAnnotatedClass(Book.class)
                    .addAnnotatedClass(AladinBook.class)
                    .addAnnotatedClass(BookComment.class)
                    .buildMetadata();

            // SessionFactory 생성 단계에서 mappedBy와 외래키 매핑의 유효성이 최종 검증된다.
            try (var sessionFactory = metadata.buildSessionFactory()) {
                assertThat(sessionFactory.isOpen()).isTrue();
            }
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Test
    void 코멘트를_설정하면_연관관계의_주인에도_도서가_설정된다() {
        Book book = new Book();
        BookComment comment = BookComment.create("추천 내용", "description");

        // 코멘트 저장 시 item_id가 누락되지 않도록 양방향 관계 설정 결과를 확인한다.
        book.settingBookCommentList(List.of(comment));

        assertThat(comment.getBook()).isSameAs(book);
        assertThat(book.getBookCommentList()).containsExactly(comment);
    }
}
