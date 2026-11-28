package bkrc.chaptersofu.application.history.provided;

public interface HistoryRegister {
    void saveHistory(Long bookId, Long memberId);
    int deleteHistoryByMemberId(Long memberId);
}
