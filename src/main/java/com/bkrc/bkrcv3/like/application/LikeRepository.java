package com.bkrc.bkrcv3.like.application;

import com.bkrc.bkrcv3.like.domain.Like;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByBookItemIdAndMemberMemberId(int itemId, Long memberId);

    @EntityGraph(attributePaths = "book")
    List<Like> findByMemberMemberId(Long memberId);
}
