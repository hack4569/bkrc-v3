package com.bkrc.bkrcv3.history.application;

import com.bkrc.bkrcv3.history.entity.History;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository historyRepository;

    public List<History> getHistoryByLoginId(String loginId) {
        return historyRepository.findAllByLoginId(loginId).orElse(null);
    }

    public int deleteHistoryByLoginId(String loginId) {
        return historyRepository.deleteByLoginId(loginId);
    }
}
