package bkrc.chaptersofu.application.hotbook.provided;

import bkrc.chaptersofu.domain.aladin.AladinBook;

import java.util.List;

public interface HotBookFinder {
    List<AladinBook> readAll(String dateStr);
}
