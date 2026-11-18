package com.elementars.eclient.module.render;

import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventReceivePacket;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import net.minecraft.network.protocol.game.ClientboundAddPaintingPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class NoRender extends Module
{
    private final Value<Boolean> mob;
    private final Value<Boolean> gentity;
    public final Value<Boolean> armor;
    public final Value<Boolean> armorTrans;
    public final Value<Integer> alpha;
    private final Value<Boolean> object;
    private final Value<Boolean> xp;
    private final Value<Boolean> paint;
    private final Value<Boolean> fire;

    public NoRender() {
        super("NoRender", "Prevents rendering of certain things", 0, Category.RENDER, true);
        this.mob = this.register(new Value<Boolean>("Mob", this, false));
        this.gentity = this.register(new Value<Boolean>("GEntity", this, false));
        this.armor = this.register(new Value<Boolean>("Armor", this, false));
        this.armorTrans = this.register(new Value<Boolean>("Armor Transparency", this, false));
        this.alpha = this.register(new Value<Integer>("Transparency", this, 255, 0, 255));
        this.object = this.register(new Value<Boolean>("Object", this, false));
        this.xp = this.register(new Value<Boolean>("XP", this, false));
        this.paint = this.register(new Value<Boolean>("Paintings", this, false));
        this.fire = this.register(new Value<Boolean>("Fire", this, true));
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @EventTarget
    public void onPacket(final EventReceivePacket event) {
        final Packet<?> packet = event.getPacket();
        if (packet instanceof ClientboundAddMobPacket && this.mob.getValue()) {
            event.setCancelled(true);
            return;
        }
        if (packet instanceof ClientboundAddExperienceOrbPacket && this.xp.getValue()) {
            event.setCancelled(true);
            return;
        }
        if (packet instanceof ClientboundAddPaintingPacket && this.paint.getValue()) {
            event.setCancelled(true);
            return;
        }
        if (packet instanceof ClientboundAddEntityPacket) {
            final ClientboundAddEntityPacket addEntity = (ClientboundAddEntityPacket)packet;
            if (addEntity.getType() == EntityType.LIGHTNING_BOLT) {
                if (this.gentity.getValue()) {
                    event.setCancelled(true);
                }
            }
            else if (this.object.getValue()) {
                event.setCancelled(true);
            }
        }
    }

    @SubscribeEvent
    public void onBlockOverlay(final RenderBlockOverlayEvent event) {
        if (this.fire.getValue() && event.getOverlayType() == RenderBlockOverlayEvent.OverlayType.FIRE) {
            event.setCanceled(true);
        }
    }
}
