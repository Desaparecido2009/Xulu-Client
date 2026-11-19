package com.elementars.eclient.module.misc;

import com.elementars.eclient.command.Command;
import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventReceivePacket;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.Wrapper;
import dev.xulu.settings.Value;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;

public class CoordLogger extends Module
{
    private final Value<Boolean> tp;
    private final Value<Boolean> lightning;
    private final Value<Boolean> portal;
    private final Value<Boolean> wither;
    private final Value<Boolean> dragon;
    private final Value<Boolean> savetofile;
    private final HashMap<Entity, Vec3> knownPlayers;

    public CoordLogger() {
        super("CoordLogger", "Logs coords taken from several exploits", 0, Category.MISC, true);
        this.tp = this.register(new Value<Boolean>("TpExploit", this, false));
        this.lightning = this.register(new Value<Boolean>("Thunder", this, false));
        this.portal = this.register(new Value<Boolean>("EndPortal", this, false));
        this.wither = this.register(new Value<Boolean>("Wither", this, false));
        this.dragon = this.register(new Value<Boolean>("Dragon", this, false));
        this.savetofile = this.register(new Value<Boolean>("SaveToFile", this, false));
        this.knownPlayers = new HashMap<Entity, Vec3>();
    }

    @EventTarget
    public void onPacket(final EventReceivePacket event) {
        if (this.lightning.getValue() && event.getPacket() instanceof ClientboundSoundPacket) {
            final ClientboundSoundPacket packet = (ClientboundSoundPacket)event.getPacket();
            if (packet.getSource() == SoundSource.WEATHER && packet.getSound() == SoundEvents.LIGHTNING_BOLT_THUNDER) {
                this.sendNotification(ChatFormatting.RED + "Lightning spawned at X" + packet.getX() + " Z" + packet.getZ());
            }
        }
        if (event.getPacket() instanceof ClientboundLevelEventPacket) {
            final ClientboundLevelEventPacket packet2 = (ClientboundLevelEventPacket)event.getPacket();
            if (this.portal.getValue() && packet2.getType() == 1038) {
                this.sendNotification(ChatFormatting.RED + "End Portal activated at X" + packet2.getPos().getX() + " Y" + packet2.getPos().getY() + " Z" + packet2.getPos().getZ());
            }
            if (this.wither.getValue() && packet2.getType() == 1023) {
                this.sendNotification(ChatFormatting.RED + "Wither spawned at X" + packet2.getPos().getX() + " Y" + packet2.getPos().getY() + " Z" + packet2.getPos().getZ());
            }
            if (this.dragon.getValue() && packet2.getType() == 1028) {
                this.sendNotification(ChatFormatting.RED + "Dragon killed at X" + packet2.getPos().getX() + " Y" + packet2.getPos().getY() + " Z" + packet2.getPos().getZ());
            }
        }
    }

    @Override
    public void onUpdate() {
        if (!this.tp.getValue()) {
            return;
        }
        if (CoordLogger.mc.player == null || CoordLogger.mc.level == null) {
            return;
        }
        for (final Player entity : CoordLogger.mc.level.players()) {
            if (entity != CoordLogger.mc.player) {
                final Vec3 targetPos = entity.position();
                if (this.knownPlayers.containsKey(entity)) {
                    if (CoordLogger.mc.player.position().distanceTo(targetPos) >= 128.0 && this.knownPlayers.get(entity).distanceTo(targetPos) >= 64.0) {
                        this.sendNotification(ChatFormatting.RED + "Player " + entity.getName().getString() + " moved to Position " + targetPos.toString());
                    }
                    this.knownPlayers.put(entity, targetPos);
                }
                else {
                    this.knownPlayers.put(entity, targetPos);
                }
            }
        }
    }

    private void sendNotification(final String s) {
        Command.sendChatMessage(s);
        if (this.savetofile.getValue()) {
            Wrapper.getFileManager().appendTextFile(s, "CoordLogger.txt");
        }
    }
}
