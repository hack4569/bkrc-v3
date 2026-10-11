package bkrc.chaptersofu.application.like.provided;

import bkrc.chaptersofu.domain.like.Like;

import java.util.List;

public interface LikeFinder {
    List<Like> getMyLikes(Long memberId);
}
