package bkrc.chaptersofu.application.history.provided;

import bkrc.chaptersofu.application.history.provided.*;

import bkrc.chaptersofu.adapter.webapi.CommonApiTest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@CommonApiTest
@Transactional
class HistoryFinderTest {
    @Autowired HistoryFinder historyFinder;
    @Autowired HistoryRegister historyRegister;

    @Test
    void 회원_ID로_이력_목록을_조회한다() {
        historyRegister.saveHistory(1001, 2001L);
        historyRegister.saveHistory(1002, 2001L);

        assertThat(historyFinder.getHistoryByMemberId(2001L))
                .extracting(history -> history.getItemId())
                .containsExactlyInAnyOrder(1001, 1002);
    }

    @Test
    void 이력이_없으면_빈_목록을_반환한다() {
        assertThat(historyFinder.getHistoryByMemberId(9999L)).isEmpty();
    }
}
