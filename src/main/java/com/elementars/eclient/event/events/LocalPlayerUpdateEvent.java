package com.elementars.eclient.event.events;

import com.elementars.eclient.event.Event;
import net.minecraft.world.entity.LivingEntity;

public class LocalPlayerUpdateEvent extends Event
{
    LivingEntity entityLivingBase;

    public LocalPlayerUpdateEvent(final LivingEntity entity) {
        this.entityLivingBase = entity;
    }

    public LivingEntity getEntityLivingBase() {
        return this.entityLivingBase;
    }
}
