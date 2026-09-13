package com.bkrc.bkrcv3.like.application;

import com.bkrc.bkrcv3.like.entity.LikeCount;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LikeCountRepository extends JpaRepository<LikeCount, Integer> {
    Optional<LikeCount> findByItemId(Integer itemId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO like_count_book (item_id, like_count, event_version)
            VALUES (:itemId, :delta, 1)
            ON DUPLICATE KEY UPDATE
                like_count = like_count + :delta,
                event_version = event_version + 1
            """, nativeQuery = true)
    void applyDelta(@Param("itemId") Integer itemId, @Param("delta") Integer delta);
}
