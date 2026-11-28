package bkrc.chaptersofu.application.like.provided;

import bkrc.chaptersofu.application.like.provided.*;

import bkrc.chaptersofu.application.bkrc.BookRepository;
import bkrc.chaptersofu.adapter.client.aladin.mapper.AladinBookMapper;
import bkrc.chaptersofu.domain.aladin.AladinBookResponse;
import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.book.BookException;
import bkrc.chaptersofu.adapter.webapi.CommonApiTest;
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
    private static final int EXTERNAL_ITEM_ID = 100_001;

    @Autowired
    private LikeRegister likeRegister;
    @Autowired
    private LikeFinder likeFinder;
    @Autowired
    private MemberRegister memberRegister;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private EntityManager entityManager;

    private Member member;
    private Long bookId;

    @BeforeEach
    void setUp() {
        member = memberRegister.saveMember(
                new MemberRegisterRequest("likeRegisterUser", "password", "password"));
        bookId = bookRepository.save(createBook(EXTERNAL_ITEM_ID)).getBookId();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void 도서에_좋아요한다() {
        Like like = likeRegister.like(bookId, member.getMemberId());

        assertThat(like.getLikeId()).isNotNull();
        assertThat(like.getBook().getItemId()).isEqualTo(EXTERNAL_ITEM_ID);
        assertThat(like.getMember().getMemberId()).isEqualTo(member.getMemberId());
    }

    @Test
    void 이미_좋아요한_도서는_좋아요에_실패한다() {
        likeRegister.like(bookId, member.getMemberId());
        entityManager.flush();

        LikeException exception = catchThrowableOfType(
                LikeException.class,
                () -> likeRegister.like(bookId, member.getMemberId())
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LIKE_ALREADY_EXISTS);
    }

    @Test
    void 존재하지_않는_도서_좋아요는_실패한다() {
        BookException exception = catchThrowableOfType(
                BookException.class,
                () -> likeRegister.like(999_999L, member.getMemberId())
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BOOK_NOT_FOUND);
    }

    @Test
    void 좋아요를_취소한다() {
        likeRegister.like(bookId, member.getMemberId());
        entityManager.flush();

        likeRegister.unLike(bookId, member.getMemberId());
        entityManager.flush();
        entityManager.clear();

        assertThat(likeFinder.getMyLikes(member.getMemberId())).isEmpty();
    }

    @Test
    void 존재하지_않는_좋아요_취소는_실패한다() {
        LikeException exception = catchThrowableOfType(
                LikeException.class,
                () -> likeRegister.unLike(bookId, member.getMemberId())
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LIKE_ALREADY_EXISTS);
    }

    private bkrc.chaptersofu.domain.book.Book createBook(Integer itemId) {
        AladinBookResponse response = new AladinBookResponse();
        response.setItemId(itemId);
        response.setTitle("좋아요 테스트 도서");
        response.setAuthor("테스트 저자");
        response.setPublisher("테스트 출판사");
        response.setLink("https://example.com/books/" + itemId);
        response.setCover("https://example.com/books/" + itemId + ".jpg");
        return AladinBookMapper.toBook(AladinBook.toEntity(response));
    }
}
