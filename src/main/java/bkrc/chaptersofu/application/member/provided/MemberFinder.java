package bkrc.chaptersofu.application.member.provided;

import bkrc.chaptersofu.domain.member.Member;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface MemberFinder extends UserDetailsService {
    Member getMemberByLoginId(String loginId);
    Member getMemberById(Long memberId);
}
