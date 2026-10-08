package com.bkrc.bkrcv3.like.application.provided;

import com.bkrc.bkrcv3.like.domain.Like;

import java.util.List;

public interface LikeFinder {
    List<Like> getMyLikes(Long memberId);
}
