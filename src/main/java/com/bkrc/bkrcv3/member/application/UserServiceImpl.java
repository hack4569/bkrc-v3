package com.bkrc.bkrcv3.member.application;

import com.bkrc.bkrcv3.common.dataserializer.DataSerializer;
import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.common.event.EventType;
import com.bkrc.bkrcv3.common.event.payload.MemberJoinEventPayload;
import com.bkrc.bkrcv3.common.event.payload.MemberModifyEventPayload;
import com.bkrc.bkrcv3.member.application.request.MemberModifyRequest;
import com.bkrc.bkrcv3.member.application.request.MemberRegisterRequest;
import com.bkrc.bkrcv3.member.dto.MemberDto;
import com.bkrc.bkrcv3.member.entity.Member;
import com.bkrc.bkrcv3.member.entity.MemberException;
import com.bkrc.bkrcv3.member.entity.PasswordEncoder;
import com.bkrc.bkrcv3.outbox.outbox.Outbox;
import com.bkrc.bkrcv3.outbox.outbox.OutboxEvent;
import com.bkrc.bkrcv3.outbox.outbox.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository; // 추가
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Member saveMember(MemberRegisterRequest request) {
        checkDuplicateId(request);
        checkPwd(request.password(), request.passwordCheck());

        var member = Member.register(request.loginId(), request.password(), passwordEncoder);
        var savedMember = memberRepository.save(member);

        Outbox outbox = outboxRepository.save(Outbox.of(
                EventType.MEMBER_JOIN,
                String.valueOf(savedMember.getLoginId()),
                Event.of(EventType.MEMBER_JOIN,MemberJoinEventPayload.builder()
                        .loginId(savedMember.getLoginId())
                        .created(savedMember.getCreated())
                        .build()).toJson()
        ));

        // 트랜잭션 커밋 후 이벤트 발행
        eventPublisher.publishEvent(OutboxEvent.of(outbox));
        return savedMember;
    }

    private void checkPwd(String pwd, String pwdChk) {
        if (!pwd.equals(pwdChk)) {
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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var member = memberRepository.findMemberByLoginId(username).orElseThrow( () -> new UsernameNotFoundException(username));

        return new User(member.getLoginId(), member.getPassword(),
                true, true, true, true,
                new ArrayList<>());
    }

    @Override
    public Member modifyMember(String loginId, MemberModifyRequest request) {
        var member = memberRepository.findMemberByLoginId(loginId).orElseThrow( () -> new UsernameNotFoundException(loginId));
        if (!member.checkPassword(request.originPassword(), passwordEncoder)) {
            throw new MemberException("비밀번호가 일치하지 않습니다.");
        }
        this.checkPwd(request.newPassword(), request.newPasswordCheck());
        var updatedMember = Member.register(request.loginId(), request.newPassword(), passwordEncoder);
        var modifiedMember = memberRepository.save(updatedMember);
        outboxRepository.save(Outbox.of(
                EventType.MEMBER_MODIFY,
                modifiedMember.getLoginId(),
                DataSerializer.serialize(
                        MemberModifyEventPayload.builder()
                                .loginId(modifiedMember.getLoginId())
                                .updated(modifiedMember.getUpdated())
                                .build()
                )
        ));
        return modifiedMember;
    }

    public String encodePassword(String password) {
        return passwordEncoder.hashPassword(password);
    }


}
