package com.bkrc.bkrcv3.history.application.provided;

import com.bkrc.bkrcv3.history.domain.History;

import java.util.List;

public interface HistoryFinder {
    List<History> getHistoryByMemberId(Long memberId);
}
