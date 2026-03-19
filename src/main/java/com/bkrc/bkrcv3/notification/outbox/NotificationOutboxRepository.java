package com.bkrc.bkrcv3.notification.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {
    List<NotificationOutbox> findByStatus(NotificationOutbox.OutboxStatus status);
}
