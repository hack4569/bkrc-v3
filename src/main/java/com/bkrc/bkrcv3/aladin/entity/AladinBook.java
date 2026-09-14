package com.bkrc.bkrcv3.aladin.entity;

import com.bkrc.bkrcv3.history.entity.History;
import java.util.Objects;
import com.bkrc.bkrcv3.like.entity.Like;
import com.bkrc.bkrcv3.aladin.application.response.AladinBookResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "알라딘 도서 엔티티")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="aladin_book")
public class AladinBook {

    @Schema(description = "도서 고유 ID (알라딘 itemId)", example = "123456789")
    @Id
    private Integer itemId;
    @Schema(description = "도서 제목", example = "Clean Code")
    private String title;
    @Schema(description = "도서 상세 페이지 링크")
    private String link;
    @Schema(description = "저자", example = "로버트 C. 마틴")
    private String author;
    @Schema(description = "출판일 (yyyy-MM-dd)", example = "2013-12-24")
    private String pubDate;
    @Schema(description = "도서 소개 (HTML 포함 가능)")
    @Column(columnDefinition = "TEXT")
    private String description;
    @Schema(description = "ISBN-10", example = "8966260959")
    private String isbn;
    @Schema(description = "ISBN-13", example = "9788966260959")
    private String isbn13;
    @Schema(description = "판매가", example = "26100")
    private Integer priceSales;
    @Schema(description = "정가", example = "29000")
    private Integer priceStandard;
    @Schema(description = "판매 유형 (BOOK 등)", example = "BOOK")
    private String mallType;
    @Schema(description = "재고 상태", example = "")
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
    @Schema(description = "베스트셀러 순위 (임시 데이터, DB 미저장)", example = "1")
    @Transient
    private Integer bestRank;

    @Schema(description = "부가 정보 (목차, 추천사 등, DB 미저장)")
    @Transient
    private SubInfo subInfo;
    @Schema(description = "전체 소개글 (DB 미저장)")
    @Transient
    private String fullDescription;
    @Schema(description = "전체 소개글 2 (DB 미저장)")
    @Transient
    private String fullDescription2;
    //    @OneToMany(mappedBy = "aladinBook", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    public List<Review> reviewList;
    @Schema(description = "목차")
    private String toc;
    @Schema(description = "AI/MD 추천 코멘트 목록")
    @OneToMany(mappedBy = "aladinBook", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookComment> bookCommentList;

    @JsonIgnore
    @Schema(description = "좋아요한 도서 목록")
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Like> bookLikes;

    public static AladinBook toEntity(AladinBookResponse response) {
        if (response == null) {
            return null;
        }

        AladinBook book = new AladinBook();
        book.itemId = response.getItemId();
        book.title = response.getTitle();
        book.link = response.getLink();
        book.author = response.getAuthor();
        book.pubDate = response.getPubDate();
        book.description = response.getDescription();
        book.isbn = response.getIsbn();
        book.isbn13 = response.getIsbn13();
        book.priceSales = response.getPriceSales();
        book.priceStandard = response.getPriceStandard();
        book.mallType = response.getMallType();
        book.stockStatus = response.getStockStatus();
        book.mileage = response.getMileage();
        book.cover = response.getCover();
        book.categoryId = response.getCategoryId();
        book.categoryName = response.getCategoryName();
        book.publisher = response.getPublisher();
        book.salesPoint = response.getSalesPoint();
        book.adult = response.getAdult();
        book.fixedPrice = response.getFixedPrice();
        book.customerReviewRank = response.getCustomerReviewRank();
        book.bestRank = response.getBestRank();
        book.subInfo = response.getSubInfo();
        book.fullDescription = response.getFullDescription();
        book.fullDescription2 = response.getFullDescription2();
        book.toc = response.getToc();
        book.bookCommentList = response.getBookCommentList();

        return book;
    }

    public void settingBookCommentList(List<BookComment> bookCommentList) {
        bookCommentList.forEach(i -> i.setAladinBook(this));
        this.bookCommentList = bookCommentList;
    }

    /** 허용된 카테고리 집합에 포함되는지 (도메인 규칙) */
    public boolean isInAllowedCategories(Set<Integer> allowedCategoryIds) {
        if (allowedCategoryIds == null || allowedCategoryIds.isEmpty()) {
            return true;
        }
        return categoryId != null && allowedCategoryIds.contains(categoryId);
    }

    /** 1년전 출간된 책인지 (도메인 규칙) */
    public boolean publishDateFilter() {
        if (!StringUtils.hasText(pubDate)) {
            return false;
        }
        try {
            LocalDate pubLocalDate = LocalDate.parse(
                    pubDate.replaceAll("[^0-9]", "").substring(0, 8),
                    DateTimeFormatter.ofPattern("yyyyMMdd")
            );
            return pubLocalDate.isBefore(LocalDate.now().minusYears(1));
        } catch (Exception e) {
            return false;
        }
    }

    /** 히스토리에 없는 책을 필터링하는 Predicate (도메인 규칙) */
    public static Predicate historyFilter(List<History> histories) {
        // 히스토리에 없는 책을 필터링하는 Predicate
        if (ObjectUtils.isEmpty(histories)) return book -> true;

        // 히스토리에 없는 책을 필터링하는 Predicate
        Predicate<AladinBook> historyFilter = book -> {

            return !(histories.stream().anyMatch(history ->
                    Objects.equals(book.getItemId(), history.getItemId()) &&
                            !LocalDate.now().isEqual(history.getCreated().toLocalDate())
            ));
        };
        return historyFilter;
    }
    public String getDescForRecommend() {
        String fullDescription = StringUtils.hasText(this.getFullDescription2()) ? this.getFullDescription2() : this.getFullDescription();
        if (StringUtils.hasText(fullDescription)) {
            return fullDescription;
        }
        return null;
    }

}
