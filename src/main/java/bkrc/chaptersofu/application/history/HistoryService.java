package bkrc.chaptersofu.application.history;

import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.common.shared.Snowflake;
import bkrc.chaptersofu.domain.history.History;
import bkrc.chaptersofu.domain.history.HistoryException;
import bkrc.chaptersofu.application.history.provided.HistoryFinder;
import bkrc.chaptersofu.application.history.provided.HistoryRegister;
import bkrc.chaptersofu.application.support.AfterCommitExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryService implements HistoryFinder, HistoryRegister {
    private final HistoryRepository historyRepository;
    private final Snowflake snowflake;

    @Override
    public List<History> getHistoryByMemberId(Long memberId) {
        return historyRepository.findAllByMemberId(memberId).orElseGet(List::of);
    }

    @Override
    public int deleteHistoryByMemberId(Long memberId) {
        return historyRepository.deleteByMemberId(memberId);
    }

    @Override
    public void saveHistory(Integer itemId, Long memberId) {
        try {
            History saved = historyRepository.saveAndFlush(History.create(itemId, memberId, snowflake.nextId()));
            // 중복 제약까지 확인하기 위해 flush가 성공한 뒤 열람 이력을 기록합니다.
            AfterCommitExecutor.execute(() -> log.info(
                    "HISTORY_RECORDED historyId={} bookId={} memberId={}",
                    saved.getId(), saved.getItemId(), saved.getMemberId()
            ));
        } catch (DataIntegrityViolationException de) {
            throw new HistoryException(
                    ErrorCode.HISTORY_ALREADY_EXISTS,
                    "bookId=" + itemId + ", memberId=" + memberId,
                    de
            );
        } catch (Exception ex) {
            throw new HistoryException(
                    ErrorCode.SERVER_ERROR,
                    "operation=saveHistory, bookId=" + itemId + ", memberId=" + memberId,
                    ex
            );
        }
    }
}
