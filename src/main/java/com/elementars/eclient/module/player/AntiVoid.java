package com.elementars.eclient.module.player;

import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventReceivePacket;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AntiVoid extends Module
{
    private final Value<String> mode;
    private final Value<Integer> y_level;
    public Set<String> ipList;
    public static AntiVoid INSTANCE;
    private boolean wasElytraFlying;
    int delay;

    public AntiVoid() {
        super("AntiVoid", "Prevents death from afk flying", 0, Category.PLAYER, true);
        this.mode = this.register(new Value<String>("Mode", this, "Normal", new String[] { "AFK Elytra", "Normal" }));
        this.y_level = this.register(new Value<Integer>("Y Level", this, 100, 0, 256));
        this.ipList = new HashSet<String>();
        AntiVoid.INSTANCE = this;
    }

    @Override
    public void onEnable() {
        AntiVoid.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        AntiVoid.EVENT_BUS.unregister(this);
    }

    private boolean isOverVoid() {
        return AntiVoid.mc.level.getBlockState(new BlockPos((int)AntiVoid.mc.player.getX(), 0, (int)AntiVoid.mc.player.getZ())).getBlock() == Blocks.AIR;
    }

    @Override
    public void onUpdate() {
        if (AntiVoid.mc.player != null && AntiVoid.mc.level != null) {
            if (this.mode.getValue().equalsIgnoreCase("Normal")) {
                boolean isVoid = true;
                for (int i = (int)AntiVoid.mc.player.getY(); i > -1; --i) {
                    if (AntiVoid.mc.level.getBlockState(new BlockPos((int)AntiVoid.mc.player.getX(), i, (int)AntiVoid.mc.player.getZ())).getBlock() != Blocks.AIR) {
                        isVoid = false;
                        break;
                    }
                }
                if (AntiVoid.mc.player.getY() < this.y_level.getValue() && isVoid) {
                    AntiVoid.mc.player.setDeltaMovement(AntiVoid.mc.player.getDeltaMovement().x, 0.0, AntiVoid.mc.player.getDeltaMovement().z);
                }
            }
            else if (AntiVoid.mc.player.isFallFlying()) {
                this.wasElytraFlying = true;
            }
            else if (AntiVoid.mc.player.getY() < this.y_level.getValue() && this.wasElytraFlying && this.isOverVoid()) {
                AntiVoid.mc.level.disconnect();
                this.wasElytraFlying = false;
            }
        }
    }

    @EventTarget
    public void onPacket(final EventReceivePacket event) {
        if (event.getPacket() instanceof ClientboundDisconnectPacket && AntiVoid.mc.getCurrentServer() != null && AntiVoid.mc.player != null) {
            this.ipList.add(AntiVoid.mc.getCurrentServer().ip);
        }
    }

    @SubscribeEvent
    public void onDisconnect(final ClientPlayerNetworkEvent.LoggedOutEvent event) {
        if (AntiVoid.mc.getCurrentServer() != null && AntiVoid.mc.player != null) {
            this.ipList.add(AntiVoid.mc.getCurrentServer().ip);
        }
    }

    @SubscribeEvent
    public void onConnect(final ClientPlayerNetworkEvent.LoggedInEvent event) {
        if (AntiVoid.mc.getCurrentServer() != null) {
            this.ipList.remove(AntiVoid.mc.getCurrentServer().ip);
        }
    }
}
