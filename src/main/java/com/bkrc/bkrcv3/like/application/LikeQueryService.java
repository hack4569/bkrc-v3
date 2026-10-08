package com.bkrc.bkrcv3.like.application;

import com.bkrc.bkrcv3.like.application.provided.LikeFinder;
import com.bkrc.bkrcv3.like.domain.Like;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LikeQueryService implements LikeFinder {
    private final LikeRepository likeRepository;

    @Override
    public List<Like> getMyLikes(Long memberId) {
        return likeRepository.findByMemberMemberId(memberId);
    }

}
