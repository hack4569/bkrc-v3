package bkrc.chaptersofu.application.like.provided;

import bkrc.chaptersofu.application.like.provided.*;

import bkrc.chaptersofu.application.bkrc.BookRepository;
import bkrc.chaptersofu.adapter.client.aladin.mapper.AladinBookMapper;
import bkrc.chaptersofu.domain.aladin.AladinBookResponse;
import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.adapter.webapi.CommonApiTest;
import bkrc.chaptersofu.application.member.provided.MemberRegister;
import bkrc.chaptersofu.application.member.provided.MemberRegisterRequest;
import bkrc.chaptersofu.domain.member.Member;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@CommonApiTest
@Transactional
class LikeFinderTest {
    private static final int FIRST_ITEM_ID = 200_001;
    private static final int SECOND_ITEM_ID = 200_002;

    @Autowired
    private LikeFinder likeFinder;
    @Autowired
    private LikeRegister likeRegister;
    @Autowired
    private MemberRegister memberRegister;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private EntityManager entityManager;

    private Member member;
    private Long firstBookId;
    private Long secondBookId;

    @BeforeEach
    void setUp() {
        member = memberRegister.saveMember(
                new MemberRegisterRequest("likeFinderUser", "password", "password"));
        firstBookId = bookRepository.save(createBook(FIRST_ITEM_ID, "첫 번째 테스트 도서")).getBookId();
        secondBookId = bookRepository.save(createBook(SECOND_ITEM_ID, "두 번째 테스트 도서")).getBookId();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void 나의_좋아요_목록을_조회한다() {
        likeRegister.like(firstBookId, member.getMemberId());
        likeRegister.like(secondBookId, member.getMemberId());
        entityManager.flush();
        entityManager.clear();

        var likes = likeFinder.getMyLikes(member.getMemberId());

        assertThat(likes)
                .hasSize(2)
                .extracting(like -> like.getBook().getItemId())
                .containsExactlyInAnyOrder(FIRST_ITEM_ID, SECOND_ITEM_ID);
    }

    @Test
    void 회원의_좋아요가_없으면_빈_목록을_반환한다() {
        assertThat(likeFinder.getMyLikes(member.getMemberId())).isEmpty();
    }

    private bkrc.chaptersofu.domain.book.Book createBook(Integer itemId, String title) {
        AladinBookResponse response = new AladinBookResponse();
        response.setItemId(itemId);
        response.setTitle(title);
        response.setAuthor("테스트 저자");
        response.setPublisher("테스트 출판사");
        response.setLink("https://example.com/books/" + itemId);
        response.setCover("https://example.com/books/" + itemId + ".jpg");
        return AladinBookMapper.toBook(AladinBook.toEntity(response));
    }
}
