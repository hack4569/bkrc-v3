package com.bkrc.bkrcv3.aladin.entity;

import com.bkrc.bkrcv3.required.Ai;
import com.bkrc.bkrcv3.aladin.application.response.AladinBookResponse;
import com.bkrc.bkrcv3.common.constants.RcmdConst;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = "알라딘 도서 엔티티")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Slf4j
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

    public void settingBookCommentList(Ai ai) {
        List<BookComment> bookCommentList = new ArrayList<>();

        //책소개
        this.setUserBookDesc(ai, bookCommentList);
        //편집자 추천
        this.setUserMdRecommend(ai, bookCommentList);
        //ai 추천
        this.setAiRecommend(ai, bookCommentList);
        //책 속에서
        this.setUserPhrase(bookCommentList);
        //목차
        this.setUserToc(bookCommentList);
        bookCommentList.forEach(i -> i.setAladinBook(this));
        this.bookCommentList = bookCommentList;
    }

    private void setAiRecommend(Ai ai, List<BookComment> bookCommentList) {
        List<String> recommendations = ai.getRecommend(this.getTitle());
        if (!CollectionUtils.isEmpty(recommendations)) {
            String aiRecommend = "";
            if (recommendations.size() > 1) {
                aiRecommend += recommendations.stream().collect(Collectors.joining("<br>"));
            } else {
                aiRecommend = recommendations.get(0);
            }
            log.info("AladinBook aiRecommend: " + aiRecommend);
            bookCommentList.add(BookComment.create(aiRecommend, "aiRecommend"));
        }
    }

    /** 허용된 카테고리 집합에 포함되는지 (도메인 규칙) */
    public boolean isInAllowedCategories(Set<Integer> allowedCategoryIds) {
        if (allowedCategoryIds == null || allowedCategoryIds.isEmpty()) {
            return true;
        }
        return categoryId != null && allowedCategoryIds.contains(categoryId);
    }

    /** 기준일(yyyyMMdd) 이후 출간된 책인지 (도메인 규칙) */
    public boolean isPublishedAfter(int anchorDateYyyyMMdd) {
        if (pubDate == null || pubDate.isEmpty()) {
            return false;
        }
        try {
            int bookDate = Integer.parseInt(pubDate.replaceAll("[^0-9]", "").substring(0, 8));
            return bookDate >= anchorDateYyyyMMdd;
        } catch (Exception e) {
            return false;
        }
    }

    /** HTML 태그 제거 (도메인 내부 유틸) */
    private static String stripHtmlTags(String originStr) {
        if (originStr == null || originStr.isEmpty()) {
            return "";
        }
        return originStr.replaceAll("<[^>]*>", "");
    }

    private void setUserMdRecommend(Ai ai, List<BookComment> bookCommentList) {
        List<MdRecommend> mdRecommendList = this.getSubInfo().getMdRecommendList();
        if (!ObjectUtils.isEmpty(mdRecommendList)) {
            for (MdRecommend mdRecommend : mdRecommendList) {
                this.filterDescriptionByAi(ai, mdRecommend.getComment(), bookCommentList, "mdRecommend");
            }
        }
    }

    private void filterDescriptionByAi(Ai ai, String comment, List<BookComment> bookCommentList, String type) {
        bookCommentList.add(BookComment.create(ai.filteringContent(comment), type));
    }

    private void setUserBookDesc(Ai ai, List<BookComment> bookCommentList) {
        String fullDescription = StringUtils.hasText(this.getFullDescription2()) ? this.getFullDescription2() : this.getFullDescription();
        if (StringUtils.hasText(fullDescription)) {
            this.filterDescriptionByAi(ai, fullDescription, bookCommentList, "description");
        }
    }

    private void setUserToc(List<BookComment> bookCommentList) {
        String toc = this.getSubInfo().getToc();
        if (StringUtils.hasText(toc)) {
            toc = toc.replaceAll("<(/)?([pP]*)(\\s[pP]*=[^>]*)?(\\s)*(/)?>", "");
            bookCommentList.add(BookComment.create(toc, "toc"));
        }
    }

    private void setUserPhrase(List<BookComment> bookCommentList) {
        Phrase phrase;
        if (!ObjectUtils.isEmpty(this.getSubInfo().getPhraseList())) {
            int phraseLen = this.getSubInfo().getPhraseList().size();
            //j==0일 경우 이미지 확률이 높음
            for (int j = 1; j < phraseLen; j++) {

                phrase = this.getSubInfo().getPhraseList().get(j);
                String filteredPhrase = stripHtmlTags(phrase.getPhrase());
                if (!StringUtils.hasText(filteredPhrase)) {
                    continue;
                }
                String[] phraseArr = filteredPhrase.split("\\.");
                StringBuilder phraseContent = new StringBuilder();
                int phraseArrLen = phraseArr.length < RcmdConst.paragraphSlide ? phraseArr.length : RcmdConst.paragraphSlide;
                for (int k = 0; k < phraseArrLen; k++) {
                    phraseContent.append(phraseArr[k])
                            .append(". ");
                }
                bookCommentList.add(BookComment.create(phraseContent.toString(), "phrase"));
            }
        }
    }

    private void filterDescription(String description, List<BookComment> recommendCommentList, String type) {
        //설명 첫번재 문단은 삭제
        String descriptionParagraph = descriptionParagraphFunc(description);

        //모든 태그 제거
        String filteredDescriptionParagraph = stripHtmlTags(descriptionParagraph);
        String[] descriptionArr = filteredDescriptionParagraph.split("\\.");
        List<String> descriptionList = Arrays.asList(descriptionArr);
        //글자가 많을 경우 2개 또는 ... 처리
        int introduceSlide = descriptionList.size() >= 3 && filteredDescriptionParagraph.length() > RcmdConst.strMaxCount * 2 ? RcmdConst.introduceSlide : 1;
        int slide = 0;
        for (int i = 0; i < introduceSlide; i++) {
            StringBuilder content = new StringBuilder();
            for (int j = 0; content.length() < RcmdConst.strMaxCount; j++) {
                if (descriptionList.size() <= slide ) {
                    break;
                }
                //int startIdx = descriptionList.size() >= 3 ? slide + 1 : slide;


                content.append(descriptionList.get(slide))
                        .append(". ");
                slide++;
            }
            String contentValue = content.toString();
            if (StringUtils.hasText(content)) {
                recommendCommentList.add(BookComment.create(contentValue, type));
            }
        }

    }

    private String descriptionParagraphFunc(String originParagraph) {

        if (StringUtils.hasText(originParagraph)) {
            if (originParagraph.toLowerCase().contains("<br>")) {
                List<String> paragraphList = Arrays.stream(originParagraph.toLowerCase().split("<br>"))
                        .filter(p -> StringUtils.hasText(p) && p.length() > 10)
                        .collect(Collectors.toList());
                //paragraphList.stream().collect(Collectors.joining(".")).toString();
                if (originParagraph.length() < 100 & paragraphList.size() < 4) {
                    return originParagraph;
                }else {
                    paragraphList.remove(0);
                    return paragraphList.stream().collect(Collectors.joining("<BR>")).toString();
                }
            }
        }
        return originParagraph;
    }
}