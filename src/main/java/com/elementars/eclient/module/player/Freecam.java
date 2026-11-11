package com.elementars.eclient.module.player;

import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventSendPacket;
import com.elementars.eclient.event.events.PlayerMoveEvent;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.Entity;
import dev.xulu.settings.Value;

public class Freecam extends Module
{
    private double posX;
    private double posY;
    private double posZ;
    private float pitch;
    private float yaw;
    private RemotePlayer clonedPlayer;
    private boolean isRidingEntity;
    private Entity ridingEntity;
    private boolean savedPosition;
    private final Value<Boolean> cancelPackets;
    private final Value<Integer> speed;
    private final Value<Integer> vspeed;

    public Freecam() {
        super("Freecam", "Allows you to fly out of your body", 0, Category.PLAYER, true);
        this.cancelPackets = this.register(new Value<Boolean>("Cancel Packets", this, true));
        this.speed = this.register(new Value<Integer>("Speed", this, 11, 1, 20));
        this.vspeed = this.register(new Value<Integer>("V Speed", this, 7, 1, 20));
    }

    @Override
    public void onUpdate() {
        if (Freecam.mc.player == null) {
            return;
        }
        Freecam.mc.player.noPhysics = true;
        Freecam.mc.player.getAbilities().flying = true;
        float flySpeed = this.speed.getValue() / 100.0f;
        if (Freecam.mc.options.keyJump.isDown() || Freecam.mc.options.keyShift.isDown()) {
            flySpeed = this.vspeed.getValue() / 100.0f;
        }
        Freecam.mc.player.getAbilities().setFlyingSpeed(flySpeed);
        Freecam.mc.player.fallDistance = 0.0f;
        Freecam.mc.player.setOnGround(false);
    }

    @Override
    public void onEnable() {
        if (Freecam.mc.player == null || Freecam.mc.level == null) {
            return;
        }
        this.isRidingEntity = Freecam.mc.player.isPassenger();
        if (this.isRidingEntity) {
            this.ridingEntity = Freecam.mc.player.getVehicle();
            Freecam.mc.player.stopRiding();
        }
        this.posX = Freecam.mc.player.getX();
        this.posY = Freecam.mc.player.getY();
        this.posZ = Freecam.mc.player.getZ();
        this.pitch = Freecam.mc.player.getXRot();
        this.yaw = Freecam.mc.player.getYRot();
        this.savedPosition = true;
        final ClientLevel level = Freecam.mc.level;
        this.clonedPlayer = new RemotePlayer(level, Freecam.mc.getUser().getGameProfile());
        this.clonedPlayer.moveTo(this.posX, this.posY, this.posZ, this.yaw, this.pitch);
        this.clonedPlayer.setYHeadRot(Freecam.mc.player.getYHeadRot());
        this.clonedPlayer.setOldPosAndRot();
        level.addPlayer(-100, this.clonedPlayer);
        Freecam.mc.player.noPhysics = true;
    }

    @Override
    public void onDisable() {
        if (Freecam.mc.player != null && this.savedPosition) {
            Freecam.mc.player.moveTo(this.posX, this.posY, this.posZ, this.yaw, this.pitch);
            Freecam.mc.player.setOldPosAndRot();
            Freecam.mc.player.getAbilities().flying = false;
            Freecam.mc.player.getAbilities().setFlyingSpeed(0.05f);
            Freecam.mc.player.noPhysics = false;
            Freecam.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
            Freecam.mc.player.fallDistance = 0.0f;
            if (this.isRidingEntity && this.ridingEntity != null) {
                Freecam.mc.player.startRiding(this.ridingEntity, true);
            }
        }
        if (Freecam.mc.level != null) {
            Freecam.mc.level.removeEntity(-100, Entity.RemovalReason.DISCARDED);
        }
        this.clonedPlayer = null;
        this.ridingEntity = null;
        this.savedPosition = false;
        if (Freecam.mc.level != null) {
            Freecam.mc.levelRenderer.allChanged();
        }
    }

    @EventTarget
    public void onPacketSent(final EventSendPacket event) {
        if (this.cancelPackets.getValue() && (event.getPacket() instanceof ServerboundMovePlayerPacket || event.getPacket() instanceof ServerboundPlayerInputPacket)) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onMove(final PlayerMoveEvent event) {
        if (Freecam.mc.player != null) {
            Freecam.mc.player.noPhysics = true;
        }
    }
}
