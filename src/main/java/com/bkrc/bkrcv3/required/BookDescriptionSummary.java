package com.bkrc.bkrcv3.required;

public record BookDescriptionSummary(
        String overview,
        String insight
) {
    public static BookDescriptionSummary empty() {
        return new BookDescriptionSummary("", "");
    }
}
