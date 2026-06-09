package com.bkrc.bkrcv3.member.application;

import com.bkrc.bkrcv3.member.application.request.MemberModifyRequest;
import com.bkrc.bkrcv3.member.application.request.MemberRegisterRequest;
import com.bkrc.bkrcv3.member.application.response.MemberInfoResponse;
import com.bkrc.bkrcv3.member.dto.MemberDto;
import com.bkrc.bkrcv3.member.entity.Member;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    Member saveMember(MemberRegisterRequest request);
    MemberDto getMemberByLoginId(String loginId);
    MemberInfoResponse getMemberInfo(Long memberId);
    Member modifyMember(Long memberId, MemberModifyRequest request);
    List<Member> getAllMembers();
}
