package bkrc.chaptersofu.application.like;

import bkrc.chaptersofu.application.like.provided.LikeFinder;
import bkrc.chaptersofu.domain.like.Like;
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
