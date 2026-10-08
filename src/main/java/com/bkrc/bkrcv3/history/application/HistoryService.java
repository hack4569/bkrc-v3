package com.bkrc.bkrcv3.history.application;

import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.common.shared.Snowflake;
import com.bkrc.bkrcv3.history.entity.History;
import com.bkrc.bkrcv3.history.entity.HistoryException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository historyRepository;
    private final Snowflake snowflake;

    public List<History> getHistoryByMemberId(Long memberId) {
        return historyRepository.findAllByMemberId(memberId).orElse(null);
    }

    public int deleteHistoryByMemberId(Long memberId) {
        return historyRepository.deleteByMemberId(memberId);
    }

    public void saveHistory(Integer itemId, Long memberId) {
        try {
            historyRepository.save(History.create(itemId, memberId, snowflake.nextId()));
        } catch (DataIntegrityViolationException de) {
            throw new HistoryException(ErrorCode.HISTORY_ALREADY_EXISTS, de);
        } catch (Exception ex) {
            throw new HistoryException(ErrorCode.SERVER_ERROR, ex);
        }
    }
}
