package com.bkrc.bkrcv3.like.application.provided;

import java.time.Duration;

public interface LikeCountUpdater {
    boolean createOrUpdate(Integer bookId, Integer likeCount, Long eventVersion, Duration ttl);
}
