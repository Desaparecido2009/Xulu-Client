package com.elementars.eclient.module.movement;

import com.elementars.eclient.event.Event;
import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EntityEvent;
import com.elementars.eclient.event.events.EventReceivePacket;
import com.elementars.eclient.event.events.PlayerMoveEvent;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

public class Velocity extends Module
{
    public static Velocity INSTANCE;
    private final Value<Boolean> noPush;
    private final Value<Float> horizontal;
    private final Value<Float> vertical;

    public Velocity() {
        super("Velocity", "Modifies knockback", 0, Category.MOVEMENT, true);
        this.horizontal = this.register(new Value<Float>("Horizontal", this, 0.0f, 0.0f, 100.0f));
        this.vertical = this.register(new Value<Float>("Vertical", this, 0.0f, 0.0f, 100.0f));
        this.noPush = this.register(new Value<Boolean>("No Push", this, true));
        Velocity.INSTANCE = this;
    }

    public static boolean shouldCancelPush() {
        return Velocity.INSTANCE != null && Velocity.INSTANCE.isToggled() && Velocity.INSTANCE.noPush.getValue();
    }

    public static boolean shouldCancelAll() {
        return Velocity.INSTANCE != null && Velocity.INSTANCE.isToggled()
                && Velocity.INSTANCE.horizontal.getValue() <= 0.0f
                && Velocity.INSTANCE.vertical.getValue() <= 0.0f;
    }

    private boolean fullyCancelled() {
        return this.horizontal.getValue() <= 0.0f && this.vertical.getValue() <= 0.0f;
    }

    private double horizontalScale() {
        return this.horizontal.getValue() / 100.0;
    }

    private double verticalScale() {
        return this.vertical.getValue() / 100.0;
    }

    @EventTarget
    public void onPacket(final EventReceivePacket event) {
        if (Velocity.mc.player == null) {
            return;
        }
        if (event.getPacket() instanceof ClientboundSetEntityMotionPacket) {
            final ClientboundSetEntityMotionPacket velocity = (ClientboundSetEntityMotionPacket)event.getPacket();
            if (velocity.getId() != Velocity.mc.player.getId()) {
                return;
            }
            if (this.fullyCancelled()) {
                event.setCancelled(true);
                return;
            }
            event.setPacket(new ClientboundSetEntityMotionPacket(velocity.getId(), new Vec3(
                    velocity.getXa() / 8000.0 * this.horizontalScale(),
                    velocity.getYa() / 8000.0 * this.verticalScale(),
                    velocity.getZa() / 8000.0 * this.horizontalScale())));
        }
        else if (event.getPacket() instanceof ClientboundExplodePacket) {
            final ClientboundExplodePacket explosion = (ClientboundExplodePacket)event.getPacket();
            final Vec3 knockback = new Vec3(
                    explosion.getKnockbackX() * this.horizontalScale(),
                    explosion.getKnockbackY() * this.verticalScale(),
                    explosion.getKnockbackZ() * this.horizontalScale());
            event.setPacket(new ClientboundExplodePacket(explosion.getX(), explosion.getY(), explosion.getZ(),
                    explosion.getPower(), explosion.getToBlow(), knockback));
        }
    }

    @EventTarget
    public void onEntityCollision(final EntityEvent.EntityCollision event) {
        if (event.getEntity() != Velocity.mc.player) {
            return;
        }
        if (this.fullyCancelled()) {
            event.setCancelled(true);
            return;
        }
        event.setX(event.getX() * this.horizontalScale());
        event.setY(event.getY() * this.verticalScale());
        event.setZ(event.getZ() * this.horizontalScale());
    }

    @EventTarget
    public void onMove(final PlayerMoveEvent event) {
        if (event.getEventState() != Event.State.PRE) {
            return;
        }
        final MoverType type = event.getType();
        if (type != MoverType.PISTON && type != MoverType.SHULKER_BOX && type != MoverType.SHULKER) {
            return;
        }
        if (this.fullyCancelled()) {
            event.setX(0.0);
            event.setY(0.0);
            event.setZ(0.0);
            event.setCancelled(true);
            return;
        }
        event.setX(event.getX() * this.horizontalScale());
        event.setY(event.getY() * this.verticalScale());
        event.setZ(event.getZ() * this.horizontalScale());
    }
}
