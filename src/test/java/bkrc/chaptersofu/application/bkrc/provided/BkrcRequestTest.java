package bkrc.chaptersofu.application.bkrc.provided;

import bkrc.chaptersofu.application.aladin.provided.AladinRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 외부 도서 API 요청 파라미터 변환 규칙을 검증한다.
 * BkrcRequest와 AladinRequest는 동일한 리플렉션 기반 변환을 사용하므로 함께 검증한다.
 */
class BkrcRequestTest {

    @Test
    void 값이_있는_필드만_요청_파라미터로_변환한다() {
        BkrcRequest request = BkrcRequest.create("9781234567890");
        request.nextPage();

        var params = request.getApiParamMap();

        assertThat(params.getFirst("itemId")).isEqualTo("9781234567890");
        assertThat(params.getFirst("itemIdType")).isEqualTo("ISBN13");
        assertThat(params.getFirst("output")).isEqualTo("js");
        // 빈 값(null) 필드는 API 오류를 피하기 위해 파라미터에서 제외되어야 한다.
        assertThat(params).doesNotContainKeys("query", "cover", "filterType");
        assertThat(request.getStart()).isEqualTo(1);
        assertThat(params.getFirst("start")).isEqualTo("1");
    }

    @Test
    void 알라딘_요청도_같은_규칙으로_변환한다() {
        AladinRequest request = AladinRequest.builder().query("데미안").start(1).build();
        request.nextPage();

        var params = request.getApiParamMap();

        assertThat(params.getFirst("query")).isEqualTo("데미안");
        assertThat(params.getFirst("start")).isEqualTo("2");
        assertThat(params.getFirst("querytype")).isEqualTo("Bestseller");
        assertThat(AladinRequest.create("isbn").getItemId()).isEqualTo("isbn");
    }
}
