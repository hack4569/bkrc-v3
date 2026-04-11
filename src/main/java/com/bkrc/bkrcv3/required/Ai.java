package com.bkrc.bkrcv3.required;

import java.util.List;

public interface Ai {
    List<String> getRecommend(String bookTitle);
    String filteringContent(String comment);
}
