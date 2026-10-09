package com.bkrc.bkrcv3.history.application.provided;

public interface HistoryRegister {
    void saveHistory(Integer itemId, Long memberId);
    int deleteHistoryByMemberId(Long memberId);
}
