package bkrc.chaptersofu.domain.recommendation;

import bkrc.chaptersofu.domain.common.shared.BaseEntity;
import bkrc.chaptersofu.domain.member.Member;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "book_recommendation", uniqueConstraints = @UniqueConstraint(
        name = "uk_book_recommendation_book_member", columnNames = {"book_id", "member_id"}))
@Getter
@NoArgsConstructor
public class BookRecommendation extends BaseEntity {
    @Id
    private Long recommendationId;
    @Column(name = "book_id", nullable = false)
    private Long bookId;
    @Column(nullable = false)
    private String cover;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, length = 1000)
    private String link;
    @Column(nullable = false, length = 100)
    private String recommendation;
    @Column(nullable = false, length = 1)
    private String approved;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnore
    private Member member;

    public static BookRecommendation create(Long id, Long bookId, String cover, String title,
                                             String link, String recommendation, Member member) {
        BookRecommendation value = new BookRecommendation();
        value.recommendationId = id;
        value.bookId = bookId;
        value.cover = cover;
        value.title = title;
        value.link = link;
        value.recommendation = recommendation;
        value.approved = "W";
        value.member = member;
        return value;
    }

    public void updateRecommendation(String recommendation) {
        this.recommendation = recommendation;
        this.approved = "W";
    }
}
