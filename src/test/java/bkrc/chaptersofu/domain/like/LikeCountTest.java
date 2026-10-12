package bkrc.chaptersofu.domain.like;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LikeCountTest {
    @Test
    void 좋아요_수를_증가시키면_이벤트_버전도_증가한다() {
        LikeCount count = LikeCount.create(1, 3);

        count.increase();

        assertThat(count.getLikeCount()).isEqualTo(4);
        assertThat(count.getEventVersion()).isEqualTo(1L);
    }

    @Test
    void 좋아요_수를_감소시키면_이벤트_버전도_증가한다() {
        LikeCount count = LikeCount.create(1, 3);

        count.decrease();

        assertThat(count.getLikeCount()).isEqualTo(2);
        assertThat(count.getEventVersion()).isEqualTo(1L);
    }
}
