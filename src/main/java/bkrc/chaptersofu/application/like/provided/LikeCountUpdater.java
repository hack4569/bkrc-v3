package bkrc.chaptersofu.application.like.provided;

import java.time.Duration;

public interface LikeCountUpdater {
    boolean createOrUpdate(Long bookId, Integer likeCount, Long eventVersion, Duration ttl);
}
