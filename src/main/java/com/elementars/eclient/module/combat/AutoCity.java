package com.elementars.eclient.module.combat;

import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventReceivePacket;
import com.elementars.eclient.event.events.EventSendPacket;
import com.elementars.eclient.event.events.RenderEvent;
import com.elementars.eclient.friend.Friends;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.BlockInteractionHelper;
import com.elementars.eclient.util.XuluTessellator;
import dev.xulu.settings.Value;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AutoCity extends Module
{
    private static final float BREAK_THRESHOLD = 0.7f;
    private static final Direction[] HORIZONTALS = { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };

    private final Value<Integer> range;
    private final Value<Integer> targetRange;
    private final Value<Integer> delay;
    private final Value<String> mode;
    private final Value<Boolean> rotate;
    private final Value<Boolean> walls;
    private final Value<Boolean> onlyHole;
    private final Value<Boolean> feet;
    private final Value<Boolean> head;
    private final Value<Boolean> instantRebreak;
    private final Value<Boolean> switchToPickaxe;
    private final Value<Boolean> swing;
    private final Value<Boolean> render;
    private final Value<Boolean> box;
    private final Value<Boolean> outline;
    private final Value<Integer> boxAlpha;
    private final Value<Float> lineWidth;

    private Player target;
    private BlockPos cityPos;
    private BlockPos activePos;
    private BlockState activeState;
    private Direction activeFacing;
    private int startTick;
    private boolean stopSent;
    private int delayTicks;
    private boolean finishing;
    private boolean waitingRebreak;
    private boolean serverStarted;
    private int instantRebreakTicks;
    private BlockPos lastBrokenPos;
    private BlockPos lastStartedPos;
    private float activeDamage;
    private float lastDamage;

    public AutoCity() {
        super("AutoCity", "Breaks the blocks protecting your target.", 0, Category.COMBAT, true);
        this.range = this.register(new Value<Integer>("Range", this, 4, 1, 10));
        this.targetRange = this.register(new Value<Integer>("TargetRange", this, 6, 1, 10));
        this.delay = this.register(new Value<Integer>("Delay", this, 2, 0, 20));
        this.mode = this.register(new Value<String>("Mode", this, "Packet", new ArrayList<String>(java.util.Arrays.asList("Packet", "Legit"))));
        this.rotate = this.register(new Value<Boolean>("Rotate", this, true));
        this.walls = this.register(new Value<Boolean>("Walls", this, false));
        this.onlyHole = this.register(new Value<Boolean>("OnlyHole", this, true));
        this.feet = this.register(new Value<Boolean>("Feet", this, true));
        this.head = this.register(new Value<Boolean>("Head", this, false));
        this.instantRebreak = this.register(new Value<Boolean>("InstantRebreak", this, true));
        this.switchToPickaxe = this.register(new Value<Boolean>("Switch", this, true));
        this.swing = this.register(new Value<Boolean>("Swing", this, true));
        this.render = this.register(new Value<Boolean>("Render", this, true));
        this.box = this.register(new Value<Boolean>("Box", this, true)).visibleWhen(aBoolean -> this.render.getValue());
        this.outline = this.register(new Value<Boolean>("Outline", this, true)).visibleWhen(aBoolean -> this.render.getValue());
        this.boxAlpha = this.register(new Value<Integer>("BoxAlpha", this, 85, 0, 255)).visibleWhen(integer -> this.box.getValue() && this.render.getValue());
        this.lineWidth = this.register(new Value<Float>("LineWidth", this, 1.0f, 0.1f, 5.0f)).visibleWhen(aFloat -> this.outline.getValue() && this.render.getValue());
        this.target = null;
        this.cityPos = null;
        this.activePos = null;
        this.activeState = null;
        this.activeFacing = Direction.UP;
        this.delayTicks = 0;
        this.finishing = false;
        this.waitingRebreak = false;
        this.serverStarted = false;
        this.instantRebreakTicks = 0;
        this.lastBrokenPos = null;
        this.lastStartedPos = null;
        this.activeDamage = 0.0f;
        this.lastDamage = 0.0f;
    }

    @Override
    public void onUpdate() {
        if (AutoCity.mc.level == null || AutoCity.mc.player == null) {
            return;
        }
        if (this.delayTicks > 0) {
            --this.delayTicks;
        }
        this.updateTarget();
        if (this.target == null) {
            if (this.cityPos != null) {
                this.resetCity(true);
            }
            return;
        }
        if (this.cityPos != null && !this.isCityBlockValid(this.cityPos) && !this.shouldKeepCityLock()) {
            this.resetCity(true);
            this.delayTicks = this.delay.getValue();
            return;
        }
        if (this.cityPos == null) {
            if (this.delayTicks > 0) {
                return;
            }
            final BlockPos pos = this.findCityBlock();
            if (pos == null) {
                return;
            }
            this.cityPos = pos;
        }
        final BlockState cityState = AutoCity.mc.level.getBlockState(this.cityPos);
        if (cityState.isAir()) {
            if (this.activePos != null || !this.instantRebreak.getValue()) {
                this.finish();
                return;
            }
            this.waitingRebreak = true;
            return;
        }
        if (this.activePos == null) {
            if (this.waitingRebreak || (this.instantRebreak.getValue() && this.serverStarted)) {
                if (this.tryInstantRebreak(cityState)) {
                    return;
                }
            }
            if (this.delayTicks > 0) {
                return;
            }
            this.startMine(this.cityPos);
            return;
        }
        if (!this.activePos.equals(this.cityPos)) {
            this.hardAbort();
            return;
        }
        if (this.finishing) {
            if (!this.isMineable(this.activePos)) {
                this.hardAbort();
                return;
            }
            this.tickMine();
            return;
        }
        if (!this.isMineable(this.activePos) || !this.withinRange(this.activePos)) {
            this.abort();
            this.delayTicks = this.delay.getValue();
            return;
        }
        this.tickMine();
    }

    private void updateTarget() {
        if (this.target != null && this.isTargetReachable(this.target)
                && (this.cityPos != null || !this.onlyHole.getValue() || this.isHole(this.target.blockPosition()))) {
            return;
        }
        final Player previous = this.target;
        this.target = AutoCity.mc.level.players().stream()
                .filter(this::isValidTarget)
                .min(Comparator.comparingDouble(player -> AutoCity.mc.player.distanceToSqr(player)))
                .orElse(null);
        if (this.target != previous) {
            this.lastBrokenPos = null;
            this.resetCity(true);
            this.delayTicks = this.delay.getValue();
        }
    }

    private boolean isTargetReachable(final Player player) {
        if (player == AutoCity.mc.player || !player.isAlive() || player.isSpectator() || player.isDeadOrDying()) {
            return false;
        }
        if (Friends.isFriend(player.getName().getString())) {
            return false;
        }
        return AutoCity.mc.player.distanceTo(player) <= this.targetRange.getValue();
    }

    private boolean isValidTarget(final Player player) {
        return this.isTargetReachable(player)
                && (!this.onlyHole.getValue() || this.isHole(player.blockPosition()));
    }

    private boolean isHole(final BlockPos feetPos) {
        if (!AutoCity.mc.level.getBlockState(feetPos.below()).getMaterial().blocksMotion()) {
            return false;
        }
        int solid = 0;
        for (final Direction direction : HORIZONTALS) {
            if (AutoCity.mc.level.getBlockState(feetPos.relative(direction)).getMaterial().blocksMotion()) {
                ++solid;
            }
        }
        return solid >= 2;
    }

    private boolean isCityBlockValid(final BlockPos pos) {
        if (this.target == null) {
            return false;
        }
        if (!AutoCity.mc.level.getBlockState(pos).isAir() && (!this.withinRange(pos) || !this.isMineable(pos))) {
            return false;
        }
        final BlockPos base = this.target.blockPosition();
        for (final Direction direction : HORIZONTALS) {
            if (this.feet.getValue() && pos.equals(base.relative(direction))) {
                return true;
            }
            if (this.head.getValue() && pos.equals(base.above().relative(direction))) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldKeepCityLock() {
        if (!this.instantRebreak.getValue() || this.cityPos == null || !this.withinRange(this.cityPos)) {
            return false;
        }
        final BlockState state = AutoCity.mc.level.getBlockState(this.cityPos);
        if (state.isAir()) {
            return this.waitingRebreak;
        }
        return this.activePos != null && this.activePos.equals(this.cityPos);
    }

    private BlockPos findCityBlock() {
        final BlockPos base = this.target.blockPosition();
        final List<BlockPos> candidates = new ArrayList<BlockPos>();
        if (this.feet.getValue()) {
            for (final Direction direction : HORIZONTALS) {
                candidates.add(base.relative(direction));
            }
        }
        if (this.head.getValue()) {
            for (final Direction direction : HORIZONTALS) {
                candidates.add(base.above().relative(direction));
            }
        }
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (final BlockPos pos : candidates) {
            if (!this.instantRebreak.getValue() && pos.equals(this.lastBrokenPos)) {
                continue;
            }
            if (!this.isMineable(pos) || !this.withinRange(pos)) {
                continue;
            }
            if (this.walls.getValue() && !this.canSee(pos)) {
                continue;
            }
            final double distance = AutoCity.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = pos;
            }
        }
        return best != null ? best.immutable() : null;
    }

    private boolean isMineable(final BlockPos pos) {
        return this.isMineable(pos, AutoCity.mc.level.getBlockState(pos));
    }

    private boolean isMineable(final BlockPos pos, final BlockState state) {
        return !state.isAir()
                && !state.getMaterial().isLiquid()
                && state.getMaterial().blocksMotion()
                && state.getDestroySpeed(AutoCity.mc.level, pos) != -1.0f;
    }

    private boolean withinRange(final BlockPos pos) {
        final double rangeSq = (double)this.range.getValue() * (double)this.range.getValue();
        return AutoCity.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) <= rangeSq;
    }

    private boolean canSee(final BlockPos pos) {
        final Vec3 eyes = AutoCity.mc.player.getEyePosition();
        final BlockHitResult hit = AutoCity.mc.level.clip(new net.minecraft.world.level.ClipContext(eyes, Vec3.atCenterOf(pos), net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, AutoCity.mc.player));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(pos);
    }

    private void startMine(final BlockPos pos) {
        final BlockState state = AutoCity.mc.level.getBlockState(pos);
        if (!this.isMineable(pos) || !this.canMine(state) || !this.withinRange(pos)) {
            this.delayTicks = 20;
            return;
        }
        this.cityPos = pos.immutable();
        this.activeState = state;
        if (this.rotate.getValue()) {
            BlockInteractionHelper.faceVectorPacketInstant(Vec3.atCenterOf(pos));
        }
        this.activePos = pos.immutable();
        this.activeFacing = Direction.UP;
        this.startTick = AutoCity.mc.player.tickCount;
        this.stopSent = false;
        this.finishing = false;
        this.waitingRebreak = false;
        this.instantRebreakTicks = 0;
        this.activeDamage = 0.0f;
        this.lastDamage = 0.0f;
        if (this.swing.getValue()) {
            AutoCity.mc.player.swing(InteractionHand.MAIN_HAND);
        }
        this.withPickaxe(state, () -> AutoCity.mc.player.connection.send(
                new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.activePos, this.activeFacing)));
        this.serverStarted = true;
    }

    private boolean tryInstantRebreak(final BlockState state) {
        if (!this.instantRebreak.getValue() || this.cityPos == null || !this.withinRange(this.cityPos)
                || !this.isMineable(this.cityPos, state)) {
            return false;
        }
        if (AutoCity.mc.player.isCreative()) {
            return this.creativeRebreak(state);
        }
        if (!this.canMine(state)) {
            return false;
        }
        if (this.lastStartedPos == null || !this.lastStartedPos.equals(this.cityPos)) {
            return false;
        }
        this.activePos = this.cityPos.immutable();
        this.activeState = state;
        this.activeFacing = Direction.UP;
        this.startTick = AutoCity.mc.player.tickCount;
        this.stopSent = false;
        this.finishing = false;
        this.waitingRebreak = false;
        this.instantRebreakTicks = 1;
        this.activeDamage = this.getBreakThreshold();
        this.lastDamage = this.activeDamage;
        if (this.rotate.getValue()) {
            BlockInteractionHelper.faceVectorPacketInstant(Vec3.atCenterOf(this.cityPos));
        }
        if (this.swing.getValue()) {
            AutoCity.mc.player.swing(InteractionHand.MAIN_HAND);
        }
        this.sendStopAndAbort(state);
        this.stopSent = true;
        return true;
    }

    private void sendStopAndAbort(final BlockState state) {
        this.withPickaxe(state, () -> {
            AutoCity.mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.activePos, this.activeFacing));
            AutoCity.mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.activePos, this.activeFacing));
        });
    }

    private boolean creativeRebreak(final BlockState state) {
        this.activePos = this.cityPos.immutable();
        this.activeState = state;
        this.activeFacing = Direction.UP;
        this.startTick = AutoCity.mc.player.tickCount;
        this.stopSent = false;
        this.finishing = false;
        this.waitingRebreak = false;
        this.instantRebreakTicks = 1;
        this.activeDamage = this.getBreakThreshold();
        this.lastDamage = this.activeDamage;
        if (this.rotate.getValue()) {
            BlockInteractionHelper.faceVectorPacketInstant(Vec3.atCenterOf(this.cityPos));
        }
        if (this.swing.getValue()) {
            AutoCity.mc.player.swing(InteractionHand.MAIN_HAND);
        }
        AutoCity.mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.activePos, this.activeFacing));
        AutoCity.mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.activePos, this.activeFacing));
        this.lastStartedPos = this.activePos.immutable();
        this.serverStarted = true;
        this.stopSent = true;
        return true;
    }

    private void tickMine() {
        final BlockState state = AutoCity.mc.level.getBlockState(this.activePos);
        if (state.isAir()) {
            this.finish();
            return;
        }
        if (!this.canMine(state)) {
            this.abort();
            this.delayTicks = this.delay.getValue();
            return;
        }
        if (state.getBlock() != this.activeState.getBlock()) {
            if (!this.instantRebreak.getValue()) {
                this.lastBrokenPos = this.cityPos;
                this.hardAbort();
                this.cityPos = null;
                this.waitingRebreak = false;
                this.instantRebreakTicks = 0;
                this.delayTicks = this.delay.getValue();
                return;
            }
            if (!this.tryInstantRebreak(state)) {
                this.startMine(this.cityPos);
            }
            return;
        }
        if (this.instantRebreakTicks > 0) {
            ++this.instantRebreakTicks;
            if (this.instantRebreakTicks > 10) {
                this.instantRebreakTicks = 0;
                this.startMine(this.cityPos);
            }
            return;
        }
        final float perTick = this.currentProgress(state, 0);
        final float threshold = this.getBreakThreshold();
        final int elapsed = AutoCity.mc.player.tickCount - this.startTick;
        final int timeout = Math.min(400, Math.max(100,
                (int)Math.ceil((double)(threshold + 0.05f) / (double)Math.max(perTick, 1.0E-4f)) + 20));
        this.lastDamage = this.activeDamage;
        this.activeDamage += perTick;
        if (this.activeDamage < threshold) {
            if (elapsed > timeout) {
                this.hardAbort();
                this.delayTicks = 10;
            }
            return;
        }
        if (!this.stopSent) {
            this.stopSent = true;
            if (this.swing.getValue()) {
                AutoCity.mc.player.swing(InteractionHand.MAIN_HAND);
            }
            this.sendStopAndAbort(state);
        }
        if (elapsed > timeout) {
            this.hardAbort();
            this.delayTicks = 10;
        }
    }

    private float getBreakThreshold() {
        return this.mode.getValue().equalsIgnoreCase("Packet") ? BREAK_THRESHOLD + 0.05f : 1.0f;
    }

    private float currentProgress(final BlockState state, final int elapsed) {
        final int selected = AutoCity.mc.player.getInventory().selected;
        final int pickaxe = this.findPickaxeSlot(state);
        if (this.switchToPickaxe.getValue() && pickaxe != -1) {
            AutoCity.mc.player.getInventory().selected = pickaxe;
        }
        final float progress = state.getDestroyProgress(AutoCity.mc.player, AutoCity.mc.level, this.activePos) * (float)(elapsed + 1);
        AutoCity.mc.player.getInventory().selected = selected;
        return progress;
    }

    private void finish() {
        if (!this.instantRebreak.getValue()) {
            this.lastBrokenPos = this.cityPos != null ? this.cityPos : this.activePos;
            this.finishing = false;
            this.clearActive();
            this.cityPos = null;
            this.waitingRebreak = false;
            this.serverStarted = false;
            this.instantRebreakTicks = 0;
            this.activeDamage = 0.0f;
            this.lastDamage = 0.0f;
            this.delayTicks = this.delay.getValue();
            return;
        }
        this.finishing = false;
        this.clearActive();
        this.waitingRebreak = true;
        this.instantRebreakTicks = 0;
        this.delayTicks = 0;
    }

    private void abort() {
        if (this.activePos == null) {
            this.finishing = false;
            return;
        }
        if (!this.stopSent) {
            AutoCity.mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.activePos, Direction.DOWN));
            this.finishing = false;
            this.serverStarted = false;
            this.clearActive();
            return;
        }
        this.finishing = true;
    }

    private void hardAbort() {
        if (this.activePos != null) {
            AutoCity.mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.activePos, Direction.DOWN));
        }
        this.finishing = false;
        this.clearActive();
        this.serverStarted = false;
    }

    private void resetCity(final boolean abort) {
        if (abort) {
            this.hardAbort();
        }
        else {
            this.finishing = false;
            this.clearActive();
        }
        this.cityPos = null;
        this.waitingRebreak = false;
        this.instantRebreakTicks = 0;
        this.serverStarted = false;
    }

    private void clearActive() {
        this.activePos = null;
        this.activeState = null;
        this.stopSent = false;
    }

    private boolean canMine(final BlockState state) {
        return !this.switchToPickaxe.getValue() || this.findPickaxeSlot(state) != -1;
    }

    private void withPickaxe(final BlockState state, final Runnable action) {
        if (!this.switchToPickaxe.getValue()) {
            action.run();
            return;
        }
        final int slot = this.findPickaxeSlot(state);
        if (slot == -1) {
            return;
        }
        final int restore = AutoCity.mc.player.getInventory().selected;
        if (slot != restore) {
            AutoCity.mc.player.connection.send(new ServerboundSetCarriedItemPacket(slot));
            AutoCity.mc.player.getInventory().selected = slot;
        }
        try {
            action.run();
        }
        finally {
            if (slot != restore) {
                AutoCity.mc.player.connection.send(new ServerboundSetCarriedItemPacket(restore));
                AutoCity.mc.player.getInventory().selected = restore;
            }
        }
    }

    private int findPickaxeSlot(final BlockState state) {
        int best = -1;
        float bestSpeed = -1.0f;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = AutoCity.mc.player.getInventory().getItem(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof PickaxeItem)) {
                continue;
            }
            final float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                best = i;
            }
        }
        return best;
    }

    @EventTarget
    public void onSend(final EventSendPacket event) {
        if (!(event.getPacket() instanceof ServerboundPlayerActionPacket)) {
            return;
        }
        final ServerboundPlayerActionPacket packet = (ServerboundPlayerActionPacket)event.getPacket();
        if (packet.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            this.lastStartedPos = packet.getPos().immutable();
        }
    }

    @EventTarget
    public void onReceive(final EventReceivePacket event) {
        if (!this.instantRebreak.getValue() || this.cityPos == null || this.target == null) {
            return;
        }
        if (event.getPacket() instanceof ClientboundBlockUpdatePacket) {
            final ClientboundBlockUpdatePacket packet = (ClientboundBlockUpdatePacket)event.getPacket();
            this.handleCityBlockUpdate(packet.getPos(), packet.getBlockState());
        }
        else if (event.getPacket() instanceof ClientboundSectionBlocksUpdatePacket) {
            ((ClientboundSectionBlocksUpdatePacket)event.getPacket()).runUpdates(this::handleCityBlockUpdate);
        }
    }

    private void handleCityBlockUpdate(final BlockPos pos, final BlockState state) {
        if (this.cityPos == null || !this.cityPos.equals(pos)) {
            return;
        }
        if (state.isAir()) {
            if (this.activePos != null) {
                this.finish();
            }
            else {
                this.waitingRebreak = true;
            }
            return;
        }
        if (this.activePos != null) {
            if (this.activePos.equals(this.cityPos) && this.activeState != null
                    && state.getBlock() != this.activeState.getBlock()) {
                this.tryInstantRebreak(state);
            }
            return;
        }
        this.tryInstantRebreak(state);
    }

    public BlockPos getCityPos() {
        return this.cityPos == null ? null : this.cityPos.immutable();
    }

    public Player getTargetPlayer() {
        return this.target;
    }

    public BlockPos getMiningBlock() {
        if (this.cityPos == null || this.activePos == null || !this.activePos.equals(this.cityPos)) {
            return null;
        }
        return this.activeDamage / Math.max(this.getBreakThreshold(), 1.0E-4f) > 0.75f ? this.cityPos.immutable() : null;
    }

    public float getBlockDamage() {
        return this.activeDamage;
    }

    public boolean isMiningActive() {
        return this.activePos != null && this.cityPos != null;
    }

    public boolean isInstantMineComplete() {
        return this.activePos == null || this.cityPos == null || AutoCity.mc.level == null
                || AutoCity.mc.level.getBlockState(this.cityPos).isAir();
    }

    @Override
    public void onEnable() {
        this.target = null;
        this.cityPos = null;
        this.activePos = null;
        this.activeState = null;
        this.activeFacing = Direction.UP;
        this.delayTicks = 0;
        this.finishing = false;
        this.waitingRebreak = false;
        this.serverStarted = false;
        this.instantRebreakTicks = 0;
        this.lastBrokenPos = null;
        this.lastStartedPos = null;
        this.activeDamage = 0.0f;
        this.lastDamage = 0.0f;
    }

    @Override
    public void onDisable() {
        this.hardAbort();
        this.target = null;
        this.cityPos = null;
        this.lastBrokenPos = null;
        this.lastStartedPos = null;
        this.activeDamage = 0.0f;
        this.lastDamage = 0.0f;
        this.delayTicks = 0;
        this.waitingRebreak = false;
        this.serverStarted = false;
    }

    @Override
    public void onWorldRender(final RenderEvent event) {
        if (!this.render.getValue() || this.activePos == null || AutoCity.mc.level == null) {
            return;
        }
        final BlockState state = AutoCity.mc.level.getBlockState(this.activePos);
        if (state.isAir()) {
            return;
        }
        if (this.box.getValue()) {
            XuluTessellator.prepare(7);
            XuluTessellator.drawBox(this.activePos, 255, 80, 80, this.boxAlpha.getValue(), 63);
            XuluTessellator.release();
        }
        if (this.outline.getValue()) {
            final Vec3 renderPos = event.getRenderPos();
            final AABB bounds = BlockInteractionHelper.getBlockBounds(state, AutoCity.mc.level, this.activePos).inflate(0.002).move(-renderPos.x, -renderPos.y, -renderPos.z);
            XuluTessellator.drawBoundingBox(bounds, this.lineWidth.getValue(), 255, 80, 80, 255);
        }
    }

    @Override
    public String getHudInfo() {
        return this.target != null ? this.target.getName().getString() : "";
    }
}
