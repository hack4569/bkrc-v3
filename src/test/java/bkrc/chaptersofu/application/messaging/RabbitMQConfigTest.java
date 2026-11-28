package bkrc.chaptersofu.application.messaging;

import bkrc.chaptersofu.application.required.outbox.OutboxStatusUpdater;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * RabbitMQ 토폴로지(큐-익스체인지-DLX 바인딩)와 발행 확인 콜백의 Outbox 정리 규칙을 검증한다.
 * 실제 브로커 연결 없이 빈 팩토리 메서드를 직접 호출한다.
 */
class RabbitMQConfigTest {
    private final RabbitMQConfig config = new RabbitMQConfig();

    @Test
    void 업무_큐는_모두_Dead_Letter_Exchange로_연결된다() {
        // 소비 실패 메시지가 유실되지 않도록 모든 업무 큐에 DLX 설정이 있어야 한다.
        for (Queue queue : new Queue[]{config.joinQueue(), config.modifyQueue(), config.withdrawQueue(),
                config.likeQueue(), config.couponIssueQueue()}) {
            assertThat(queue.isDurable()).isTrue();
            assertThat(queue.getArguments())
                    .containsEntry("x-dead-letter-exchange", RabbitMQConfig.DEAD_DIRECT_EXCHANGE)
                    .containsEntry("x-dead-letter-routing-key", RabbitMQConfig.DEAD_LETTER_ROUTING_KEY);
        }
        assertThat(config.deadLetterQueue().getName()).isEqualTo(RabbitMQConfig.DLQ);
    }

    @Test
    void 큐는_라우팅_키로_올바른_익스체인지에_바인딩된다() {
        assertBinding(config.joinQueueBinding(), RabbitMQConfig.JOIN_QUEUE,
                RabbitMQConfig.NOTIFICATION_DIRECT_EXCHANGE, RabbitMQConfig.JOIN_ROUTING_KEY);
        assertBinding(config.modifyQueueBinding(), RabbitMQConfig.MODIFY_QUEUE,
                RabbitMQConfig.NOTIFICATION_DIRECT_EXCHANGE, RabbitMQConfig.MODIFY_ROUTING_KEY);
        assertBinding(config.withdrawQueueBinding(), RabbitMQConfig.WITHDRAW_QUEUE,
                RabbitMQConfig.NOTIFICATION_DIRECT_EXCHANGE, RabbitMQConfig.WITHDRAW_ROUTING_KEY);
        assertBinding(config.likeQueueBinding(), RabbitMQConfig.LIKE_QUEUE,
                RabbitMQConfig.HOTBOOK_DIRECT_EXCHANGE, RabbitMQConfig.LIKE_ROUTING_KEY);
        assertBinding(config.couponIssueQueueBinding(), RabbitMQConfig.COUPON_ISSUE_QUEUE,
                RabbitMQConfig.COUPON_DIRECT_EXCHANGE, RabbitMQConfig.COUPON_ISSUE_ROUTING_KEY);
        assertBinding(config.deadLetterQueueBinding(), RabbitMQConfig.DLQ,
                RabbitMQConfig.DEAD_DIRECT_EXCHANGE, RabbitMQConfig.DEAD_LETTER_ROUTING_KEY);
    }

    @Test
    void 브로커_ACK를_받은_Outbox만_삭제한다() {
        OutboxStatusUpdater updater = mock(OutboxStatusUpdater.class);
        RabbitTemplate template = config.rabbitTemplate(mock(ConnectionFactory.class), updater);
        // RabbitTemplate은 콜백 조회 API가 없으므로 등록된 콜백을 리플렉션으로 꺼내 직접 호출한다.
        RabbitTemplate.ConfirmCallback callback =
                (RabbitTemplate.ConfirmCallback) ReflectionTestUtils.getField(template, "confirmCallback");

        callback.confirm(new CorrelationData("1"), true, null);
        callback.confirm(new CorrelationData("2"), false, "nack");
        callback.confirm(null, true, null);

        // NACK는 재처리 스케줄러가 다시 발행해야 하므로 삭제하지 않는다.
        verify(updater).delete(1L);
        verify(updater, times(1)).delete(anyLong());
    }

    private void assertBinding(Binding binding, String queue, String exchange, String routingKey) {
        assertThat(binding.getDestination()).isEqualTo(queue);
        assertThat(binding.getExchange()).isEqualTo(exchange);
        assertThat(binding.getRoutingKey()).isEqualTo(routingKey);
    }
}
