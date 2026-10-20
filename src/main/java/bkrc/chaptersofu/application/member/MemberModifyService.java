package bkrc.chaptersofu.application.member;

import bkrc.chaptersofu.application.messaging.RabbitMQConfig;
import bkrc.chaptersofu.application.required.event.Event;
import bkrc.chaptersofu.application.required.event.EventType;
import bkrc.chaptersofu.application.required.event.payload.MemberJoinEventPayload;
import bkrc.chaptersofu.application.required.outbox.Outbox;
import bkrc.chaptersofu.application.required.outbox.OutboxEvent;
import bkrc.chaptersofu.application.required.outbox.OutboxRepository;
import bkrc.chaptersofu.application.support.AfterCommitExecutor;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.common.shared.Snowflake;
import bkrc.chaptersofu.domain.member.MemberNotFoundException;
import bkrc.chaptersofu.application.member.provided.MemberRegister;
import bkrc.chaptersofu.application.member.provided.MemberModifyRequest;
import bkrc.chaptersofu.application.member.provided.MemberRegisterRequest;
import bkrc.chaptersofu.application.member.provided.MemberWithdrawRequest;
import bkrc.chaptersofu.domain.member.DuplicateMemberException;
import bkrc.chaptersofu.domain.member.Member;
import bkrc.chaptersofu.domain.member.PasswordEncoder;
import bkrc.chaptersofu.domain.member.PasswordNotEquals;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@Validated
@RequiredArgsConstructor
@Slf4j
public class MemberModifyService implements MemberRegister {
    private final MemberRepository memberRepository;
    private final Snowflake snowflake;
    private final PasswordEncoder passwordEncoder;
    private final OutboxRepository outboxRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Member saveMember(MemberRegisterRequest request) {
        checkDuplicateId(request);
        checkPwd(request.password(), request.passwordCheck());

        var member = Member.register(snowflake.nextId(), request.loginId(), request.password(), passwordEncoder);
        var savedMember = memberRepository.save(member);
        Outbox outbox = outboxRepository.save(Outbox.of(
                EventType.MEMBER_JOIN,
                RabbitMQConfig.NOTIFICATION_DIRECT_EXCHANGE,
                RabbitMQConfig.JOIN_ROUTING_KEY,
                Event.of(EventType.MEMBER_JOIN, MemberJoinEventPayload.builder()
                        .loginId(savedMember.getLoginId())
                        .created(savedMember.getCreated())
                        .build()).toJson()
        ));

        // 트랜잭션 커밋 후 이벤트 발행
        eventPublisher.publishEvent(OutboxEvent.of(outbox));
        // 회원과 Outbox가 모두 저장된 경우에만 가입 성공으로 기록합니다.
        AfterCommitExecutor.execute(() -> log.info(
                "MEMBER_REGISTERED memberId={} loginId={} outboxId={}",
                savedMember.getMemberId(), savedMember.getLoginId(), outbox.getOutboxId()
        ));
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
        // 비밀번호 자체는 기록하지 않고 변경된 회원 식별자만 남깁니다.
        AfterCommitExecutor.execute(() -> log.info("MEMBER_MODIFIED memberId={}", memberId));
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
        // 삭제 트랜잭션이 커밋된 이후에만 탈퇴 완료 로그를 남깁니다.
        AfterCommitExecutor.execute(() -> log.info("MEMBER_WITHDRAWN memberId={}", memberId));

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
