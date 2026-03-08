package com.bkrc.bkrcv3.aladin.application.response;

import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.aladin.entity.BookComment;
import com.bkrc.bkrcv3.aladin.entity.SubInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.awt.print.Book;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="aladin_book")
public class AladinBookResponse {

    @Id
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

    @Transient
    private SubInfo subInfo;
    //프리미엄
    @Transient
    private String fullDescription;
    @Transient
    private String fullDescription2;
    //    @OneToMany(mappedBy = "aladinBook", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    public List<Review> reviewList;
    private String toc;
    @OneToMany(mappedBy = "aladinBook", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookComment> bookCommentList;

    public static AladinBookResponse from(AladinBook aladinBook) {
        AladinBookResponse aladinBookResponse = new AladinBookResponse();

        aladinBookResponse.setItemId(aladinBook.getItemId());
        aladinBookResponse.setTitle(aladinBook.getTitle());
        aladinBookResponse.setLink(aladinBook.getLink());
        aladinBookResponse.setAuthor(aladinBook.getAuthor());
        aladinBookResponse.setPubDate(aladinBook.getPubDate());
        aladinBookResponse.setDescription(aladinBook.getDescription());
        aladinBookResponse.setIsbn(aladinBook.getIsbn());
        aladinBookResponse.setIsbn13(aladinBook.getIsbn13());
        aladinBookResponse.setPriceSales(aladinBook.getPriceSales());
        aladinBookResponse.setPriceStandard(aladinBook.getPriceStandard());
        aladinBookResponse.setMallType(aladinBook.getMallType());
        aladinBookResponse.setStockStatus(aladinBook.getStockStatus());
        aladinBookResponse.setMileage(aladinBook.getMileage());
        aladinBookResponse.setCover(aladinBook.getCover());
        aladinBookResponse.setCategoryId(aladinBook.getCategoryId());
        aladinBookResponse.setCategoryName(aladinBook.getCategoryName());
        aladinBookResponse.setPublisher(aladinBook.getPublisher());
        aladinBookResponse.setSalesPoint(aladinBook.getSalesPoint());
        aladinBookResponse.setAdult(aladinBook.getAdult());
        aladinBookResponse.setFixedPrice(aladinBook.getFixedPrice());
        aladinBookResponse.setCustomerReviewRank(aladinBook.getCustomerReviewRank());
        aladinBookResponse.setBestRank(aladinBook.getBestRank());
        aladinBookResponse.setSubInfo(aladinBook.getSubInfo());
        aladinBookResponse.setFullDescription(aladinBook.getFullDescription());
        aladinBookResponse.setFullDescription2(aladinBook.getFullDescription2());
        aladinBookResponse.setToc(aladinBook.getToc());
        aladinBookResponse.setBookCommentList(aladinBook.getBookCommentList());

        return aladinBookResponse;
    }

    public static AladinBook from(AladinBookResponse response) {
        if (response == null) {
            return null;
        }
        AladinBook book = new AladinBook();
        book.setItemId(response.getItemId());
        book.setTitle(response.getTitle());
        book.setLink(response.getLink());
        book.setAuthor(response.getAuthor());
        book.setPubDate(response.getPubDate());
        book.setDescription(response.getDescription());
        book.setIsbn(response.getIsbn());
        book.setIsbn13(response.getIsbn13());
        book.setPriceSales(response.getPriceSales());
        book.setPriceStandard(response.getPriceStandard());
        book.setMallType(response.getMallType());
        book.setStockStatus(response.getStockStatus());
        book.setMileage(response.getMileage());
        book.setCover(response.getCover());
        book.setCategoryId(response.getCategoryId());
        book.setCategoryName(response.getCategoryName());
        book.setPublisher(response.getPublisher());
        book.setSalesPoint(response.getSalesPoint());
        book.setAdult(response.getAdult());
        book.setFixedPrice(response.getFixedPrice());
        book.setCustomerReviewRank(response.getCustomerReviewRank());
        book.setBestRank(response.getBestRank());
        book.setSubInfo(response.getSubInfo());
        book.setFullDescription(response.getFullDescription());
        book.setFullDescription2(response.getFullDescription2());
        book.setToc(response.getToc());
        book.setBookCommentList(response.getBookCommentList());
        return book;
    }
}