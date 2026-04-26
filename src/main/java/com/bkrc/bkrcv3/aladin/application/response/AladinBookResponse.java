package com.bkrc.bkrcv3.aladin.application.response;

import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.aladin.entity.BookComment;
import com.bkrc.bkrcv3.aladin.entity.SubInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Schema(description = "알라딘 도서 응답 DTO")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AladinBookResponse {

    @Schema(description = "도서 고유 ID (알라딘 itemId)", example = "123456789")
    private Integer itemId;
    @Schema(description = "도서 제목", example = "Clean Code")
    private String title;
    @Schema(description = "도서 상세 페이지 링크")
    private String link;
    @Schema(description = "저자", example = "로버트 C. 마틴")
    private String author;
    @Schema(description = "출판일 (yyyy-MM-dd)", example = "2013-12-24")
    private String pubDate;
    @Schema(description = "도서 소개")
    private String description;
    @Schema(description = "ISBN-10", example = "8966260959")
    private String isbn;
    @Schema(description = "ISBN-13", example = "9788966260959")
    private String isbn13;
    @Schema(description = "판매가", example = "26100")
    private Integer priceSales;
    @Schema(description = "정가", example = "29000")
    private Integer priceStandard;
    @Schema(description = "판매 유형", example = "BOOK")
    private String mallType;
    @Schema(description = "재고 상태")
    private String stockStatus;
    @Schema(description = "마일리지", example = "1450")
    private Integer mileage;
    @Schema(description = "도서 커버 이미지 URL")
    private String cover;
    @Schema(description = "카테고리 ID", example = "2105")
    private Integer categoryId;
    @Schema(description = "카테고리명", example = "국내도서>컴퓨터/인터넷")
    private String categoryName;
    @Schema(description = "출판사", example = "인사이트")
    private String publisher;
    @Schema(description = "판매 포인트 (판매량 지표)", example = "120000")
    private Integer salesPoint;
    @Schema(description = "성인 도서 여부", example = "false")
    private Boolean adult;
    @Schema(description = "정가 판매 여부", example = "true")
    private Boolean fixedPrice;
    @Schema(description = "고객 평점 (10점 만점)", example = "9")
    private Integer customerReviewRank;
    @Schema(description = "베스트셀러 순위", example = "1")
    private Integer bestRank;

    @Schema(description = "부가 정보 (목차, 추천사 등)")
    private SubInfo subInfo;
    @Schema(description = "전체 소개글")
    private String fullDescription;
    @Schema(description = "전체 소개글 2")
    private String fullDescription2;
    @Schema(description = "목차")
    private String toc;
    @Schema(description = "AI/MD 추천 코멘트 목록")
    private List<BookComment> bookCommentList;

    public static AladinBookResponse from(AladinBook aladinBook) {
        AladinBookResponse dto = new AladinBookResponse();
        dto.setItemId(aladinBook.getItemId());
        dto.setTitle(aladinBook.getTitle());
        dto.setLink(aladinBook.getLink());
        dto.setAuthor(aladinBook.getAuthor());
        dto.setPubDate(aladinBook.getPubDate());
        dto.setDescription(aladinBook.getDescription());
        dto.setIsbn(aladinBook.getIsbn());
        dto.setIsbn13(aladinBook.getIsbn13());
        dto.setPriceSales(aladinBook.getPriceSales());
        dto.setPriceStandard(aladinBook.getPriceStandard());
        dto.setMallType(aladinBook.getMallType());
        dto.setStockStatus(aladinBook.getStockStatus());
        dto.setMileage(aladinBook.getMileage());
        dto.setCover(aladinBook.getCover());
        dto.setCategoryId(aladinBook.getCategoryId());
        dto.setCategoryName(aladinBook.getCategoryName());
        dto.setPublisher(aladinBook.getPublisher());
        dto.setSalesPoint(aladinBook.getSalesPoint());
        dto.setAdult(aladinBook.getAdult());
        dto.setFixedPrice(aladinBook.getFixedPrice());
        dto.setCustomerReviewRank(aladinBook.getCustomerReviewRank());
        dto.setBestRank(aladinBook.getBestRank());
        dto.setSubInfo(aladinBook.getSubInfo());
        dto.setFullDescription(aladinBook.getFullDescription());
        dto.setFullDescription2(aladinBook.getFullDescription2());
        dto.setToc(aladinBook.getToc());
        dto.setBookCommentList(aladinBook.getBookCommentList());
        return dto;
    }
}
