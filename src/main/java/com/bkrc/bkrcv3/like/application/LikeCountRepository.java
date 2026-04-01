package com.bkrc.bkrcv3.like.application;

import com.bkrc.bkrcv3.like.entity.LikeCount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface LikeCountRepository extends JpaRepository<LikeCount, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<LikeCount> findByItemId(Integer itemId);
}
