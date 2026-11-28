package bkrc.chaptersofu.domain.book;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;


@Schema(description = "도서 카테고리")
@Entity
@Table(name="book_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookCategory {
    @Schema(description = "카테고리 ID")
    @Id
    private String categoryId;

    @Schema(description = "알라딘 서브 카테고리 ID", example = "2105")
    private String categoryName;
}
