package bkrc.chaptersofu.application.like.required;

import java.time.Duration;

public interface LikeCountStore {
    Long readLikeCount(Integer bookId);

    /** 저장된 버전보다 새로운 이벤트인 경우에만 갱신하고 true를 반환합니다. */
    boolean createOrUpdate(Integer bookId, Integer likeCount, Long eventVersion, Duration ttl);
}
