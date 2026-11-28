package bkrc.chaptersofu.adapter.client.aladin.mapper;

import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.aladin.AladinBookResponse;
import bkrc.chaptersofu.domain.aladin.BookComment;
import bkrc.chaptersofu.domain.aladin.SubInfo;
import bkrc.chaptersofu.domain.book.BookMall;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AladinBookMapperTest {

    @Test
    void 알라딘_도서를_공통_도서로_변환한다() {
        // 변환 과정에서 값과 객체 참조가 빠짐없이 전달되는지 확인할 대표 데이터를 구성한다.
        SubInfo subInfo = new SubInfo();
        List<BookComment> comments = List.of(BookComment.create("추천 내용", "mdRecommend"));
        AladinBookResponse response = new AladinBookResponse();
        response.setItemId(123);
        response.setTitle("테스트 도서");
        response.setLink("https://example.com/books/123");
        response.setAuthor("테스트 저자");
        response.setPubDate("2026-11-20");
        response.setDescription("도서 소개");
        response.setIsbn("1234567890");
        response.setIsbn13("9781234567890");
        response.setMallType("BOOK");
        response.setCover("https://example.com/cover.jpg");
        response.setCategoryId(2105);
        response.setCategoryName("국내도서>컴퓨터");
        response.setPublisher("테스트 출판사");
        response.setAdult(false);
        response.setCustomerReviewRank(9);
        response.setBestRank(1);
        response.setSubInfo(subInfo);
        response.setFullDescription("전체 소개");
        response.setFullDescription2("전체 소개 2");
        response.setToc("목차");
        response.setBookCommentList(comments);
        AladinBook source = AladinBook.toEntity(response);

        var result = AladinBookMapper.toBook(source);

        assertThat(result.getItemId()).isEqualTo(source.getItemId());
        assertThat(result.getTitle()).isEqualTo(source.getTitle());
        assertThat(result.getLink()).isEqualTo(source.getLink());
        assertThat(result.getAuthor()).isEqualTo(source.getAuthor());
        assertThat(result.getPubDate()).isEqualTo(source.getPubDate());
        assertThat(result.getDescription()).isEqualTo(source.getDescription());
        assertThat(result.getIsbn()).isEqualTo(source.getIsbn());
        assertThat(result.getIsbn13()).isEqualTo(source.getIsbn13());
        assertThat(result.getMallName()).isEqualTo(BookMall.ALADIN);
        assertThat(result.getCover()).isEqualTo(source.getCover());
        assertThat(result.getCategoryId()).isEqualTo(source.getCategoryId());
        assertThat(result.getCategoryName()).isEqualTo(source.getCategoryName());
        assertThat(result.getPublisher()).isEqualTo(source.getPublisher());
        assertThat(result.getAdult()).isEqualTo(source.getAdult());
        assertThat(result.getCustomerReviewRank()).isEqualTo(source.getCustomerReviewRank());
        assertThat(result.getBestRank()).isEqualTo(source.getBestRank());
        assertThat(result.getSubInfo()).isSameAs(source.getSubInfo());
        assertThat(result.getFullDescription()).isEqualTo(source.getFullDescription());
        assertThat(result.getFullDescription2()).isEqualTo(source.getFullDescription2());
        assertThat(result.getToc()).isEqualTo(source.getToc());
        assertThat(result.getBookCommentList()).isSameAs(source.getBookCommentList());
    }

    @Test
    void null_목록은_빈_목록으로_변환한다() {
        // 외부 응답의 item 누락을 정상적인 빈 결과로 취급하는 계약을 검증한다.
        assertThat(AladinBookMapper.toBooks(null)).isEmpty();
    }
}
