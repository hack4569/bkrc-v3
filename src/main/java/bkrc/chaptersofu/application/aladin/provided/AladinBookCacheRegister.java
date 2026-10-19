package bkrc.chaptersofu.application.aladin.provided;

import bkrc.chaptersofu.domain.aladin.AladinBook;

import java.util.List;

public interface AladinBookCacheRegister {
    void saveListForRedis(List<AladinBook> books);
}
