package bkrc.chaptersofu.adapter.tests.like.application.provided;

import bkrc.chaptersofu.application.like.provided.*;

import bkrc.chaptersofu.application.aladin.AladinBookRepository;
import bkrc.chaptersofu.domain.aladin.AladinBookResponse;
import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.aladin.AladinException;
import bkrc.chaptersofu.adapter.tests.api.CommonApiTest;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.like.Like;
import bkrc.chaptersofu.domain.like.LikeException;
import bkrc.chaptersofu.application.member.provided.MemberRegister;
import bkrc.chaptersofu.application.member.provided.MemberRegisterRequest;
import bkrc.chaptersofu.domain.member.Member;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@CommonApiTest
@Transactional
class LikeRegisterTest {
    private static final int BOOK_ID = 100_001;

    @Autowired
    private LikeRegister likeRegister;
    @Autowired
    private LikeFinder likeFinder;
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
                new MemberRegisterRequest("likeRegisterUser", "password", "password"));
        aladinBookRepository.save(createBook(BOOK_ID));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void like() {
        Like like = likeRegister.like(BOOK_ID, member.getMemberId());

        assertThat(like.getLikeId()).isNotNull();
        assertThat(like.getBook().getItemId()).isEqualTo(BOOK_ID);
        assertThat(like.getMember().getMemberId()).isEqualTo(member.getMemberId());
    }

    @Test
    void failLikeWhenAlreadyLiked() {
        likeRegister.like(BOOK_ID, member.getMemberId());
        entityManager.flush();

        LikeException exception = catchThrowableOfType(
                LikeException.class,
                () -> likeRegister.like(BOOK_ID, member.getMemberId())
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LIKE_ALREADY_EXISTS);
    }

    @Test
    void failLikeWhenBookDoesNotExist() {
        AladinException exception = catchThrowableOfType(
                AladinException.class,
                () -> likeRegister.like(999_999, member.getMemberId())
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BOOK_NOT_FOUND);
    }

    @Test
    void unLike() {
        likeRegister.like(BOOK_ID, member.getMemberId());
        entityManager.flush();

        likeRegister.unLike(BOOK_ID, member.getMemberId());
        entityManager.flush();
        entityManager.clear();

        assertThat(likeFinder.getMyLikes(member.getMemberId())).isEmpty();
    }

    @Test
    void failUnLikeWhenLikeDoesNotExist() {
        LikeException exception = catchThrowableOfType(
                LikeException.class,
                () -> likeRegister.unLike(BOOK_ID, member.getMemberId())
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LIKE_ALREADY_EXISTS);
    }

    private AladinBook createBook(Integer itemId) {
        AladinBookResponse response = new AladinBookResponse();
        response.setItemId(itemId);
        response.setTitle("좋아요 테스트 도서");
        response.setAuthor("테스트 저자");
        response.setPublisher("테스트 출판사");
        response.setLink("https://example.com/books/" + itemId);
        response.setCover("https://example.com/books/" + itemId + ".jpg");
        return AladinBook.toEntity(response);
    }
}
