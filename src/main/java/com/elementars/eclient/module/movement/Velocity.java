package com.elementars.eclient.module.movement;

import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EntityEvent;
import com.elementars.eclient.event.events.EventReceivePacket;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;

public class Velocity extends Module
{
    private final Value<Float> horizontal;
    private final Value<Float> vertical;

    public Velocity() {
        super("Velocity", "Modifies knockback", 0, Category.MOVEMENT, true);
        this.horizontal = this.register(new Value<Float>("Horizontal", this, 0.0f, 0.0f, 100.0f));
        this.vertical = this.register(new Value<Float>("Vertical", this, 0.0f, 0.0f, 100.0f));
    }

    @EventTarget
    public void onPacket(final EventReceivePacket event) {
        if (Velocity.mc.player == null) {
            return;
        }
        if (event.getPacket() instanceof ClientboundSetEntityMotionPacket) {
            final ClientboundSetEntityMotionPacket velocity = (ClientboundSetEntityMotionPacket)event.getPacket();
            if (velocity.getId() == Velocity.mc.player.getId()) {
                if (this.horizontal.getValue() == 0.0f && this.vertical.getValue() == 0.0f) {
                    event.setCancelled(true);
                }
                final int h = (int)(float)this.horizontal.getValue();
                final int v = (int)(float)this.vertical.getValue();
                event.setPacket(new ClientboundSetEntityMotionPacket(velocity.getId(), new Vec3(velocity.getXa() * h / 8000.0, velocity.getYa() * v / 8000.0, velocity.getZa() * h / 8000.0)));
            }
        }
        else if (event.getPacket() instanceof ClientboundExplodePacket) {
            if (this.horizontal.getValue() == 0.0f && this.vertical.getValue() == 0.0f) {
                event.setCancelled(true);
            }
            final ClientboundExplodePacket explosion = (ClientboundExplodePacket)event.getPacket();
            final float knockbackX = explosion.getKnockbackX() * this.horizontal.getValue();
            final float knockbackY = explosion.getKnockbackY() * this.vertical.getValue();
            final float knockbackZ = explosion.getKnockbackZ() * this.horizontal.getValue();
            final Vec3 knockback = (knockbackX == 0.0f && knockbackY == 0.0f && knockbackZ == 0.0f) ? null : new Vec3(knockbackX, knockbackY, knockbackZ);
            event.setPacket(new ClientboundExplodePacket(explosion.getX(), explosion.getY(), explosion.getZ(), explosion.getPower(), explosion.getToBlow(), knockback));
        }
    }

    @EventTarget
    public void onEntityCollision(final EntityEvent.EntityCollision event) {
        if (event.getEntity() == Velocity.mc.player) {
            if (this.horizontal.getValue() == 0.0f && this.vertical.getValue() == 0.0f) {
                event.setCancelled(true);
                return;
            }
            event.setX(-event.getX() * this.horizontal.getValue());
            event.setY(0.0);
            event.setZ(-event.getZ() * this.horizontal.getValue());
        }
    }
}
