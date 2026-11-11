package com.elementars.eclient.module.combat;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventSendPacket;
import com.elementars.eclient.friend.Friends;
import com.elementars.eclient.mixin.mixins.AccessorServerboundMovePlayerPacket;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.EntityUtil;
import com.elementars.eclient.util.LagCompensator;
import dev.xulu.settings.Value;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Aura extends Module
{
    final Value<Boolean> players;
    final Value<Boolean> animals;
    final Value<Boolean> mobs;
    final Value<Double> range;
    final Value<Boolean> wait;
    final Value<Boolean> walls;
    final Value<Boolean> rotate;
    final Value<Boolean> sharpness;
    boolean isSpoofingAngles;
    double yaw;
    double pitch;

    public Aura() {
        super("Aura", "Hits people", 0, Category.COMBAT, true);
        this.players = this.register(new Value<Boolean>("Players", this, true));
        this.animals = this.register(new Value<Boolean>("Animals", this, false));
        this.mobs = this.register(new Value<Boolean>("Mobs", this, false));
        this.range = this.register(new Value<Double>("Range", this, 5.5, 1.0, 10.0));
        this.wait = this.register(new Value<Boolean>("Wait", this, true));
        this.walls = this.register(new Value<Boolean>("Walls", this, false));
        this.rotate = this.register(new Value<Boolean>("Rotate", this, true));
        this.sharpness = this.register(new Value<Boolean>("32k Switch", this, false));
        this.isSpoofingAngles = false;
    }

    @Override
    public void onUpdate() {
        if (Aura.mc.player == null || Aura.mc.level == null || Aura.mc.player.isDeadOrDying()) {
            return;
        }
        if (!this.rotate.getValue()) {
            this.resetRotation();
        }
        if (this.shouldPause()) {
            this.resetRotation();
            return;
        }
        final ItemStack offhand = Aura.mc.player.getOffhandItem();
        final InteractionHand usedHand = Aura.mc.player.getUsedItemHand();
        final boolean shield = offhand.is(Items.SHIELD) && usedHand == InteractionHand.OFF_HAND;
        final boolean gap = (offhand.is(Items.GOLDEN_APPLE) || offhand.is(Items.ENCHANTED_GOLDEN_APPLE)) && usedHand == InteractionHand.OFF_HAND;
        if (Aura.mc.player.isUsingItem() && !shield && !gap) {
            return;
        }
        if (this.wait.getValue()) {
            if (Aura.mc.player.getAttackStrengthScale(this.getLagComp()) < 1.0f) {
                return;
            }
            if (Aura.mc.player.tickCount % 2 != 0) {
                return;
            }
        }
        for (final Entity target : Aura.mc.level.entitiesForRendering()) {
            if (!EntityUtil.isLiving(target)) {
                continue;
            }
            if (target == Aura.mc.player) {
                continue;
            }
            if (Aura.mc.player.distanceTo(target) > this.range.getValue()) {
                continue;
            }
            final LivingEntity living = (LivingEntity)target;
            if (living.getHealth() <= 0.0f) {
                continue;
            }
            if (living.hurtTime != 0 && this.wait.getValue()) {
                continue;
            }
            if (!this.walls.getValue() && !Aura.mc.player.hasLineOfSight(target) && !this.canEntityFeetBeSeen(target)) {
                continue;
            }
            if (this.players.getValue() && target instanceof Player && !Friends.isFriend(target.getName().getString())) {
                this.attack(target);
                return;
            }
            if (EntityUtil.isPassive(target)) {
                if (this.animals.getValue()) {
                    this.attack(target);
                    return;
                }
                continue;
            }
            if (EntityUtil.isMobAggressive(target) && this.mobs.getValue()) {
                this.attack(target);
                return;
            }
        }
        this.resetRotation();
    }

    private boolean checkSharpness(final ItemStack stack) {
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, stack) >= 16;
    }

    private void attack(final Entity e) {
        if (this.sharpness.getValue() && !this.checkSharpness(Aura.mc.player.getMainHandItem())) {
            int newSlot = -1;
            for (int i = 0; i < 9; ++i) {
                final ItemStack stack = Aura.mc.player.getInventory().getItem(i);
                if (!stack.isEmpty() && this.checkSharpness(stack)) {
                    newSlot = i;
                    break;
                }
            }
            if (newSlot != -1) {
                Aura.mc.player.getInventory().selected = newSlot;
            }
        }
        if (this.rotate.getValue()) {
            this.lookAtPacket(e.getX(), e.getY(), e.getZ(), Aura.mc.player);
            Aura.mc.player.connection.send(new ServerboundMovePlayerPacket.Rot((float)this.yaw, (float)this.pitch, Aura.mc.player.isOnGround()));
        }
        Aura.mc.gameMode.attack(Aura.mc.player, e);
        Aura.mc.player.swing(InteractionHand.MAIN_HAND);
    }

    private float getLagComp() {
        if (this.wait.getValue()) {
            final float tickRate = LagCompensator.INSTANCE == null ? 20.0f : LagCompensator.INSTANCE.getTickRate();
            return -(20.0f - tickRate);
        }
        return 0.0f;
    }

    private boolean canEntityFeetBeSeen(final Entity entityIn) {
        final Vec3 eyes = new Vec3(Aura.mc.player.getX(), Aura.mc.player.getY() + Aura.mc.player.getEyeHeight(), Aura.mc.player.getZ());
        final Vec3 feet = new Vec3(entityIn.getX(), entityIn.getY(), entityIn.getZ());
        return Aura.mc.level.clip(new ClipContext(eyes, feet, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, Aura.mc.player)).getType() == HitResult.Type.MISS;
    }

    private void lookAtPacket(final double px, final double py, final double pz, final Player me) {
        final double[] v = AutoCrystal.calculateLookAt(px, py, pz, me);
        this.setYawAndPitch((float)v[0], (float)v[1]);
    }

    private void setYawAndPitch(final float yaw1, final float pitch1) {
        this.yaw = yaw1;
        this.pitch = pitch1;
        this.isSpoofingAngles = true;
    }

    private void resetRotation() {
        if (this.isSpoofingAngles && Aura.mc.player != null) {
            final float realYaw = Aura.mc.player.getYRot();
            final float realPitch = Aura.mc.player.getXRot();
            this.yaw = realYaw;
            this.pitch = realPitch;
            this.isSpoofingAngles = false;
            if (Aura.mc.player.connection != null) {
                Aura.mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(realYaw, realPitch, Aura.mc.player.isOnGround()));
            }
        }
    }

    @Override
    public void onDisable() {
        this.resetRotation();
    }

    @EventTarget
    public void onSend(final EventSendPacket event) {
        final Packet<?> packet = event.getPacket();
        if (!(packet instanceof ServerboundMovePlayerPacket) || !this.rotate.getValue() || !this.isSpoofingAngles) {
            return;
        }
        if (packet instanceof ServerboundMovePlayerPacket.Pos) {
            final ServerboundMovePlayerPacket.Pos pos = (ServerboundMovePlayerPacket.Pos)packet;
            event.setPacket(new ServerboundMovePlayerPacket.PosRot(pos.getX(0.0), pos.getY(0.0), pos.getZ(0.0), (float)this.yaw, (float)this.pitch, pos.isOnGround()));
        }
        else if (packet instanceof ServerboundMovePlayerPacket.StatusOnly) {
            event.setPacket(new ServerboundMovePlayerPacket.Rot((float)this.yaw, (float)this.pitch, ((ServerboundMovePlayerPacket.StatusOnly)packet).isOnGround()));
        }
        else {
            final AccessorServerboundMovePlayerPacket accessor = (AccessorServerboundMovePlayerPacket)packet;
            accessor.setPacketYRot((float)this.yaw);
            accessor.setPacketXRot((float)this.pitch);
        }
    }

    private boolean shouldPause() {
        final Module surround = Xulu.MODULE_MANAGER.getModule(Surround.class);
        final Module autoTrap = Xulu.MODULE_MANAGER.getModule(AutoTrap.class);
        final Module holeFill = Xulu.MODULE_MANAGER.getModule(HoleFill.class);
        final Module holeBlocker = Xulu.MODULE_MANAGER.getModule(HoleBlocker.class);
        return (surround != null && surround.isToggled() && Surround.isExposed() && ((Surround)surround).findObiInHotbar() != -1)
            || (autoTrap != null && autoTrap.isToggled())
            || (holeFill != null && holeFill.isToggled())
            || (holeBlocker != null && holeBlocker.isToggled() && HoleBlocker.isExposed() && surround != null && ((Surround)surround).findObiInHotbar() != -1);
    }
}
