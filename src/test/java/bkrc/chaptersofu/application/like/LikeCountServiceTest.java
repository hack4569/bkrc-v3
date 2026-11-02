package bkrc.chaptersofu.application.like;

import org.junit.jupiter.api.Test;
import bkrc.chaptersofu.application.like.required.LikeCountStore;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LikeCountServiceTest {
    @Test
    void 이벤트_버전이_없으면_갱신을_거부한다() {
        LikeCountService service = new LikeCountService(mock(LikeCountStore.class));
        assertThatThrownBy(() -> service.createOrUpdate(7, 3, null, Duration.ofMinutes(1)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("eventVersion");
    }
}
