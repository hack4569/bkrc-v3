package com.bkrc.bkrcv3.member.application;

import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.common.shared.Snowflake;
import com.bkrc.bkrcv3.exception.MemberNotFoundException;
import com.bkrc.bkrcv3.member.application.provided.MemberRegister;
import com.bkrc.bkrcv3.member.application.request.MemberModifyRequest;
import com.bkrc.bkrcv3.member.application.request.MemberRegisterRequest;
import com.bkrc.bkrcv3.member.application.request.MemberWithdrawRequest;
import com.bkrc.bkrcv3.member.domain.DuplicateMemberException;
import com.bkrc.bkrcv3.member.domain.Member;
import com.bkrc.bkrcv3.member.domain.PasswordEncoder;
import com.bkrc.bkrcv3.member.domain.PasswordNotEquals;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@Validated
@RequiredArgsConstructor
public class MemberModifyService implements MemberRegister {
    private final MemberRepository memberRepository;
    private final Snowflake snowflake;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Member saveMember(MemberRegisterRequest request) {
        checkDuplicateId(request);
        checkPwd(request.password(), request.passwordCheck());

        var member = Member.register(snowflake.nextId(), request.loginId(), request.password(), passwordEncoder);
        var savedMember = memberRepository.save(member);
//        Outbox outbox = outboxRepository.save(Outbox.of(
//                EventType.MEMBER_JOIN,
//                RabbitMQConfig.NOTIFICATION_DIRECT_EXCHANGE,
//                RabbitMQConfig.JOIN_ROUTING_KEY,
//                Event.of(EventType.MEMBER_JOIN, MemberJoinEventPayload.builder()
//                        .loginId(savedMember.getLoginId())
//                        .created(savedMember.getCreated())
//                        .build()).toJson()
//        ));
//
//        // 트랜잭션 커밋 후 이벤트 발행
//        eventPublisher.publishEvent(OutboxEvent.of(outbox));
        return savedMember;
    }

    @Override
    public Member modifyMember(Long memberId, MemberModifyRequest request) {
        var member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        if (!member.checkPassword(request.originPassword(), passwordEncoder)) {
            throw new PasswordNotEquals(ErrorCode.USER_NOT_EQUALS_PW);
        }
        this.checkPwd(request.newPassword(), request.newPasswordCheck());
        member.modify(request.newPassword(), passwordEncoder);
        var modifiedMember = memberRepository.save(member);
//        Outbox outbox = outboxRepository.save(Outbox.of(
//                        EventType.MEMBER_MODIFY,
//                        RabbitMQConfig.NOTIFICATION_DIRECT_EXCHANGE,
//                        RabbitMQConfig.MODIFY_ROUTING_KEY,
//                        Event.of(EventType.MEMBER_MODIFY, MemberModifyEventPayload.builder()
//                                .loginId(modifiedMember.getLoginId())
//                                .updated(modifiedMember.getUpdated())
//                                .build()).toJson()
//                )
//        );
//        eventPublisher.publishEvent(OutboxEvent.of(outbox));
        return modifiedMember;
    }

    @Override
    public void withdrawMember(Long memberId, MemberWithdrawRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        if (!member.checkPassword(request.password(), passwordEncoder)) {
            throw new PasswordNotEquals(ErrorCode.USER_NOT_EQUALS_PW);
        }
        //historyRepository.deleteByMemberId(memberId);
        memberRepository.deleteById(memberId);

//        Outbox outbox = outboxRepository.save(Outbox.of(
//                EventType.MEMBER_WITHDRAW,
//                RabbitMQConfig.NOTIFICATION_DIRECT_EXCHANGE,
//                RabbitMQConfig.WITHDRAW_ROUTING_KEY,
//                Event.of(EventType.MEMBER_WITHDRAW, MemberWithdrawEventPayload.builder()
//                        .loginId(member.getLoginId())
//                        .withdrawnAt(java.time.LocalDateTime.now())
//                        .build()).toJson()
//        ));
//        eventPublisher.publishEvent(OutboxEvent.of(outbox));
    }

    private void checkPwd(String pwd, String pwdChk) {
        if (!pwd.equals(pwdChk)) {
            throw new PasswordNotEquals(ErrorCode.USER_NOT_EQUALS_PW);
        }
    }

    private void checkDuplicateId(MemberRegisterRequest request) {
        if (memberRepository.findMemberByLoginId(request.loginId()).isPresent()) {
            throw new DuplicateMemberException(ErrorCode.USER_ALREADY_EXISTS);
        }
    }
}
