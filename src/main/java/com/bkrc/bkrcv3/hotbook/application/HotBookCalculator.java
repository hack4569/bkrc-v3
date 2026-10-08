package com.bkrc.bkrcv3.hotbook.application;

import com.bkrc.bkrcv3.like.application.provided.LikeCountFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HotBookCalculator {
    private final LikeCountFinder likeCountFinder;

    private static final long ARTICLE_LIKE_COUNT_WEIGHT = 3;
    public long calculate(Integer bookId) {
        return Optional.ofNullable(likeCountFinder.readLikeCount(bookId)).orElse(0L) * ARTICLE_LIKE_COUNT_WEIGHT;
    }
}
