package bkrc.chaptersofu.application.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class AfterCommitExecutorTest {

    @AfterEach
    void cleanUpTransactionContext() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void 트랜잭션이_없으면_즉시_실행한다() {
        AtomicBoolean executed = new AtomicBoolean(false);

        AfterCommitExecutor.execute(() -> executed.set(true));

        assertThat(executed).isTrue();
    }

    @Test
    void 트랜잭션_중에는_커밋된_후에_실행한다() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
        AtomicBoolean executed = new AtomicBoolean(false);

        AfterCommitExecutor.execute(() -> executed.set(true));
        assertThat(executed).isFalse();

        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);

        assertThat(executed).isTrue();
    }
}
