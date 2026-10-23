package bkrc.chaptersofu.application.outbox;

import bkrc.chaptersofu.application.required.event.Event;
import bkrc.chaptersofu.application.required.event.EventPayload;
import bkrc.chaptersofu.application.required.event.EventType;
import bkrc.chaptersofu.application.required.event.payload.MemberJoinEventPayload;
import bkrc.chaptersofu.application.required.outbox.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DataJpaTest
@Import(OutboxEventRecorder.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class OutboxEventRecorderTest {
    @Autowired OutboxEventRecorder recorder;
    @Autowired OutboxRepository repository;
    @Autowired PlatformTransactionManager transactionManager;
    @MockitoBean ApplicationEventPublisher publisher;

    private EventPayload payload() {
        return MemberJoinEventPayload.builder().loginId("outbox-test").build();
    }

    @Test
    void 호출자_트랜잭션_없이는_기록하지_않는다() {
        assertThatThrownBy(() -> recorder.record(EventType.MEMBER_JOIN, "exchange", "route", payload()))
                .isInstanceOf(IllegalTransactionStateException.class);
        verifyNoInteractions(publisher);
    }

    @Test
    void 이벤트를_직렬화하고_저장한_Outbox로_내부_이벤트를_발행한다() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            EventPayload payload = payload();
            Outbox outbox = recorder.record(EventType.MEMBER_JOIN, "exchange", "route", payload);
            assertThat(repository.findById(outbox.getOutboxId())).isPresent();
            assertThat(outbox.getEventType()).isEqualTo(EventType.MEMBER_JOIN);
            assertThat(outbox.getExchange()).isEqualTo("exchange");
            assertThat(outbox.getRoutingKey()).isEqualTo("route");
            assertThat(outbox.getPayload()).isEqualTo(Event.of(EventType.MEMBER_JOIN, payload).toJson());
            assertThat(outbox.getOutboxStatus()).isEqualTo(OutboxStatus.PENDING);
            verify(publisher).publishEvent(argThat((Object event) ->
                    event instanceof OutboxEvent value && value.getOutbox() == outbox));
            status.setRollbackOnly();
        });
    }

    @Test
    void 호출자_롤백으로_Outbox도_롤백된다() {
        Outbox outbox = new TransactionTemplate(transactionManager).execute(status -> {
            Outbox saved = recorder.record(EventType.MEMBER_JOIN, "exchange", "route", payload());
            status.setRollbackOnly();
            return saved;
        });
        assertThat(repository.findById(outbox.getOutboxId())).isEmpty();
    }

    @Test
    void 내부_발행_실패시_Outbox_저장도_롤백된다() {
        long before = repository.count();
        doThrow(new IllegalStateException("publication failed")).when(publisher).publishEvent(any(Object.class));
        assertThatThrownBy(() -> new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                recorder.record(EventType.MEMBER_JOIN, "exchange", "route", payload())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(repository.count()).isEqualTo(before);
    }
}
