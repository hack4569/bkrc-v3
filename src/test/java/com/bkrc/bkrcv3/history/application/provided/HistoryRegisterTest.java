package com.bkrc.bkrcv3.history.application.provided;

import com.bkrc.bkrcv3.api.CommonApiTest;
import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.history.domain.HistoryException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@CommonApiTest
@Transactional
class HistoryRegisterTest {
    @Autowired HistoryRegister historyRegister;
    @Autowired HistoryFinder historyFinder;

    @Test
    void saveAndDeleteHistory() {
        historyRegister.saveHistory(1001, 2001L);
        assertThat(historyFinder.getHistoryByMemberId(2001L)).hasSize(1);

        assertThat(historyRegister.deleteHistoryByMemberId(2001L)).isEqualTo(1);
        assertThat(historyFinder.getHistoryByMemberId(2001L)).isEmpty();
    }

    @Test
    void failSaveDuplicatedHistory() {
        historyRegister.saveHistory(1001, 2001L);

        HistoryException exception = catchThrowableOfType(
                HistoryException.class,
                () -> historyRegister.saveHistory(1001, 2001L));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.HISTORY_ALREADY_EXISTS);
    }
}
