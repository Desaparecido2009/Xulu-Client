package com.elementars.eclient.event.events;

import com.elementars.eclient.event.Event;
import net.minecraft.network.protocol.Packet;

public class EventSendPacket extends Event
{
    private Packet<?> packet;

    public EventSendPacket(final Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return this.packet;
    }

    public void setPacket(final Packet<?> packet) {
        this.packet = packet;
    }
}
