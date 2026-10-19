package com.elementars.eclient.module.player;

import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventSendPacket;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.phys.AABB;

public class NoBreakAnimation extends Module
{
    private boolean isMining;
    private BlockPos lastPos;
    private Direction lastFacing;

    public NoBreakAnimation() {
        super("NoBreakAnimation", "Prevents block break animation server side", 0, Category.PLAYER, true);
        this.isMining = false;
        this.lastPos = null;
        this.lastFacing = null;
    }

    @EventTarget
    public void onSend(final EventSendPacket event) {
        if (event.getPacket() instanceof ServerboundPlayerActionPacket) {
            final ServerboundPlayerActionPacket packet = (ServerboundPlayerActionPacket)event.getPacket();
            for (final Entity entity : NoBreakAnimation.mc.level.getEntities((Entity)null, new AABB(packet.getPos()))) {
                if (entity instanceof EndCrystal) {
                    this.resetMining();
                    return;
                }
                if (entity instanceof LivingEntity) {
                    this.resetMining();
                    return;
                }
            }
            if (packet.getAction().equals(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK)) {
                this.isMining = true;
                this.setMiningInfo(packet.getPos(), packet.getDirection());
            }
            if (packet.getAction().equals(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK)) {
                this.resetMining();
            }
        }
    }

    @Override
    public void onUpdate() {
        if (!NoBreakAnimation.mc.options.keyAttack.isDown()) {
            this.resetMining();
            return;
        }
        if (this.isMining && this.lastPos != null && this.lastFacing != null) {
            NoBreakAnimation.mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.lastPos, this.lastFacing));
        }
    }

    private void setMiningInfo(final BlockPos lastPos, final Direction lastFacing) {
        this.lastPos = lastPos;
        this.lastFacing = lastFacing;
    }

    public void resetMining() {
        this.isMining = false;
        this.lastPos = null;
        this.lastFacing = null;
    }
}
