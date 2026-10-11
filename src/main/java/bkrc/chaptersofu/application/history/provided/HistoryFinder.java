package bkrc.chaptersofu.application.history.provided;

import bkrc.chaptersofu.domain.history.History;

import java.util.List;

public interface HistoryFinder {
    List<History> getHistoryByMemberId(Long memberId);
}
