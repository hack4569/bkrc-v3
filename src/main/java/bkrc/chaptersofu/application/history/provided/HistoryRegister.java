package bkrc.chaptersofu.application.history.provided;

public interface HistoryRegister {
    void saveHistory(Integer itemId, Long memberId);
    int deleteHistoryByMemberId(Long memberId);
}
