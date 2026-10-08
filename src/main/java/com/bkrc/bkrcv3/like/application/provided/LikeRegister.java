package com.bkrc.bkrcv3.like.application.provided;

import com.bkrc.bkrcv3.like.domain.Like;

public interface LikeRegister {
    Like like(Integer itemId, Long memberId);
    void unLike(Integer itemId, Long memberId);
}
