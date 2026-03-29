package com.bkrc.bkrcv3.notification.outbox;

import com.bkrc.bkrcv3.common.event.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {
    List<NotificationOutbox> findByStatus(NotificationOutbox.OutboxStatus status);
}