package com.bkrc.bkrcv3.common.event;

import com.bkrc.bkrcv3.common.dataserializer.DataSerializer;
import com.bkrc.bkrcv3.required.EventPayload;
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

    public static Event<EventPayload> fromJson(String json) {
        EventRaw eventRaw = DataSerializer.deserialize(json, EventRaw.class);
        if (eventRaw == null) {
            return null;
        }
        Event<EventPayload> event = new Event<>();
        event.type = EventType.from(eventRaw.getType());
        event.payload = DataSerializer.deserialize(eventRaw.getPayload(), event.type.getPayloadClass());
        return event;
    }
    @Getter
    private static class EventRaw {
        private String type;
        private Object payload;
    }
}
