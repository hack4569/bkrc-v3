package bkrc.chaptersofu.adapter.tests.aladin.application.provided;

import bkrc.chaptersofu.application.aladin.provided.*;

import bkrc.chaptersofu.application.aladin.AladinBookRepository;
import bkrc.chaptersofu.domain.aladin.AladinBookResponse;
import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.adapter.tests.api.CommonApiTest;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@CommonApiTest
@Transactional
class AladinFinderTest {
    @Autowired AladinFinder aladinFinder;
    @Autowired AladinBookRepository aladinBookRepository;
    @Autowired EntityManager entityManager;

    @Test
    void findAladinBook() {
        aladinBookRepository.save(createBook(4001));
        entityManager.flush();
        entityManager.clear();

        AladinBook found = aladinFinder.getAladinBook(4001);

        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("알라딘 조회 테스트 도서");
    }

    @Test
    void returnNullWhenAladinBookDoesNotExist() {
        assertThat(aladinFinder.getAladinBook(9999)).isNull();
    }

    private AladinBook createBook(Integer itemId) {
        AladinBookResponse response = new AladinBookResponse();
        response.setItemId(itemId);
        response.setTitle("알라딘 조회 테스트 도서");
        response.setAuthor("테스트 저자");
        response.setPublisher("테스트 출판사");
        return AladinBook.toEntity(response);
    }
}
