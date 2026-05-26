package com.bkrc.bkrcv3.history.application;

import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.common.shared.Snowflake;
import com.bkrc.bkrcv3.exception.BusinessException;
import com.bkrc.bkrcv3.history.entity.History;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository historyRepository;
    private final Snowflake snowflake;

    public List<History> getHistoryByLoginId(String loginId) {
        return historyRepository.findAllByLoginId(loginId).orElse(null);
    }

    public int deleteHistoryByLoginId(String loginId) {
        return historyRepository.deleteByLoginId(loginId);
    }

    public void saveHistory(Integer itemId, String loginId) {
        try {
            historyRepository.save(History.create(itemId, loginId, snowflake.nextId()));
        } catch (DataIntegrityViolationException de) {
            throw new BusinessException(ErrorCode.HISTORY_ALREADY_EXISTS);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.SERVER_ERROR);
        }
    }
}
