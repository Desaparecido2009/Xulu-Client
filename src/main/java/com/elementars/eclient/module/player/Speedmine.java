package com.elementars.eclient.module.player;

import com.elementars.eclient.event.Event;
import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventClickBlock;
import com.elementars.eclient.event.events.EventPlayerDamageBlock;
import com.elementars.eclient.event.events.EventSendPacket;
import com.elementars.eclient.event.events.RenderEvent;
import com.elementars.eclient.mixin.mixins.AccessorMultiPlayerGameMode;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.BlockInteractionHelper;
import com.elementars.eclient.util.XuluTessellator;
import dev.xulu.settings.Value;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Speedmine extends Module
{
    private static final float BREAK_MARGIN = 0.75f;
    private static Speedmine INSTANCE;
    public Value<Boolean> tweaks;
    public Value<Mode> mode;
    public Value<Boolean> reset;
    public Value<Float> damage;
    public Value<Boolean> noBreakAnim;
    public Value<Boolean> noDelay;
    public Value<Boolean> noSwing;
    public Value<Boolean> noTrace;
    public Value<Boolean> allow;
    public Value<Boolean> pickaxe;
    public Value<Boolean> doubleBreak;
    public Value<Boolean> webSwitch;
    public Value<Boolean> render;
    public Value<Boolean> box;
    public Value<Boolean> outline;
    private final Value<Integer> boxAlpha;
    private final Value<Float> lineWidth;
    public BlockPos currentPos;
    public BlockState currentBlockState;
    private final PacketBreak mainBreak;
    private final PacketBreak doubleBreakBreak;
    private BlockPos pendingDoubleBreak;
    private BlockPos lastDoubleBreakSource;

    public Speedmine() {
        super("Speedmine", "Speeds up mining.", 0, Category.PLAYER, true);
        this.tweaks = this.register(new Value<Boolean>("Tweaks", this, true));
        this.mode = this.register(new Value<Mode>("Mode", this, Mode.PACKET, Mode.values())).visibleWhen(mode1 -> this.tweaks.getValue());
        this.reset = this.register(new Value<Boolean>("Reset", this, true));
        this.damage = this.register(new Value<Float>("Damage", this, 0.7f, 0.0f, 1.0f)).visibleWhen(aFloat -> this.mode.getValue() == Mode.DAMAGE && this.tweaks.getValue());
        this.noBreakAnim = this.register(new Value<Boolean>("NoBreakAnim", this, false));
        this.noDelay = this.register(new Value<Boolean>("NoDelay", this, false));
        this.noSwing = this.register(new Value<Boolean>("NoSwing", this, false));
        this.noTrace = this.register(new Value<Boolean>("NoTrace", this, false));
        this.allow = this.register(new Value<Boolean>("AllowMultiTask", this, false));
        this.pickaxe = this.register(new Value<Boolean>("Pickaxe", this, true)).visibleWhen(aBoolean -> this.noTrace.getValue());
        this.doubleBreak = this.register(new Value<Boolean>("DoubleBreak", this, false));
        this.webSwitch = this.register(new Value<Boolean>("WebSwitch", this, false));
        this.render = this.register(new Value<Boolean>("Render", this, false));
        this.box = this.register(new Value<Boolean>("Box", this, false)).visibleWhen(aBoolean -> this.render.getValue());
        this.outline = this.register(new Value<Boolean>("Outline", this, true)).visibleWhen(aBoolean -> this.render.getValue());
        this.boxAlpha = this.register(new Value<Integer>("BoxAlpha", this, 85, 0, 255)).visibleWhen(integer -> this.box.getValue() && this.render.getValue());
        this.lineWidth = this.register(new Value<Float>("LineWidth", this, 1.0f, 0.1f, 5.0f)).visibleWhen(aFloat -> this.outline.getValue() && this.render.getValue());
        this.mainBreak = new PacketBreak();
        this.doubleBreakBreak = new PacketBreak();
        this.pendingDoubleBreak = null;
        this.lastDoubleBreakSource = null;
        Speedmine.INSTANCE = this;
    }

    public static Speedmine getInstance() {
        return Speedmine.INSTANCE;
    }

    @Override
    public void onUpdate() {
        if (Speedmine.mc.level == null || Speedmine.mc.player == null) {
            return;
        }
        if (this.mainBreak.pos != null && !Speedmine.mc.options.keyAttack.isDown()) {
            this.abortBreak(this.mainBreak);
        }
        this.tickBreak(this.mainBreak);
        if (this.currentPos != null) {
            final BlockState state = Speedmine.mc.level.getBlockState(this.currentPos);
            if (!state.equals(this.currentBlockState) || state.isAir()) {
                this.currentPos = null;
                this.currentBlockState = null;
                this.lastDoubleBreakSource = null;
            }
            else if (this.webSwitch.getValue() && this.currentBlockState.is(Blocks.COBWEB) && Speedmine.mc.player.getMainHandItem().getItem() instanceof PickaxeItem) {
                switchToHotbarSlot(SwordItem.class, false);
            }
        }
        if (!Speedmine.mc.options.keyAttack.isDown() && this.mainBreak.pos == null && this.currentPos != null) {
            this.pendingDoubleBreak = null;
            this.lastDoubleBreakSource = null;
        }
        this.startPendingDoubleBreak();
        this.tickBreak(this.doubleBreakBreak);
        if (this.noDelay.getValue()) {
            ((AccessorMultiPlayerGameMode)Speedmine.mc.gameMode).xulu$setDestroyDelay(0);
        }
        if (this.reset.getValue() && Speedmine.mc.options.keyUse.isDown() && !this.allow.getValue()) {
            ((AccessorMultiPlayerGameMode)Speedmine.mc.gameMode).xulu$setDestroying(false);
        }
    }

    @EventTarget
    public void onPacketSend(final EventSendPacket event) {
        if (Speedmine.mc.level == null || Speedmine.mc.player == null) {
            return;
        }
        if (event.getEventState() == Event.State.PRE && this.noSwing.getValue() && event.getPacket() instanceof ServerboundSwingPacket) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onClickBlock(final EventClickBlock event) {
        if (Speedmine.mc.player == null || Speedmine.mc.level == null) {
            return;
        }
        final AccessorMultiPlayerGameMode accessor = (AccessorMultiPlayerGameMode)Speedmine.mc.gameMode;
        if (this.reset.getValue() && accessor.xulu$getDestroyProgress() > 0.1f) {
            accessor.xulu$setDestroying(true);
        }
    }

    @EventTarget
    public void onBlockEvent(final EventPlayerDamageBlock event) {
        if (Speedmine.mc.player == null || Speedmine.mc.level == null) {
            return;
        }
        final BlockPos pos = event.getPos();
        if (!canBreak(pos)) {
            return;
        }
        this.currentPos = pos.immutable();
        this.currentBlockState = Speedmine.mc.level.getBlockState(pos);
        this.queueDoubleBreak(pos);
        if (!this.tweaks.getValue()) {
            return;
        }
        if (this.noTrace.getValue()) {
            final ItemStack held = Speedmine.mc.player.getMainHandItem();
            if (this.pickaxe.getValue() ? !(held.getItem() instanceof PickaxeItem) : !(held.getItem() instanceof DiggerItem)) {
                return;
            }
        }
        final AccessorMultiPlayerGameMode accessor = (AccessorMultiPlayerGameMode)Speedmine.mc.gameMode;
        if (this.reset.getValue()) {
            accessor.xulu$setDestroying(false);
        }
        switch (this.mode.getValue()) {
            case PACKET: {
                if (this.doubleBreakBreak.pos != null && !this.doubleBreakBreak.pos.equals(pos)) {
                    final BlockPos above = this.doubleBreakBreak.pos;
                    this.abortBreak(this.doubleBreakBreak);
                    if (canBreak(above)) {
                        this.pendingDoubleBreak = above;
                    }
                }
                if (this.mainBreak.pos == null || !this.mainBreak.pos.equals(pos)) {
                    this.abortBreak(this.mainBreak);
                    this.beginBreak(this.mainBreak, pos, event.getFacing());
                }
                event.setCancelled(true);
                break;
            }
            case DAMAGE: {
                if (accessor.xulu$getDestroyProgress() >= this.damage.getValue()) {
                    accessor.xulu$setDestroyProgress(1.0f);
                }
                break;
            }
            case INSTANT: {
                Speedmine.mc.player.swing(InteractionHand.MAIN_HAND);
                Speedmine.mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, event.getFacing()));
                Speedmine.mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos, event.getFacing()));
                Speedmine.mc.gameMode.destroyBlock(pos);
                event.setCancelled(true);
                break;
            }
        }
    }

    @Override
    public void onDisable() {
        this.abortBreak(this.mainBreak);
        this.abortBreak(this.doubleBreakBreak);
        this.pendingDoubleBreak = null;
        this.lastDoubleBreakSource = null;
        this.currentPos = null;
        this.currentBlockState = null;
    }

    @Override
    public void onWorldRender(final RenderEvent event) {
        if (!this.render.getValue() || Speedmine.mc.level == null) {
            return;
        }
        final BlockPos pos = this.mainBreak.pos != null ? this.mainBreak.pos : (this.doubleBreakBreak.pos != null ? this.doubleBreakBreak.pos : this.currentPos);
        if (pos == null) {
            return;
        }
        final BlockState state = Speedmine.mc.level.getBlockState(pos);
        if (state.isAir()) {
            return;
        }
        if (this.box.getValue()) {
            XuluTessellator.prepare(7);
            XuluTessellator.drawBox(pos, 255, 255, 255, this.boxAlpha.getValue(), 63);
            XuluTessellator.release();
        }
        if (this.outline.getValue()) {
            final Vec3 renderPos = event.getRenderPos();
            final AABB bounds = BlockInteractionHelper.getBlockBounds(state, Speedmine.mc.level, pos).inflate(0.002).move(-renderPos.x, -renderPos.y, -renderPos.z);
            XuluTessellator.drawBoundingBox(bounds, this.lineWidth.getValue(), 255, 255, 255, 255);
        }
    }

    private void queueDoubleBreak(final BlockPos pos) {
        if (!this.doubleBreak.getValue() || !this.tweaks.getValue() || pos.equals(this.lastDoubleBreakSource)) {
            return;
        }
        this.lastDoubleBreakSource = pos.immutable();
        final BlockPos above = pos.above();
        if (above.equals(this.mainBreak.pos) || above.equals(this.doubleBreakBreak.pos)) {
            return;
        }
        if (canBreak(above) && Speedmine.mc.player.distanceToSqr(Vec3.atCenterOf(above)) <= 25.0) {
            this.pendingDoubleBreak = above.immutable();
        }
    }

    private void startPendingDoubleBreak() {
        if (this.pendingDoubleBreak == null || this.mainBreak.pos != null || this.currentPos != null || this.doubleBreakBreak.pos != null) {
            return;
        }
        final BlockPos pos = this.pendingDoubleBreak;
        this.pendingDoubleBreak = null;
        if (!canBreak(pos) || Speedmine.mc.player.distanceToSqr(Vec3.atCenterOf(pos)) > 25.0) {
            return;
        }
        this.beginBreak(this.doubleBreakBreak, pos, Direction.UP);
    }

    private void beginBreak(final PacketBreak packetBreak, final BlockPos pos, final Direction facing) {
        packetBreak.pos = pos.immutable();
        packetBreak.state = Speedmine.mc.level.getBlockState(pos);
        packetBreak.startTick = Speedmine.mc.player.tickCount;
        packetBreak.facing = facing;
        packetBreak.stopSent = false;
        Speedmine.mc.player.swing(InteractionHand.MAIN_HAND);
        Speedmine.mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, packetBreak.pos, facing));
    }

    private void abortBreak(final PacketBreak packetBreak) {
        if (packetBreak.pos != null && !packetBreak.stopSent) {
            Speedmine.mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, packetBreak.pos, Direction.DOWN));
        }
        packetBreak.reset();
    }

    private void tickBreak(final PacketBreak packetBreak) {
        if (packetBreak.pos == null) {
            return;
        }
        final BlockState state = Speedmine.mc.level.getBlockState(packetBreak.pos);
        if (state.isAir() || state.getBlock() != packetBreak.state.getBlock()) {
            packetBreak.reset();
            return;
        }
        if (Speedmine.mc.player.tickCount - packetBreak.startTick > 100) {
            this.abortBreak(packetBreak);
            return;
        }
        final float progress = state.getDestroyProgress(Speedmine.mc.player, Speedmine.mc.level, packetBreak.pos) * (Speedmine.mc.player.tickCount - packetBreak.startTick + 1);
        if (progress < BREAK_MARGIN) {
            return;
        }
        if (!packetBreak.stopSent) {
            packetBreak.stopSent = true;
            Speedmine.mc.player.swing(InteractionHand.MAIN_HAND);
        }
        Speedmine.mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, packetBreak.pos, packetBreak.facing));
    }

    private static void switchToHotbarSlot(final Class<?> itemClass, final boolean silent) {
        final int slot = findHotbarSlot(itemClass);
        if (slot != -1) {
            switchToHotbarSlot(slot, silent);
        }
    }

    private static int findHotbarSlot(final Class<?> itemClass) {
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = Speedmine.mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) {
                return i;
            }
        }
        return -1;
    }

    private static void switchToHotbarSlot(final int slot, final boolean silent) {
        if (Speedmine.mc.player.getInventory().selected == slot || slot < 0) {
            return;
        }
        Speedmine.mc.player.connection.send(new ServerboundSetCarriedItemPacket(slot));
        if (!silent) {
            Speedmine.mc.player.getInventory().selected = slot;
        }
    }

    @Override
    public String getHudInfo() {
        return this.mode.getValue().name();
    }

    public static boolean canBreak(final BlockPos pos) {
        final BlockState blockState = Speedmine.mc.level.getBlockState(pos);
        return !blockState.isAir() && !blockState.getMaterial().isLiquid() && blockState.getDestroySpeed(Speedmine.mc.level, pos) != -1.0f;
    }

    public enum Mode
    {
        PACKET,
        DAMAGE,
        INSTANT;
    }

    private static final class PacketBreak
    {
        BlockPos pos;
        BlockState state;
        int startTick;
        Direction facing;
        boolean stopSent;

        void reset() {
            this.pos = null;
            this.state = null;
            this.startTick = 0;
            this.facing = null;
            this.stopSent = false;
        }
    }
}
