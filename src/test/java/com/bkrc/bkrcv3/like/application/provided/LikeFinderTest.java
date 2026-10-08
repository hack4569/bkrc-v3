package com.bkrc.bkrcv3.like.application.provided;

import com.bkrc.bkrcv3.aladin.application.AladinBookRepository;
import com.bkrc.bkrcv3.aladin.application.response.AladinBookResponse;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.api.CommonApiTest;
import com.bkrc.bkrcv3.member.application.provided.MemberRegister;
import com.bkrc.bkrcv3.member.application.request.MemberRegisterRequest;
import com.bkrc.bkrcv3.member.domain.Member;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@CommonApiTest
@Transactional
class LikeFinderTest {
    private static final int FIRST_BOOK_ID = 200_001;
    private static final int SECOND_BOOK_ID = 200_002;

    @Autowired
    private LikeFinder likeFinder;
    @Autowired
    private LikeRegister likeRegister;
    @Autowired
    private MemberRegister memberRegister;
    @Autowired
    private AladinBookRepository aladinBookRepository;
    @Autowired
    private EntityManager entityManager;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRegister.saveMember(
                new MemberRegisterRequest("likeFinderUser", "password", "password"));
        aladinBookRepository.save(createBook(FIRST_BOOK_ID, "첫 번째 테스트 도서"));
        aladinBookRepository.save(createBook(SECOND_BOOK_ID, "두 번째 테스트 도서"));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findMyLikes() {
        likeRegister.like(FIRST_BOOK_ID, member.getMemberId());
        likeRegister.like(SECOND_BOOK_ID, member.getMemberId());
        entityManager.flush();
        entityManager.clear();

        var likes = likeFinder.getMyLikes(member.getMemberId());

        assertThat(likes)
                .hasSize(2)
                .extracting(like -> like.getBook().getItemId())
                .containsExactlyInAnyOrder(FIRST_BOOK_ID, SECOND_BOOK_ID);
    }

    @Test
    void findMyLikesReturnsEmptyListWhenMemberHasNoLikes() {
        assertThat(likeFinder.getMyLikes(member.getMemberId())).isEmpty();
    }

    private AladinBook createBook(Integer itemId, String title) {
        AladinBookResponse response = new AladinBookResponse();
        response.setItemId(itemId);
        response.setTitle(title);
        response.setAuthor("테스트 저자");
        response.setPublisher("테스트 출판사");
        response.setLink("https://example.com/books/" + itemId);
        response.setCover("https://example.com/books/" + itemId + ".jpg");
        return AladinBook.toEntity(response);
    }
}
