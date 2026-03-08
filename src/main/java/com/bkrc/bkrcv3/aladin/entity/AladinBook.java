package com.bkrc.bkrcv3.aladin.entity;

import com.bkrc.bkrcv3.aladin.application.response.AladinBookResponse;
import com.bkrc.bkrcv3.aladin.utils.BookRecommendUtil;
import com.bkrc.bkrcv3.common.constants.RcmdConst;
import com.bkrc.bkrcv3.history.entity.History;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="aladin_book")
public class AladinBook {

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
    @Transient
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

    public void settingBookCommentList() {
        List<BookComment> bookCommentList = new ArrayList<>();

        //책소개
        this.setUserBookDesc(bookCommentList);
        //편집자 추천
        this.setUserMdRecommend(bookCommentList);
        //책 속에서
        this.setUserPhrase(bookCommentList);
        //목차
        this.setUserToc(bookCommentList);
        bookCommentList.forEach(i -> i.setAladinBook(this));
        this.bookCommentList = bookCommentList;
    }

    private void setUserMdRecommend(List<BookComment> bookCommentList) {
        List<MdRecommend> mdRecommendList = this.getSubInfo().getMdRecommendList();
        if (!ObjectUtils.isEmpty(mdRecommendList)) {
            for (MdRecommend mdRecommend : mdRecommendList) {
                this.filterDescription(mdRecommend.getComment(), bookCommentList, "mdRecommend");
            }
        }
    }

    private void setUserBookDesc(List<BookComment> bookCommentList) {
        String fullDescription = StringUtils.hasText(this.getFullDescription()) ? this.getFullDescription() : this.getFullDescription2();
        if (StringUtils.hasText(fullDescription)) {
            this.filterDescription(fullDescription, bookCommentList, "description");
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
                String filteredPhrase = BookRecommendUtil.filterStr(phrase.getPhrase());
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
        String filteredDescriptionParagraph = BookRecommendUtil.filterStr(descriptionParagraph);
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

    public Predicate historyFilter(List<History> histories) {
        // 히스토리에 없는 책을 필터링하는 Predicate
        if (ObjectUtils.isEmpty(histories)) return book -> true;

        // 히스토리에 없는 책을 필터링하는 Predicate
        Predicate<AladinBook> historyFilter = book -> {

            return histories.stream().noneMatch(history ->
                    book.getItemId() == history.getItemId() &&
                            LocalDate.now().isEqual(history.getCreated().toLocalDate())
            );
        };
        return historyFilter;
    }
}