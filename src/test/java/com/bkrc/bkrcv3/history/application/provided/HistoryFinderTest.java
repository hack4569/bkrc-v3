package com.bkrc.bkrcv3.history.application.provided;

import com.bkrc.bkrcv3.api.CommonApiTest;
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
    void findHistoriesByMemberId() {
        historyRegister.saveHistory(1001, 2001L);
        historyRegister.saveHistory(1002, 2001L);

        assertThat(historyFinder.getHistoryByMemberId(2001L))
                .extracting(history -> history.getItemId())
                .containsExactlyInAnyOrder(1001, 1002);
    }

    @Test
    void returnEmptyListWhenHistoryDoesNotExist() {
        assertThat(historyFinder.getHistoryByMemberId(9999L)).isEmpty();
    }
}
