package com.elementars.eclient.event.events;

import com.elementars.eclient.event.Event;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class EventPlayerDamageBlock extends Event
{
    private final BlockPos pos;
    private final Direction facing;

    public EventPlayerDamageBlock(final BlockPos pos, final Direction facing) {
        this.pos = pos;
        this.facing = facing;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Direction getFacing() {
        return this.facing;
    }
}
