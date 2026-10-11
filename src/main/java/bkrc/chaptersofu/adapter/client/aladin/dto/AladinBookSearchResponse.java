package bkrc.chaptersofu.adapter.client.aladin.dto;

import bkrc.chaptersofu.domain.aladin.AladinBook;

public record AladinBookSearchResponse(
        Integer itemId,
        String title,
        String author,
        String cover,
        String link
) {
    public static AladinBookSearchResponse from(AladinBook book) {
        return new AladinBookSearchResponse(
                book.getItemId(),
                book.getTitle(),
                book.getAuthor(),
                book.getCover(),
                book.getLink()
        );
    }
}
