package bkrc.chaptersofu.domain.common.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 날짜 문자열 변환 유틸의 기본 포맷과 null 처리 규칙을 검증한다.
 */
class LocalDateUtilsTest {
    private final LocalDateTime dateTime = LocalDateTime.of(2026, 11, 28, 9, 5, 7);
    private final LocalDate date = LocalDate.of(2026, 11, 28);

    @Test
    void 날짜와_일시를_포맷_문자열로_변환한다() {
        assertThat(LocalDateUtils.getDate(dateTime)).isEqualTo("2026-11-28");
        assertThat(LocalDateUtils.getDateTime(dateTime)).isEqualTo("2026-11-28 09:05:07");
        assertThat(LocalDateUtils.localDateTimeToString(dateTime)).isEqualTo("20261128090507");
        assertThat(LocalDateUtils.localDateToString(date)).isEqualTo("20261128");
        assertThat(LocalDateUtils.localDateToString(date, "yyyy/MM/dd")).isEqualTo("2026/11/28");
    }

    @Test
    void null_입력은_빈_문자열로_변환한다() {
        // 화면/로그 출력 시 NPE 대신 빈 값을 사용하도록 하는 규칙이다.
        assertThat(LocalDateUtils.localDateToString(null)).isEmpty();
        assertThat(LocalDateUtils.localDateToString(null, "yyyy")).isEmpty();
        assertThat(LocalDateUtils.localDateTimeToString(null)).isEmpty();
        assertThat(LocalDateUtils.localDateTimeToString(null, "yyyy")).isEmpty();
    }

    @Test
    void 문자열을_날짜와_일시로_파싱한다() {
        assertThat(LocalDateUtils.getLocalDate("20261128")).isEqualTo(date);
        assertThat(LocalDateUtils.getLocalDateTime("20261128090507")).isEqualTo(dateTime);
        assertThat(LocalDateUtils.processMessage("123")).isEqualTo("SKC123");
    }

    @Test
    void 사용자_지정_날짜는_Calendar_월_보정_없이_변환된다() {
        // Calendar의 월은 0부터 시작하므로 입력한 월보다 한 달 뒤 날짜가 반환되는 기존 동작을 고정한다.
        assertThat(LocalDateUtils.getCustomDate("2026-01-15")).isEqualTo("20260215");
    }
}
