package com.elementars.eclient.event.events;

import com.elementars.eclient.event.Event;

import java.util.UUID;

public class EventPlayerConnect extends Event
{
    private final UUID uuid;
    private final String name;

    public EventPlayerConnect(final UUID uuid, final String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public static class Join extends EventPlayerConnect
    {
        public Join(final UUID uuid, final String name) {
            super(uuid, name);
        }
    }

    public static class Leave extends EventPlayerConnect
    {
        public Leave(final UUID uuid, final String name) {
            super(uuid, name);
        }
    }
}
