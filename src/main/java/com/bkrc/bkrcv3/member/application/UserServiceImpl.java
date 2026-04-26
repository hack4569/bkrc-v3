package com.bkrc.bkrcv3.member.application;

import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.common.event.EventType;
import com.bkrc.bkrcv3.adapter.payload.MemberJoinEventPayload;
import com.bkrc.bkrcv3.adapter.payload.MemberModifyEventPayload;
import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.common.shared.Snowflake;
import com.bkrc.bkrcv3.config.RabbitMQConfig;
import com.bkrc.bkrcv3.exception.BusinessException;
import com.bkrc.bkrcv3.member.application.request.MemberModifyRequest;
import com.bkrc.bkrcv3.member.application.request.MemberRegisterRequest;
import com.bkrc.bkrcv3.member.dto.MemberDto;
import com.bkrc.bkrcv3.member.entity.Member;
import com.bkrc.bkrcv3.member.entity.PasswordEncoder;
import com.bkrc.bkrcv3.outbox.Outbox;
import com.bkrc.bkrcv3.outbox.OutboxEvent;
import com.bkrc.bkrcv3.outbox.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
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
    private final RabbitTemplate rabbitTemplate;
    private Snowflake snowflake = new Snowflake();

    @Override
    @Transactional
    public Member saveMember(MemberRegisterRequest request) {
        checkDuplicateId(request);
        checkPwd(request.password(), request.passwordCheck());

        var member = Member.register(snowflake.nextId(), request.loginId(), request.password(), passwordEncoder);
        var savedMember = memberRepository.save(member);

        Outbox outbox = outboxRepository.save(Outbox.of(
                EventType.MEMBER_JOIN,
                RabbitMQConfig.JOIN_ROUTING_KEY,
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
            throw new BusinessException(ErrorCode.USER_NOT_EQUALS_PW);
        }
    }

    private void checkDuplicateId(MemberRegisterRequest request) {
        if (memberRepository.findMemberByLoginId(request.loginId()).isPresent()) {
            throw new UserException("이미 등록된 사용자 입니다.", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public MemberDto getMemberByLoginId(String loginId) {
        var member = memberRepository.findMemberByLoginId(loginId);
        if (!member.isPresent()) {
            throw new UserException("해당 아이디를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST);
        }
        MemberDto result = objectMapper.convertValue(member.get(), MemberDto.class);
        return result;
    }

    @Override
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var member = memberRepository.findMemberByLoginId(username).orElseThrow( () -> new UsernameNotFoundException(username));

        return new User(member.getLoginId(), member.getPassword(),
                true, true, true, true,
                new ArrayList<>());
    }

    @Override
    @Transactional
    public Member modifyMember(String loginId, MemberModifyRequest request) {
        var member = memberRepository.findMemberByLoginId(loginId).orElseThrow( () -> new UsernameNotFoundException(loginId));
        if (!member.checkPassword(request.originPassword(), passwordEncoder)) {
            throw new UserException("비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
        this.checkPwd(request.newPassword(), request.newPasswordCheck());
        member.modify(request.newPassword(), passwordEncoder);
        var modifiedMember = memberRepository.save(member);
        Outbox outbox = outboxRepository.save(Outbox.of(
                EventType.MEMBER_MODIFY,
                RabbitMQConfig.MODIFY_ROUTING_KEY,
                Event.of(EventType.MEMBER_MODIFY,MemberModifyEventPayload.builder()
                        .loginId(modifiedMember.getLoginId())
                        .updated(modifiedMember.getUpdated())
                        .build()).toJson()
                )
        );
        eventPublisher.publishEvent(OutboxEvent.of(outbox));
        return modifiedMember;
    }

    public String encodePassword(String password) {
        return passwordEncoder.hashPassword(password);
    }


}
