package com.bkrc.bkrcv3.hotbook.application;

import com.bkrc.bkrcv3.like.application.LikeCountRepository;
import com.bkrc.bkrcv3.like.application.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HotBookCalculator {
    private final LikeService likeService;

    private static final long ARTICLE_LIKE_COUNT_WEIGHT = 3;
    public long calculate(Integer bookId) {
        Long bookLikeCount = likeService.read(bookId);

        return bookLikeCount * ARTICLE_LIKE_COUNT_WEIGHT;
    }
}
