package com.elementars.eclient.module.player;

import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventSendPacket;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;

import java.util.LinkedList;
import java.util.Queue;

public class Blink extends Module
{
    private final Queue<ServerboundMovePlayerPacket> packets;
    private RemotePlayer clonedPlayer;

    public Blink() {
        super("Blink", "Hides movement for a short distance", 0, Category.PLAYER, true);
        this.packets = new LinkedList<ServerboundMovePlayerPacket>();
    }

    @EventTarget
    public void onPacket(final EventSendPacket event) {
        if (this.isToggled() && event.getPacket() instanceof ServerboundMovePlayerPacket) {
            event.setCancelled(true);
            this.packets.add((ServerboundMovePlayerPacket)event.getPacket());
        }
    }

    @Override
    public void onEnable() {
        if (Blink.mc.player == null || Blink.mc.level == null) {
            return;
        }
        final ClientLevel level = Blink.mc.level;
        this.clonedPlayer = new RemotePlayer(level, Blink.mc.getUser().getGameProfile());
        this.clonedPlayer.moveTo(Blink.mc.player.getX(), Blink.mc.player.getY(), Blink.mc.player.getZ(), Blink.mc.player.getYRot(), Blink.mc.player.getXRot());
        this.clonedPlayer.setYHeadRot(Blink.mc.player.getYHeadRot());
        this.clonedPlayer.setOldPosAndRot();
        level.addPlayer(-100, this.clonedPlayer);
    }

    @Override
    public void onDisable() {
        if (Blink.mc.getConnection() != null) {
            while (!this.packets.isEmpty()) {
                Blink.mc.getConnection().send(this.packets.poll());
            }
        }
        this.packets.clear();
        if (Blink.mc.level != null) {
            Blink.mc.level.removeEntity(-100, Entity.RemovalReason.DISCARDED);
        }
        this.clonedPlayer = null;
    }

    @Override
    public String getHudInfo() {
        return String.valueOf(this.packets.size());
    }
}
