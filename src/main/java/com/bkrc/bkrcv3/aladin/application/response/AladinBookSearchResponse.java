package com.bkrc.bkrcv3.aladin.application.response;

import com.bkrc.bkrcv3.aladin.entity.AladinBook;

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
