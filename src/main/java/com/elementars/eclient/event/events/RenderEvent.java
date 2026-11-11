package com.elementars.eclient.event.events;

import com.elementars.eclient.event.Event;
import com.elementars.eclient.util.XuluTessellator;
import net.minecraft.world.phys.Vec3;

public class RenderEvent extends Event
{
    private final XuluTessellator tessellator;
    private final Vec3 renderPos;

    public RenderEvent(final XuluTessellator tessellator, final Vec3 renderPos) {
        this.tessellator = tessellator;
        this.renderPos = renderPos;
    }

    public XuluTessellator getTessellator() {
        return this.tessellator;
    }

    public Vec3 getRenderPos() {
        return this.renderPos;
    }

    public void resetTranslation() {
        this.tessellator.setTranslation(-this.renderPos.x, -this.renderPos.y, -this.renderPos.z);
    }
}
