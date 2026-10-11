package bkrc.chaptersofu.application.like.provided;

import bkrc.chaptersofu.domain.like.Like;

public interface LikeRegister {
    Like like(Integer itemId, Long memberId);
    void unLike(Integer itemId, Long memberId);
}
