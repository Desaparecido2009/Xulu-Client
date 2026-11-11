package com.elementars.eclient.event.events;

import com.elementars.eclient.event.Event;
import net.minecraft.world.entity.Entity;

public class EventAttackEntity extends Event
{
    private final Entity target;

    public EventAttackEntity(final Entity target) {
        this.target = target;
    }

    public Entity getTarget() {
        return this.target;
    }
}
