package com.bkrc.bkrcv3.aladin.application.response;

import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.aladin.entity.BookComment;
import com.bkrc.bkrcv3.aladin.entity.SubInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** 알라딘 책 API 응답용 DTO (도메인 엔티티와 분리) */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AladinBookResponse {

    private Integer itemId;
    private String title;
    private String link;
    private String author;
    private String pubDate;
    private String description;
    private String isbn;
    private String isbn13;
    private Integer priceSales;
    private Integer priceStandard;
    private String mallType;
    private String stockStatus;
    private Integer mileage;
    private String cover;
    private Integer categoryId;
    private String categoryName;
    private String publisher;
    private Integer salesPoint;
    private Boolean adult;
    private Boolean fixedPrice;
    private Integer customerReviewRank;
    private Integer bestRank;

    private SubInfo subInfo;
    private String fullDescription;
    private String fullDescription2;
    private String toc;
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
