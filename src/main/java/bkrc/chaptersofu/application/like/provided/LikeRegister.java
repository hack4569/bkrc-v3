package bkrc.chaptersofu.application.like.provided;

import bkrc.chaptersofu.domain.like.Like;

public interface LikeRegister {
    Like like(Long bookId, Long memberId);
    void unLike(Long bookId, Long memberId);
}
