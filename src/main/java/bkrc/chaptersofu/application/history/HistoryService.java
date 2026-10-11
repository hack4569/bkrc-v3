package bkrc.chaptersofu.application.history;

import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.common.shared.Snowflake;
import bkrc.chaptersofu.domain.history.History;
import bkrc.chaptersofu.domain.history.HistoryException;
import bkrc.chaptersofu.application.history.provided.HistoryFinder;
import bkrc.chaptersofu.application.history.provided.HistoryRegister;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
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
            historyRepository.saveAndFlush(History.create(itemId, memberId, snowflake.nextId()));
        } catch (DataIntegrityViolationException de) {
            throw new HistoryException(ErrorCode.HISTORY_ALREADY_EXISTS, de);
        } catch (Exception ex) {
            throw new HistoryException(ErrorCode.SERVER_ERROR, ex);
        }
    }
}
