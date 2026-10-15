package bkrc.chaptersofu.application.required.outbox;

public interface OutboxPublisher {
    void publish(Outbox outbox);
}
