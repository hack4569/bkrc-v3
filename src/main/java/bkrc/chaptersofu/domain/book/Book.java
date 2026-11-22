package bkrc.chaptersofu.domain.book;

import bkrc.chaptersofu.domain.aladin.BookComment;
import bkrc.chaptersofu.domain.aladin.SubInfo;
import bkrc.chaptersofu.domain.history.History;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "도서 엔티티")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "book",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_book_mall_item",
                columnNames = {"mall_name", "item_id"}
        )
)
public class Book {

    @Schema(description = "내부 도서 ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long bookId;

    @Schema(description = "외부 판매처 상품 ID", example = "123456789")
    @Column(name = "item_id", nullable = false)
    private Integer itemId;

    @Schema(description = "도서 정보 판매처", example = "YES24")
    @Enumerated(EnumType.STRING)
    @Column(name = "mall_name", nullable = false, length = 20)
    private BookMall mallName;

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
    @Schema(description = "도서 커버 이미지 URL")
    private String cover;
    @Schema(description = "카테고리 ID", example = "2105")
    private Integer categoryId;
    @Schema(description = "카테고리명", example = "국내도서>컴퓨터/인터넷")
    private String categoryName;
    @Schema(description = "출판사", example = "인사이트")
    private String publisher;
    @Schema(description = "성인 도서 여부", example = "false")
    private Boolean adult;
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
    @Column(columnDefinition = "MEDIUMTEXT")
    private String toc;
    @Schema(description = "AI/MD 추천 코멘트 목록")
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookComment> bookCommentList;

    public void settingBookCommentList(List<BookComment> bookCommentList) {
        // 연관관계의 주인인 BookComment에도 현재 도서를 설정해 book_id 외래키가 함께 저장되도록 한다.
        bookCommentList.forEach(bookComment -> bookComment.setBook(this));
        this.bookCommentList = bookCommentList;
    }

    /**
     * 배치가 동일 판매처의 기존 도서를 갱신할 때 영속 식별자를 이어받는다.
     * 외부 itemId를 PK로 사용하지 않으므로 저장 전에 기존 bookId를 복원해야 INSERT 충돌을 피할 수 있다.
     */
    public void restoreBookId(Long bookId) {
        this.bookId = bookId;
    }

    /** 허용된 카테고리 집합에 포함되는지 (도메인 규칙) */
    public boolean isInAllowedCategories(Set<Integer> allowedCategoryIds) {
        if (allowedCategoryIds == null || allowedCategoryIds.isEmpty()) {
            return true;
        }
        return categoryId != null && allowedCategoryIds.contains(categoryId);
    }

    /** 기준일로부터 1년보다 이전에 출간된 책인지 확인한다. */
    public boolean wasPublishedBeforeOneYearAgo(LocalDate referenceDate)  {
        if (!StringUtils.hasText(pubDate)) {
            return false;
        }
        try {
            LocalDate pubLocalDate = LocalDate.parse(
                    pubDate.replaceAll("[^0-9]", "").substring(0, 8),
                    DateTimeFormatter.ofPattern("yyyyMMdd")
            );
            return pubLocalDate.isBefore(referenceDate.minusYears(1));
        } catch (Exception e) {
            return false;
        }
    }

    /** 기준일 이전에 조회한 이력이 없는 책인지 확인한다. */
    public boolean hasNotBeenViewedBefore(List<History> histories, LocalDate referenceDate) {
        if (histories == null || histories.isEmpty()) {
            return true;
        }

        return histories.stream().noneMatch(history ->
                Objects.equals(bookId, history.getBookId())
                        && !referenceDate.isEqual(history.getCreated().toLocalDate())
        );
    }
    public String getDescForRecommend() {
        String fullDescription = StringUtils.hasText(this.getFullDescription2()) ? this.getFullDescription2() : this.getFullDescription();
        if (StringUtils.hasText(fullDescription)) {
            return fullDescription;
        }
        return null;
    }
}
