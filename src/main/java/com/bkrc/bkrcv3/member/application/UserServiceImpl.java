package com.bkrc.bkrcv3.member.application;

import com.bkrc.bkrcv3.member.application.request.MemberRegisterRequest;
import com.bkrc.bkrcv3.member.dto.MemberDto;
import com.bkrc.bkrcv3.member.entity.Member;
import com.bkrc.bkrcv3.member.entity.MemberException;
import com.bkrc.bkrcv3.member.entity.PasswordEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Override
    public Member saveMember(MemberRegisterRequest request) {
        checkDuplicateId(request);
        checkPwd(request);

        var member = Member.register(request, passwordEncoder);
        return memberRepository.save(member);
    }

    private void checkPwd(MemberRegisterRequest request) {
        if (!request.passwordCheck().equals(request.password())) {
            throw new MemberException("비밀번호가 일치하지 않습니다.");
        }
    }

    private void checkDuplicateId(MemberRegisterRequest request) {
        if (memberRepository.findMemberByLoginId(request.loginId()).isPresent()) {
            throw new MemberException("이미 등록된 사용자 입니다.");
        }
    }

    @Override
    public MemberDto getMemberByLoginId(String loginId) {
        var member = memberRepository.findMemberByLoginId(loginId);
        if (!member.isPresent()) {
            throw new MemberException("해당 아이디를 찾을 수 없습니다.");
        }
        MemberDto result = objectMapper.convertValue(member.get(), MemberDto.class);
        return result;
    }

    @Override
    public List<Member> getAllMembers() {
        return List.of();
    }

//    public Member login(String loginId){
//        return memberRepository.findMemberByLoginId(loginId).orElse(null);
//    }

    //@Transactional
//    public void saveMember(Member member) {
//        memberRepository.save(member);
//    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var member = memberRepository.findMemberByLoginId(username).orElseThrow( () -> new UsernameNotFoundException(username));

        return new User(member.getLoginId(), member.getPassword(),
                true, true, true, true,
                new ArrayList<>());
    }
}
