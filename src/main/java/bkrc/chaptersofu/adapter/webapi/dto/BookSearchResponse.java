package bkrc.chaptersofu.adapter.webapi.dto;

import bkrc.chaptersofu.domain.book.Book;
import bkrc.chaptersofu.domain.book.BookMall;

public record BookSearchResponse(
        Long bookId,
        Integer itemId,
        BookMall mallName,
        String title,
        String author,
        String cover,
        String link
) {
    public static BookSearchResponse from(Book book) {
        return new BookSearchResponse(
                book.getBookId(),
                book.getItemId(),
                book.getMallName(),
                book.getTitle(),
                book.getAuthor(),
                book.getCover(),
                book.getLink()
        );
    }
}
