package bkrc.chaptersofu.application.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 성공 비즈니스 로그가 롤백된 작업을 성공으로 기록하지 않도록 커밋 이후에 실행합니다.
 * 트랜잭션이 없는 호출에서는 작업이 이미 완료된 것으로 보고 즉시 실행합니다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AfterCommitExecutor {

    public static void execute(Runnable action) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
