package com.bkrc.bkrcv3.like.application;

import com.bkrc.bkrcv3.like.entity.Like;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;

    @Transactional
    public Like like(Like like){
        return likeRepository
                .findByItemIdAndLoginId(like.getItemId(), like.getLoginId())
                .map(existing -> {
                    likeRepository.deleteById(existing.getLikeId());
                    return existing;
                })
                .orElseGet(() -> likeRepository.save(like));
    }
}
