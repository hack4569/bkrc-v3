package com.bkrc.bkrcv3.aladin.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;


@Schema(description = "도서 카테고리")
@Entity
@Table(name="CATEGORY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Schema(description = "카테고리 ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int cid;

    @Schema(description = "알라딘 서브 카테고리 ID", example = "2105")
    private String subCid;

    @Schema(description = "카테고리 대분류명", example = "컴퓨터/인터넷")
    private String depth1;
}
