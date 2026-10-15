package bkrc.chaptersofu.application.required.event;

import bkrc.chaptersofu.domain.common.shared.DataSerializer;
import lombok.Getter;

@Getter
public class Event<T extends EventPayload> {
    private EventType type;
    private T payload;

    public static Event<EventPayload> of(EventType type, EventPayload payload) {
        Event<EventPayload> event = new Event<>();
        event.type = type;
        event.payload = payload;
        return event;
    }

    public String toJson() {
        return DataSerializer.serialize(this);
    }

    public static EventType typeFromJson(String json) {
        EventRaw eventRaw = DataSerializer.deserialize(json, EventRaw.class);
        if (eventRaw == null) {
            return null;
        }
        return EventType.from(eventRaw.getType());
    }

    public static Event<EventPayload> fromJson(
            String json,
            Class<? extends EventPayload> payloadClass
    ) {
        EventRaw eventRaw = DataSerializer.deserialize(json, EventRaw.class);
        if (eventRaw == null) {
            return null;
        }
        Event<EventPayload> event = new Event<>();
        event.type = EventType.from(eventRaw.getType());
        event.payload = DataSerializer.deserialize(eventRaw.getPayload(), payloadClass);
        return event;
    }
    @Getter
    private static class EventRaw {
        private String type;
        private Object payload;
    }
}
