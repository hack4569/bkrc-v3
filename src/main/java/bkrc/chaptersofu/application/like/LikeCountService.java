package bkrc.chaptersofu.application.like;

import bkrc.chaptersofu.application.like.provided.LikeCountFinder;
import bkrc.chaptersofu.application.like.provided.LikeCountUpdater;
import bkrc.chaptersofu.application.like.required.LikeCountStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LikeCountService implements LikeCountFinder, LikeCountUpdater {
    private final LikeCountStore likeCountStore;

    @Override
    public Long readLikeCount(Integer bookId) {
        return likeCountStore.readLikeCount(bookId);
    }

    @Override
    public boolean createOrUpdate(Integer bookId, Integer likeCount, Long eventVersion, Duration ttl) {
        Objects.requireNonNull(eventVersion, "BOOK_LIKE eventVersion must not be null");
        return likeCountStore.createOrUpdate(bookId, likeCount, eventVersion, ttl);
    }
}
