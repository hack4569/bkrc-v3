package bkrc.chaptersofu.application.required;

public record BookDescriptionSummary(
        String overview,
        String insight
) {
    public static BookDescriptionSummary empty() {
        return new BookDescriptionSummary("", "");
    }
}
