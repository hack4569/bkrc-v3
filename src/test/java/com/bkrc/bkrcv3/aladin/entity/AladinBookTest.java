package com.bkrc.bkrcv3.aladin.entity;

import com.bkrc.bkrcv3.aladin.application.response.AladinBookResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AladinBook 도메인")
class AladinBookTest {

    @Nested
    @DisplayName("isInAllowedCategories")
    class IsInAllowedCategories {

        @Test
        @DisplayName("allowedCategoryIds가 null이면 true")
        void returnsTrueWhenAllowedCategoriesIsNull() {
            AladinBook book = bookWithCategoryId(100);
            assertThat(book.isInAllowedCategories(null)).isTrue();
        }

        @Test
        @DisplayName("allowedCategoryIds가 비어있으면 true")
        void returnsTrueWhenAllowedCategoriesIsEmpty() {
            AladinBook book = bookWithCategoryId(100);
            assertThat(book.isInAllowedCategories(Set.of())).isTrue();
        }

        @Test
        @DisplayName("책의 categoryId가 허용 목록에 있으면 true")
        void returnsTrueWhenCategoryIsAllowed() {
            AladinBook book = bookWithCategoryId(101);
            assertThat(book.isInAllowedCategories(Set.of(100, 101, 102))).isTrue();
        }

        @Test
        @DisplayName("책의 categoryId가 허용 목록에 없으면 false")
        void returnsFalseWhenCategoryIsNotAllowed() {
            AladinBook book = bookWithCategoryId(99);
            assertThat(book.isInAllowedCategories(Set.of(100, 101, 102))).isFalse();
        }

        @Test
        @DisplayName("책의 categoryId가 null이면 false")
        void returnsFalseWhenBookCategoryIdIsNull() {
            AladinBookResponse aladinBookResponse = new AladinBookResponse();
            aladinBookResponse.setCategoryId(null);
            AladinBook book = AladinBook.toEntity(aladinBookResponse);
            assertThat(book.isInAllowedCategories(Set.of(100))).isFalse();
        }
    }

    @Nested
    @DisplayName("isPublishedAfter")
    class IsPublishedAfter {

        private static final int ANCHOR_2024_01_01 = 20240101;

        @Test
        @DisplayName("pubDate가 빈 문자열이면 false")
        void returnsFalseWhenPubDateIsEmpty() {
            AladinBookResponse aladinBookResponse = new AladinBookResponse();
            aladinBookResponse.setPubDate("");
            AladinBook book = AladinBook.toEntity(aladinBookResponse);
            assertThat(book.isPublishedAfter(ANCHOR_2024_01_01)).isFalse();
        }

        @Test
        @DisplayName("yyyyMMdd 형식으로 기준일 이후면 true")
        void returnsTrueWhenPublishedAfterAnchor_yyyyMMdd() {
            AladinBook book = bookWithPubDate("20240615");
            assertThat(book.isPublishedAfter(ANCHOR_2024_01_01)).isTrue();
        }

        @Test
        @DisplayName("yyyy-MM-dd 형식으로 기준일 이후면 true")
        void returnsTrueWhenPublishedAfterAnchor_yyyyMmDd() {
            AladinBook book = bookWithPubDate("2024-06-15");
            assertThat(book.isPublishedAfter(ANCHOR_2024_01_01)).isTrue();
        }

        @Test
        @DisplayName("기준일과 같으면 true")
        void returnsTrueWhenPublishedOnAnchor() {
            AladinBook book = bookWithPubDate("2024-01-01");
            assertThat(book.isPublishedAfter(ANCHOR_2024_01_01)).isTrue();
        }

        @Test
        @DisplayName("기준일 이전이면 false")
        void returnsFalseWhenPublishedBeforeAnchor() {
            AladinBook book = bookWithPubDate("2023-12-31");
            assertThat(book.isPublishedAfter(ANCHOR_2024_01_01)).isFalse();
        }

        @Test
        @DisplayName("파싱 불가 형식이면 false")
        void returnsFalseWhenPubDateCannotBeParsed() {
            AladinBook book = bookWithPubDate("invalid");
            assertThat(book.isPublishedAfter(ANCHOR_2024_01_01)).isFalse();
        }
    }

    private static AladinBook bookWithCategoryId(int categoryId) {
        AladinBookResponse aladinBookResponse = new AladinBookResponse();
        aladinBookResponse.setCategoryId(categoryId);
        AladinBook book = AladinBook.toEntity(aladinBookResponse);

        return book;
    }

    private static AladinBook bookWithPubDate(String pubDate) {
        AladinBookResponse aladinBookResponse = new AladinBookResponse();
        aladinBookResponse.setPubDate(pubDate);
        AladinBook book = AladinBook.toEntity(aladinBookResponse);
        return book;
    }
}
