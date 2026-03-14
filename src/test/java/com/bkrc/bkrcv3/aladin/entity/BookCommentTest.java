package com.bkrc.bkrcv3.aladin.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BookComment 도메인")
class BookCommentTest {

    @Test
    @DisplayName("create로 comment와 type이 설정된 인스턴스를 만든다")
    void create_returnsInstanceWithCommentAndType() {
        String comment = "편집자 추천 문구입니다.";
        String type = "mdRecommend";

        BookComment result = BookComment.create(comment, type);

        assertThat(result).isNotNull();
        assertThat(result.getComment()).isEqualTo(comment);
        assertThat(result.getType()).isEqualTo(type);
        assertThat(result.getBookCommentId()).isNull();
        assertThat(result.getAladinBook()).isNull();
    }

    @Test
    @DisplayName("create로 toc 타입 코멘트를 만든다")
    void create_tocType() {
        BookComment result = BookComment.create("1장. 서론\n2장. 본론", "toc");

        assertThat(result.getType()).isEqualTo("toc");
        assertThat(result.getComment()).isEqualTo("1장. 서론\n2장. 본론");
    }
}
