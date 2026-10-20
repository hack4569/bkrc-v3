package bkrc.chaptersofu.application.member;

import bkrc.chaptersofu.application.member.provided.MemberFinder;
import bkrc.chaptersofu.domain.member.Member;
import bkrc.chaptersofu.domain.member.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;

@Service
@Transactional(readOnly = true)
@Validated
@RequiredArgsConstructor
public class MemberQueryService implements MemberFinder {
    private final MemberRepository memberRepository;

    @Override
    public Member getMemberByLoginId(String loginId) {
        return memberRepository.findMemberByLoginId(loginId)
                .orElseThrow(() -> new MemberNotFoundException(loginId));
    }

    @Override
    public Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
//        return MemberInfoResponse.of(
//                member.getLoginId(),
//                likeService.getMyLikes(member),
//                memberRecommendationService.getMyRecommendations(member)
//        );
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        var member = memberRepository.findMemberByLoginId(loginId).orElseThrow( () -> new UsernameNotFoundException(loginId));

        return new User(member.getLoginId(), member.getPassword(),
                true, true, true, true,
                new ArrayList<>());
    }
}
