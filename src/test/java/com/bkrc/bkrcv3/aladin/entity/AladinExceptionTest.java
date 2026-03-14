package com.bkrc.bkrcv3.aladin.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AladinException 도메인")
class AladinExceptionTest {

    private static final String DEFAULT_PREFIX = "[알라딘 API] 연동 오류 발생";

    @Test
    @DisplayName("기본 생성자로 생성 시 기본 메시지를 가진다")
    void defaultConstructor_setsDefaultMessage() {
        AladinException ex = new AladinException();

        assertThat(ex.getMessage()).startsWith(DEFAULT_PREFIX);
        assertThat(ex.getErrorMessage()).startsWith(DEFAULT_PREFIX);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("메시지 생성자로 생성 시 메시지가 포함된다")
    void messageConstructor_includesMessage() {
        String detail = "상품조회시 데이터가 없습니다.";
        AladinException ex = new AladinException(detail);

        assertThat(ex.getMessage()).contains(DEFAULT_PREFIX);
        assertThat(ex.getMessage()).contains(detail);
        assertThat(ex.getErrorMessage()).contains(detail);
    }

    @Test
    @DisplayName("메시지와 원인 생성자로 생성 시 cause가 설정된다")
    void messageAndCauseConstructor_setsCause() {
        String detail = "파싱에러";
        Throwable cause = new IllegalArgumentException("invalid json");

        AladinException ex = new AladinException(detail, cause);

        assertThat(ex.getCause()).isSameAs(cause);
        assertThat(ex.getMessage()).contains(detail);
    }
}
