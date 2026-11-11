package com.elementars.eclient.event.events;

import com.elementars.eclient.event.Event;
import net.minecraft.world.entity.player.Player;

public class EventTotemPop extends Event
{
    private final Player player;

    public EventTotemPop(final Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }
}
