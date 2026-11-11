package com.elementars.eclient.module.movement;

import com.elementars.eclient.event.Event;
import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventSendPacket;
import com.elementars.eclient.event.events.LocalPlayerUpdateEvent;
import com.elementars.eclient.event.events.MotionEvent;
import com.elementars.eclient.event.events.PlayerMoveEvent;
import com.elementars.eclient.mixin.mixins.AccessorServerboundMovePlayerPacket;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.MathUtil;
import com.elementars.eclient.util.Timer;
import dev.xulu.settings.Value;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class ElytraFly extends Module
{
    public Value<Mode> mode;
    public Value<Integer> devMode;
    public Value<Float> speed;
    public Value<Float> vSpeed;
    public Value<Float> hSpeed;
    public Value<Float> glide;
    public Value<Float> tooBeeSpeed;
    public Value<Boolean> autoStart;
    public Value<Boolean> disableInLiquid;
    public Value<Boolean> infiniteDura;
    public Value<Boolean> noKick;
    public Value<Boolean> keepMotion;
    public Value<Boolean> instaFly;
    public Value<String> instaMode;
    public Value<Boolean> allowUp;
    private static ElytraFly INSTANCE;
    private final Timer timer;
    private final Timer instaTimer;
    private Double posX;
    private Double flyHeight;
    private Double posZ;

    public ElytraFly() {
        super("ElytraFly", "Makes Elytra Flight better.", 0, Category.MOVEMENT, true);
        this.mode = this.register(new Value<Mode>("Mode", this, Mode.FLY, Mode.values()));
        this.devMode = this.register(new Value<Integer>("Type", this, 2, 1, 3)).visibleWhen(t -> this.mode.getValue() == Mode.BYPASS || this.mode.getValue() == Mode.BETTER);
        this.speed = this.register(new Value<Float>("Speed", this, 1.0f, 0.0f, 10.0f)).visibleWhen(t -> this.mode.getValue() != Mode.FLY && this.mode.getValue() != Mode.BOOST && this.mode.getValue() != Mode.BETTER && this.mode.getValue() != Mode.OHARE);
        this.vSpeed = this.register(new Value<Float>("VSpeed", this, 0.3f, 0.0f, 10.0f)).visibleWhen(t -> this.mode.getValue() == Mode.BETTER || this.mode.getValue() == Mode.OHARE);
        this.hSpeed = this.register(new Value<Float>("HSpeed", this, 1.0f, 0.0f, 10.0f)).visibleWhen(t -> this.mode.getValue() == Mode.BETTER || this.mode.getValue() == Mode.OHARE);
        this.glide = this.register(new Value<Float>("Glide", this, 1.0E-4f, 0.0f, 0.2f)).visibleWhen(t -> this.mode.getValue() == Mode.BETTER);
        this.tooBeeSpeed = this.register(new Value<Float>("TooBeeSpeed", this, 1.8000001f, 1.0f, 2.0f)).visibleWhen(t -> this.mode.getValue() == Mode.TOOBEE);
        this.autoStart = this.register(new Value<Boolean>("AutoStart", this, true));
        this.disableInLiquid = this.register(new Value<Boolean>("NoLiquid", this, true));
        this.infiniteDura = this.register(new Value<Boolean>("InfiniteDura", this, false));
        this.noKick = this.register(new Value<Boolean>("NoKick", this, false)).visibleWhen(t -> this.mode.getValue() == Mode.PACKET || this.mode.getValue() == Mode.BETTER);
        this.keepMotion = this.register(new Value<Boolean>("KeepMotion", this, true)).visibleWhen(aBoolean -> this.mode.getValue() == Mode.BETTER);
        this.instaFly = this.register(new Value<Boolean>("InstaFly", this, true)).visibleWhen(aBoolean -> this.mode.getValue() == Mode.BETTER);
        this.instaMode = this.register(new Value<String>("Takeoff Mode", this, "Static", new String[] { "None", "Static" })).visibleWhen(aBoolean -> this.mode.getValue() == Mode.BETTER);
        this.allowUp = this.register(new Value<Boolean>("AllowUp", this, true)).visibleWhen(t -> this.mode.getValue() == Mode.BETTER);
        this.timer = new Timer();
        this.instaTimer = new Timer();
        this.setInstance();
    }

    private void setInstance() {
        ElytraFly.INSTANCE = this;
    }

    public static ElytraFly getInstance() {
        if (ElytraFly.INSTANCE == null) {
            ElytraFly.INSTANCE = new ElytraFly();
        }
        return ElytraFly.INSTANCE;
    }

    @Override
    public void onEnable() {
        if (ElytraFly.mc.player != null && this.mode.getValue() == Mode.BETTER && !this.autoStart.getValue() && this.devMode.getValue() == 1) {
            ElytraFly.mc.player.connection.send(new ServerboundPlayerCommandPacket(ElytraFly.mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        }
        this.flyHeight = null;
        this.posX = null;
        this.posZ = null;
    }

    @Override
    public String getHudInfo() {
        return this.mode.getValue().name();
    }

    @Override
    public void onUpdate() {
        if (ElytraFly.mc.player == null) {
            return;
        }
        if (this.mode.getValue() == Mode.BYPASS && (this.devMode.getValue() == 1 || this.devMode.getValue() == 2) && ElytraFly.mc.player.isFallFlying()) {
            ElytraFly.mc.player.setDeltaMovement(0.0, -1.0E-4, 0.0);
            final double forwardInput = ElytraFly.mc.player.input.forwardImpulse;
            final double strafeInput = ElytraFly.mc.player.input.leftImpulse;
            final double[] result = this.forwardStrafeYaw(forwardInput, strafeInput, ElytraFly.mc.player.getYRot());
            final double forward = result[0];
            final double strafe = result[1];
            final double yaw = result[2];
            if (forwardInput != 0.0 || strafeInput != 0.0) {
                ElytraFly.mc.player.setDeltaMovement(forward * this.speed.getValue() * Math.cos(Math.toRadians(yaw + 90.0)) + strafe * this.speed.getValue() * Math.sin(Math.toRadians(yaw + 90.0)), ElytraFly.mc.player.getDeltaMovement().y, forward * this.speed.getValue() * Math.sin(Math.toRadians(yaw + 90.0)) - strafe * this.speed.getValue() * Math.cos(Math.toRadians(yaw + 90.0)));
            }
            if (ElytraFly.mc.options.keyShift.isDown()) {
                final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
                ElytraFly.mc.player.setDeltaMovement(motion.x, -1.0, motion.z);
            }
        }
    }

    @EventTarget
    public void onSendPacket(final EventSendPacket event) {
        if (!(event.getPacket() instanceof ServerboundMovePlayerPacket)) {
            return;
        }
        if (this.mode.getValue() != Mode.TOOBEE && this.mode.getValue() != Mode.BETTER) {
            return;
        }
        if (ElytraFly.mc.player == null) {
            return;
        }
        final ServerboundMovePlayerPacket packet = (ServerboundMovePlayerPacket)event.getPacket();
        if (ElytraFly.mc.player.isFallFlying() && !ElytraFly.mc.player.input.jumping && ((AccessorServerboundMovePlayerPacket)packet).getPacketXRot() < 1.0f) {
            ((AccessorServerboundMovePlayerPacket)packet).setPacketXRot(1.0f);
        }
    }

    @EventTarget
    public void onMove(final PlayerMoveEvent event) {
        if (ElytraFly.mc.player == null || !ElytraFly.mc.player.isFallFlying()) {
            return;
        }
        if (this.mode.getValue() == Mode.OHARE) {
            final ItemStack itemstack = ElytraFly.mc.player.getItemBySlot(EquipmentSlot.CHEST);
            if (itemstack.is(Items.ELYTRA) && ElytraItem.isFlyEnabled(itemstack) && ElytraFly.mc.player.isFallFlying()) {
                event.setY(ElytraFly.mc.options.keyJump.isDown() ? ((double)this.vSpeed.getValue()) : (ElytraFly.mc.options.keyShift.isDown() ? (-this.vSpeed.getValue()) : 0.0));
                double forward = ElytraFly.mc.player.input.forwardImpulse;
                double strafe = ElytraFly.mc.player.input.leftImpulse;
                float yaw = ElytraFly.mc.player.getYRot();
                if (forward == 0.0 && strafe == 0.0) {
                    event.setX(0.0);
                    event.setZ(0.0);
                }
                else {
                    if (forward != 0.0) {
                        if (strafe > 0.0) {
                            yaw += ((forward > 0.0) ? -45 : 45);
                        }
                        else if (strafe < 0.0) {
                            yaw += ((forward > 0.0) ? 45 : -45);
                        }
                        strafe = 0.0;
                        if (forward > 0.0) {
                            forward = 1.0;
                        }
                        else if (forward < 0.0) {
                            forward = -1.0;
                        }
                    }
                    final double cos = Math.cos(Math.toRadians(yaw + 90.0f));
                    final double sin = Math.sin(Math.toRadians(yaw + 90.0f));
                    event.setX(forward * this.hSpeed.getValue() * cos + strafe * this.hSpeed.getValue() * sin);
                    event.setZ(forward * this.hSpeed.getValue() * sin - strafe * this.hSpeed.getValue() * cos);
                }
            }
        }
        else if (event.getEventState() == Event.State.PRE && this.mode.getValue() == Mode.BYPASS && this.devMode.getValue() == 3) {
            if (ElytraFly.mc.player.isFallFlying()) {
                event.setX(0.0);
                event.setY(-1.0E-4);
                event.setZ(0.0);
                final double forwardInput = ElytraFly.mc.player.input.forwardImpulse;
                final double strafeInput = ElytraFly.mc.player.input.leftImpulse;
                final double[] result = this.forwardStrafeYaw(forwardInput, strafeInput, ElytraFly.mc.player.getYRot());
                final double forward2 = result[0];
                final double strafe2 = result[1];
                final double yaw2 = result[2];
                if (forwardInput != 0.0 || strafeInput != 0.0) {
                    event.setX(forward2 * this.speed.getValue() * Math.cos(Math.toRadians(yaw2 + 90.0)) + strafe2 * this.speed.getValue() * Math.sin(Math.toRadians(yaw2 + 90.0)));
                    event.setZ(forward2 * this.speed.getValue() * Math.sin(Math.toRadians(yaw2 + 90.0)) - strafe2 * this.speed.getValue() * Math.cos(Math.toRadians(yaw2 + 90.0)));
                }
                if (ElytraFly.mc.options.keyShift.isDown()) {
                    event.setY(-1.0);
                }
            }
        }
        else if (this.mode.getValue() == Mode.TOOBEE) {
            if (!ElytraFly.mc.player.isFallFlying()) {
                return;
            }
            if (ElytraFly.mc.player.input.jumping) {
                return;
            }
            if (ElytraFly.mc.player.input.shiftKeyDown) {
                final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
                ElytraFly.mc.player.setDeltaMovement(motion.x, -(this.tooBeeSpeed.getValue() / 2.0f), motion.z);
                event.setY(-(this.speed.getValue() / 2.0f));
            }
            else if (event.getY() != -1.01E-4) {
                event.setY(-1.01E-4);
                final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
                ElytraFly.mc.player.setDeltaMovement(motion.x, -1.01E-4, motion.z);
            }
        }
        this.setMoveSpeed(event, this.tooBeeSpeed.getValue());
    }

    private void setMoveSpeed(final PlayerMoveEvent event, final double speed) {
        double forward = ElytraFly.mc.player.input.forwardImpulse;
        double strafe = ElytraFly.mc.player.input.leftImpulse;
        float yaw = ElytraFly.mc.player.getYRot();
        if (forward == 0.0 && strafe == 0.0) {
            event.setX(0.0);
            event.setZ(0.0);
            final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
            ElytraFly.mc.player.setDeltaMovement(0.0, motion.y, 0.0);
        }
        else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += ((forward > 0.0) ? -45 : 45);
                }
                else if (strafe < 0.0) {
                    yaw += ((forward > 0.0) ? 45 : -45);
                }
                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                }
                else if (forward < 0.0) {
                    forward = -1.0;
                }
            }
            final double x = forward * speed * -Math.sin(Math.toRadians(yaw)) + strafe * speed * Math.cos(Math.toRadians(yaw));
            final double z = forward * speed * Math.cos(Math.toRadians(yaw)) - strafe * speed * -Math.sin(Math.toRadians(yaw));
            event.setX(x);
            event.setZ(z);
            final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
            ElytraFly.mc.player.setDeltaMovement(x, motion.y, z);
        }
    }

    @EventTarget
    public void onLivingUpdate(final LocalPlayerUpdateEvent event) {
        if (!ElytraFly.mc.player.isFallFlying()) {
            return;
        }
        switch (this.mode.getValue()) {
            case BOOST: {
                if (ElytraFly.mc.player.isInWater()) {
                    ElytraFly.mc.getConnection().send(new ServerboundPlayerCommandPacket(ElytraFly.mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
                    return;
                }
                if (ElytraFly.mc.options.keyJump.isDown()) {
                    final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
                    ElytraFly.mc.player.setDeltaMovement(motion.x, motion.y + 0.08, motion.z);
                }
                else if (ElytraFly.mc.options.keyShift.isDown()) {
                    final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
                    ElytraFly.mc.player.setDeltaMovement(motion.x, motion.y - 0.04, motion.z);
                }
                if (ElytraFly.mc.options.keyUp.isDown()) {
                    final float yaw = (float)Math.toRadians(ElytraFly.mc.player.getYRot());
                    final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
                    ElytraFly.mc.player.setDeltaMovement(motion.x - Mth.sin(yaw) * 0.05f, motion.y, motion.z + Mth.cos(yaw) * 0.05f);
                    break;
                }
                if (ElytraFly.mc.options.keyDown.isDown()) {
                    final float yaw = (float)Math.toRadians(ElytraFly.mc.player.getYRot());
                    final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
                    ElytraFly.mc.player.setDeltaMovement(motion.x + Mth.sin(yaw) * 0.05f, motion.y, motion.z - Mth.cos(yaw) * 0.05f);
                    break;
                }
                break;
            }
            case FLY: {
                ElytraFly.mc.player.getAbilities().flying = true;
                break;
            }
        }
    }

    @EventTarget
    public void onUpdateWalkingPlayer(final MotionEvent event) {
        if (ElytraFly.mc.player == null) {
            return;
        }
        if (!ElytraFly.mc.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
            return;
        }
        if (!ElytraFly.mc.player.isFallFlying()) {
            if (!ElytraFly.mc.player.isOnGround() && this.instaFly.getValue()) {
                double height = ElytraFly.mc.player.getY();
                if (this.noKick.getValue()) {
                    height -= this.glide.getValue();
                }
                if ("Static".equals(this.instaMode.getValue())) {
                    ElytraFly.mc.player.setPos(ElytraFly.mc.player.getX(), height, ElytraFly.mc.player.getZ());
                    ElytraFly.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
                }
                if (!this.instaTimer.hasReached(1000L)) {
                    return;
                }
                this.instaTimer.reset();
                ElytraFly.mc.player.connection.send(new ServerboundPlayerCommandPacket(ElytraFly.mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
            }
            else if (this.autoStart.getValue() && ElytraFly.mc.options.keyJump.isDown() && ElytraFly.mc.player.getDeltaMovement().y < 0.0 && this.timer.hasReached(250L)) {
                ElytraFly.mc.player.connection.send(new ServerboundPlayerCommandPacket(ElytraFly.mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
                this.timer.reset();
            }
            return;
        }
        switch (event.getEventState()) {
            case PRE: {
                if (this.disableInLiquid.getValue() && (ElytraFly.mc.player.isInWater() || ElytraFly.mc.player.isInLava())) {
                    ElytraFly.mc.getConnection().send(new ServerboundPlayerCommandPacket(ElytraFly.mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
                    return;
                }
                if (this.mode.getValue() == Mode.BETTER) {
                    final double[] dir = MathUtil.directionSpeed((this.devMode.getValue() == 1) ? ((double)this.speed.getValue()) : ((double)this.hSpeed.getValue()));
                    switch (this.devMode.getValue()) {
                        case 1: {
                            double jumpMotion = 0.0;
                            if (ElytraFly.mc.options.keyJump.isDown()) {
                                jumpMotion += this.speed.getValue();
                            }
                            if (ElytraFly.mc.options.keyShift.isDown()) {
                                jumpMotion -= this.speed.getValue();
                            }
                            double forwardMotion = 0.0;
                            double strafeMotion = 0.0;
                            if (ElytraFly.mc.player.input.leftImpulse != 0.0f || ElytraFly.mc.player.input.forwardImpulse != 0.0f) {
                                forwardMotion = dir[0];
                                strafeMotion = dir[1];
                            }
                            ElytraFly.mc.player.setDeltaMovement(forwardMotion, jumpMotion, strafeMotion);
                            break;
                        }
                        case 2: {
                            if (ElytraFly.mc.player.isFallFlying()) {
                                if (this.flyHeight == null) {
                                    this.flyHeight = ElytraFly.mc.player.getY();
                                }
                                if (this.noKick.getValue()) {
                                    this.flyHeight = this.flyHeight - this.glide.getValue();
                                }
                                this.posX = 0.0;
                                this.posZ = 0.0;
                                if (ElytraFly.mc.player.input.leftImpulse != 0.0f || ElytraFly.mc.player.input.forwardImpulse != 0.0f) {
                                    this.posX = dir[0];
                                    this.posZ = dir[1];
                                }
                                if (ElytraFly.mc.options.keyShift.isDown()) {
                                    this.flyHeight = ElytraFly.mc.player.getY() - this.vSpeed.getValue();
                                }
                                ElytraFly.mc.player.setPos(ElytraFly.mc.player.getX() + this.posX, this.flyHeight, ElytraFly.mc.player.getZ() + this.posZ);
                                ElytraFly.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
                                break;
                            }
                            this.flyHeight = null;
                            return;
                        }
                        case 3: {
                            if (ElytraFly.mc.player.isFallFlying()) {
                                if (this.flyHeight == null || this.posX == null || this.posX == 0.0 || this.posZ == null || this.posZ == 0.0) {
                                    this.flyHeight = ElytraFly.mc.player.getY();
                                    this.posX = ElytraFly.mc.player.getX();
                                    this.posZ = ElytraFly.mc.player.getZ();
                                }
                                if (this.noKick.getValue()) {
                                    this.flyHeight = this.flyHeight - this.glide.getValue();
                                }
                                if (ElytraFly.mc.player.input.leftImpulse != 0.0f || ElytraFly.mc.player.input.forwardImpulse != 0.0f) {
                                    this.posX = this.posX + dir[0];
                                    this.posZ = this.posZ + dir[1];
                                }
                                if (this.allowUp.getValue() && ElytraFly.mc.options.keyJump.isDown()) {
                                    this.flyHeight = ElytraFly.mc.player.getY() + this.vSpeed.getValue() / 10.0f;
                                }
                                if (ElytraFly.mc.options.keyShift.isDown()) {
                                    this.flyHeight = ElytraFly.mc.player.getY() - this.vSpeed.getValue() / 10.0f;
                                }
                                ElytraFly.mc.player.setPos(this.posX, this.flyHeight, this.posZ);
                                ElytraFly.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
                                break;
                            }
                            this.flyHeight = null;
                            this.posX = null;
                            this.posZ = null;
                            return;
                        }
                    }
                }
                final double rotationYaw = Math.toRadians(ElytraFly.mc.player.getYRot());
                if (ElytraFly.mc.player.isFallFlying()) {
                    switch (this.mode.getValue()) {
                        case VANILLA: {
                            final float speedScaled = this.speed.getValue() * 0.05f;
                            if (ElytraFly.mc.options.keyJump.isDown()) {
                                final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
                                ElytraFly.mc.player.setDeltaMovement(motion.x, motion.y + speedScaled, motion.z);
                            }
                            if (ElytraFly.mc.options.keyShift.isDown()) {
                                final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
                                ElytraFly.mc.player.setDeltaMovement(motion.x, motion.y - speedScaled, motion.z);
                            }
                            if (ElytraFly.mc.options.keyUp.isDown()) {
                                final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
                                ElytraFly.mc.player.setDeltaMovement(motion.x - Math.sin(rotationYaw) * speedScaled, motion.y, motion.z + Math.cos(rotationYaw) * speedScaled);
                            }
                            if (ElytraFly.mc.options.keyDown.isDown()) {
                                final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
                                ElytraFly.mc.player.setDeltaMovement(motion.x + Math.sin(rotationYaw) * speedScaled, motion.y, motion.z - Math.cos(rotationYaw) * speedScaled);
                                break;
                            }
                            break;
                        }
                        case PACKET: {
                            this.freezePlayer();
                            this.runNoKick();
                            final double[] directionSpeedPacket = MathUtil.directionSpeed(this.speed.getValue());
                            if (ElytraFly.mc.player.input.jumping) {
                                final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
                                ElytraFly.mc.player.setDeltaMovement(motion.x, this.speed.getValue(), motion.z);
                            }
                            if (ElytraFly.mc.player.input.shiftKeyDown) {
                                final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
                                ElytraFly.mc.player.setDeltaMovement(motion.x, -this.speed.getValue(), motion.z);
                            }
                            if (ElytraFly.mc.player.input.leftImpulse != 0.0f || ElytraFly.mc.player.input.forwardImpulse != 0.0f) {
                                final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
                                ElytraFly.mc.player.setDeltaMovement(directionSpeedPacket[0], motion.y, directionSpeedPacket[1]);
                            }
                            ElytraFly.mc.getConnection().send(new ServerboundPlayerCommandPacket(ElytraFly.mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
                            ElytraFly.mc.getConnection().send(new ServerboundPlayerCommandPacket(ElytraFly.mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
                            break;
                        }
                        case BYPASS: {
                            if (this.devMode.getValue() != 3) {
                                break;
                            }
                            if (ElytraFly.mc.options.keyJump.isDown()) {
                                final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
                                ElytraFly.mc.player.setDeltaMovement(motion.x, 0.019999999552965164, motion.z);
                            }
                            if (ElytraFly.mc.options.keyShift.isDown()) {
                                final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
                                ElytraFly.mc.player.setDeltaMovement(motion.x, -0.20000000298023224, motion.z);
                            }
                            if (ElytraFly.mc.player.tickCount % 8 == 0 && ElytraFly.mc.player.getY() <= 240.0) {
                                final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
                                ElytraFly.mc.player.setDeltaMovement(motion.x, 0.019999999552965164, motion.z);
                            }
                            ElytraFly.mc.player.getAbilities().flying = true;
                            ElytraFly.mc.player.getAbilities().setFlyingSpeed(0.025f);
                            final double[] directionSpeedBypass = MathUtil.directionSpeed(0.5199999809265137);
                            if (ElytraFly.mc.player.input.leftImpulse != 0.0f || ElytraFly.mc.player.input.forwardImpulse != 0.0f) {
                                final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
                                ElytraFly.mc.player.setDeltaMovement(directionSpeedBypass[0], motion.y, directionSpeedBypass[1]);
                                break;
                            }
                            final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
                            ElytraFly.mc.player.setDeltaMovement(0.0, motion.y, 0.0);
                            break;
                        }
                    }
                }
                if (this.infiniteDura.getValue()) {
                    ElytraFly.mc.player.connection.send(new ServerboundPlayerCommandPacket(ElytraFly.mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
                    break;
                }
                break;
            }
            case POST: {
                if (this.infiniteDura.getValue()) {
                    ElytraFly.mc.player.connection.send(new ServerboundPlayerCommandPacket(ElytraFly.mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
                    break;
                }
                break;
            }
        }
    }

    private double[] forwardStrafeYaw(final double forward, final double strafe, final double yaw) {
        final double[] result = { forward, strafe, yaw };
        if ((forward != 0.0 || strafe != 0.0) && forward != 0.0) {
            if (strafe > 0.0) {
                result[2] += ((forward > 0.0) ? -45 : 45);
            }
            else if (strafe < 0.0) {
                result[2] += ((forward > 0.0) ? 45 : -45);
            }
            result[1] = 0.0;
            if (forward > 0.0) {
                result[0] = 1.0;
            }
            else if (forward < 0.0) {
                result[0] = -1.0;
            }
        }
        return result;
    }

    private void freezePlayer() {
        ElytraFly.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
    }

    private void runNoKick() {
        if (this.noKick.getValue() && ElytraFly.mc.player.tickCount % 4 == 0) {
            final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
            ElytraFly.mc.player.setDeltaMovement(motion.x, -0.03999999910593033, motion.z);
        }
    }

    @Override
    public void onDisable() {
        if (ElytraFly.mc.player == null || ElytraFly.mc.level == null) {
            return;
        }
        if (this.mode.getValue() == Mode.BETTER && this.devMode.getValue() == 2 && this.keepMotion.getValue()) {
            final Vec3 motion = ElytraFly.mc.player.getDeltaMovement();
            ElytraFly.mc.player.setDeltaMovement(this.posX != null ? this.posX : motion.x, motion.y, this.posZ != null ? this.posZ : motion.z);
        }
        if (!ElytraFly.mc.player.getAbilities().instabuild) {
            ElytraFly.mc.player.getAbilities().flying = false;
        }
    }

    public enum Mode
    {
        VANILLA,
        PACKET,
        BOOST,
        FLY,
        BYPASS,
        BETTER,
        OHARE,
        TOOBEE;
    }
}
