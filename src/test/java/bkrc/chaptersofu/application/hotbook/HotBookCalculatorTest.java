package bkrc.chaptersofu.application.hotbook;

import bkrc.chaptersofu.application.like.provided.LikeCountFinder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class HotBookCalculatorTest {
    private final LikeCountFinder likeCountFinder = mock(LikeCountFinder.class);
    private final HotBookCalculator calculator = new HotBookCalculator(likeCountFinder);

    @Test
    void 좋아요_수에_가중치를_적용한다() {
        when(likeCountFinder.readLikeCount(10)).thenReturn(4L);
        assertThat(calculator.calculate(10)).isEqualTo(12L);
    }

    @Test
    void 좋아요_수가_없으면_점수는_0이다() {
        when(likeCountFinder.readLikeCount(10)).thenReturn(null);
        assertThat(calculator.calculate(10)).isZero();
    }
}
