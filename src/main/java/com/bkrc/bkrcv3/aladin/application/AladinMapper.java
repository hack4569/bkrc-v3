package com.bkrc.bkrcv3.aladin.application;

import com.bkrc.bkrcv3.aladin.application.response.AladinBookResponse;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import org.springframework.stereotype.Component;

/**
 * Aladin 도메인 엔티티 ↔ 애플리케이션 DTO 변환.
 * 변환 책임을 application 레이어에 두어 도메인 엔티티가 DTO에 의존하지 않도록 한다.
 */
@Component
public class AladinMapper {

    public AladinBookResponse toResponse(AladinBook book) {
        return book == null ? null : AladinBookResponse.from(book);
    }

    public AladinBook toEntity(AladinBookResponse response) {
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
