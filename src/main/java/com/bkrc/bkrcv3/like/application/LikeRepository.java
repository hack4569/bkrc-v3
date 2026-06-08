package com.bkrc.bkrcv3.like.application;

import com.bkrc.bkrcv3.like.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByBookItemIdAndMemberLoginId(int itemId, String loginId);
    Optional<List<Like>> findByMemberLoginId(String loginId);
}
