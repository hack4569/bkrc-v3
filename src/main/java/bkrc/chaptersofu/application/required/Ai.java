package bkrc.chaptersofu.application.required;

import java.util.List;

public interface Ai {
    List<String> getRecommend(String bookTitle);
    String filteringContent(String comment);
    BookDescriptionSummary summarizeDescriptions(String comment);
}
