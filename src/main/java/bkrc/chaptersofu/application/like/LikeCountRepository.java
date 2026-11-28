package bkrc.chaptersofu.application.like;

import bkrc.chaptersofu.domain.like.LikeCount;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LikeCountRepository extends JpaRepository<LikeCount, Long> {
    Optional<LikeCount> findByBookId(Long bookId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO like_count_book (book_id, like_count, event_version)
            VALUES (:bookId, :delta, 1)
            ON DUPLICATE KEY UPDATE
                like_count = like_count + :delta,
                event_version = event_version + 1
            """, nativeQuery = true)
    void applyDelta(@Param("bookId") Long bookId, @Param("delta") Integer delta);
}
