package bkrc.chaptersofu.application.outbox;

public interface OutboxPublisher {
    void publish(Outbox outbox);
}
