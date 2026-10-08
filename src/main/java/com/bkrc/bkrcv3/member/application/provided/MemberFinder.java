package com.bkrc.bkrcv3.member.application.provided;

import com.bkrc.bkrcv3.member.domain.Member;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface MemberFinder extends UserDetailsService {
    Member getMemberByLoginId(String loginId);
    Member getMemberById(Long memberId);
}
