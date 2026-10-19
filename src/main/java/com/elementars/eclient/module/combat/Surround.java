package com.elementars.eclient.module.combat;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.mixin.mixins.MixinMinecraft;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.module.ModuleManager;
import com.elementars.eclient.util.BlockInteractionHelper;
import dev.xulu.settings.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class Surround extends Module
{
    private final Value<String> mode;
    private final Value<Boolean> triggerable;
    private final Value<Boolean> turnOffCauras;
    private final Value<Integer> timeoutTicks;
    private final Value<Integer> blocksPerTick;
    private final Value<Integer> tickDelay;
    private final Value<Boolean> rotate;
    private final Value<Boolean> noGlitchBlocks;
    private final Value<Boolean> announceUsage;
    private final Value<Boolean> expand;
    private final Value<Boolean> selfDisable;
    private final Value<Boolean> disableOnMove;
    private final Value<Boolean> disableOnYChange;
    private int playerHotbarSlot;
    private int lastHotbarSlot;
    private int offsetStep;
    private int delayStep;
    private int totalTicksRunning;
    private boolean firstRun;
    private boolean isSneaking;
    double oldY;
    double oldX;
    double oldZ;
    int cDelay;
    String caura;
    boolean isDisabling;
    boolean hasDisabled;

    public Surround() {
        super("Surround", "Surrounds you with obby", 0, Category.COMBAT, true);
        this.mode = this.register(new Value<String>("Mode", this, "Full", new ArrayList<String>(Arrays.asList("Full", "Surround", "Double", "FullCity", "SurroundCity"))));
        this.triggerable = this.register(new Value<Boolean>("Triggerable", this, true));
        this.turnOffCauras = this.register(new Value<Boolean>("Toggle Other Cauras", this, false));
        this.timeoutTicks = this.register(new Value<Integer>("TimeoutTicks", this, 40, 1, 100));
        this.blocksPerTick = this.register(new Value<Integer>("BlocksPerTick", this, 4, 1, 9));
        this.tickDelay = this.register(new Value<Integer>("TickDelay", this, 0, 0, 10));
        this.rotate = this.register(new Value<Boolean>("Rotate", this, true));
        this.noGlitchBlocks = this.register(new Value<Boolean>("NoGlitchBlocks", this, true));
        this.announceUsage = this.register(new Value<Boolean>("AnnounceUsage", this, true));
        this.expand = this.register(new Value<Boolean>("Expand", this, true));
        this.selfDisable = this.register(new Value<Boolean>("SelfDisable", this, true));
        this.disableOnMove = this.register(new Value<Boolean>("DisableOnMove", this, true));
        this.disableOnYChange = this.register(new Value<Boolean>("DisableOnYChange", this, true));
        this.cDelay = 0;
        this.playerHotbarSlot = -1;
        this.lastHotbarSlot = -1;
        this.offsetStep = 0;
        this.delayStep = 0;
        this.totalTicksRunning = 0;
        this.isSneaking = false;
    }

    @Override
    public void onEnable() {
        if (Surround.mc.player == null) {
            this.disable();
            return;
        }
        this.hasDisabled = false;
        this.oldY = Surround.mc.player.getY();
        this.oldX = Surround.mc.player.getX();
        this.oldZ = Surround.mc.player.getZ();
        this.firstRun = true;
        this.offsetStep = 0;
        this.delayStep = 0;
        this.totalTicksRunning = 0;
        this.playerHotbarSlot = Surround.mc.player.getInventory().selected;
        this.lastHotbarSlot = -1;
        if (this.announceUsage.getValue()) {
            this.sendDebugMessage(ChatFormatting.GREEN.toString() + "Enabled!");
        }
    }

    @Override
    public void onDisable() {
        if (Surround.mc.player == null) {
            return;
        }
        if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
            Surround.mc.player.getInventory().selected = this.playerHotbarSlot;
        }
        if (this.isSneaking) {
            Surround.mc.player.connection.send(new ServerboundPlayerCommandPacket(Surround.mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
            this.isSneaking = false;
        }
        this.playerHotbarSlot = -1;
        this.lastHotbarSlot = -1;
        if (this.announceUsage.getValue()) {
            this.sendDebugMessage(ChatFormatting.RED.toString() + "Disabled!");
        }
    }

    @Override
    public void onUpdate() {
        if (this.cDelay > 0) {
            --this.cDelay;
        }
        if (this.cDelay == 0 && this.isDisabling) {
            Xulu.MODULE_MANAGER.getModuleByName(this.caura).toggle();
            this.isDisabling = false;
            this.hasDisabled = true;
        }
        if (Surround.mc.player == null || ModuleManager.isModuleEnabled("Freecam")) {
            return;
        }
        if (Xulu.MODULE_MANAGER.getModuleByName("AutoCrystal") != null && Xulu.MODULE_MANAGER.getModuleByName("AutoCrystal").isToggled() && this.turnOffCauras.getValue() && !this.hasDisabled) {
            this.caura = "AutoCrystal";
            this.cDelay = 19;
            this.isDisabling = true;
            Xulu.MODULE_MANAGER.getModuleByName(this.caura).toggle();
        }
        if (Xulu.MODULE_MANAGER.getModuleByName("AutoCrystalO") != null && Xulu.MODULE_MANAGER.getModuleByName("AutoCrystalO").isToggled() && this.turnOffCauras.getValue() && !this.hasDisabled) {
            this.caura = "AutoCrystalO";
            this.cDelay = 19;
            this.isDisabling = true;
            Xulu.MODULE_MANAGER.getModuleByName(this.caura).toggle();
        }
        if (Xulu.MODULE_MANAGER.getModuleByName("AutoCrystalX") != null && Xulu.MODULE_MANAGER.getModuleByName("AutoCrystalX").isToggled() && this.turnOffCauras.getValue() && !this.hasDisabled) {
            this.caura = "AutoCrystalX";
            this.cDelay = 19;
            this.isDisabling = true;
            Xulu.MODULE_MANAGER.getModuleByName(this.caura).toggle();
        }
        if (this.triggerable.getValue() && this.totalTicksRunning >= this.timeoutTicks.getValue()) {
            this.totalTicksRunning = 0;
            this.disable();
            return;
        }
        if (this.disableOnYChange.getValue() && Surround.mc.player.getY() != this.oldY) {
            this.disable();
            return;
        }
        if (this.disableOnMove.getValue() && (Math.abs(Surround.mc.player.getX() - this.oldX) > 1.0E-4 || Math.abs(Surround.mc.player.getZ() - this.oldZ) > 1.0E-4)) {
            this.disable();
            return;
        }
        if (!this.firstRun) {
            if (this.delayStep < this.tickDelay.getValue()) {
                ++this.delayStep;
                return;
            }
            this.delayStep = 0;
        }
        if (this.firstRun) {
            this.firstRun = false;
        }
        final Vec3[] offsetPattern = this.getOffsetPattern();
        final List<BlockPos> placeTargets = this.getPlaceTargets(offsetPattern);
        int blocksPlaced = 0;
        if (!placeTargets.isEmpty()) {
            if (this.offsetStep >= placeTargets.size()) {
                this.offsetStep = 0;
            }
            while (blocksPlaced < this.blocksPerTick.getValue() && this.offsetStep < placeTargets.size()) {
                if (this.placeBlock(placeTargets.get(this.offsetStep))) {
                    ++blocksPlaced;
                }
                ++this.offsetStep;
            }
            if (this.offsetStep >= placeTargets.size()) {
                this.offsetStep = 0;
            }
        }
        if (blocksPlaced > 0) {
            if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
                Surround.mc.player.getInventory().selected = this.playerHotbarSlot;
                this.lastHotbarSlot = this.playerHotbarSlot;
            }
            if (this.isSneaking) {
                Surround.mc.player.connection.send(new ServerboundPlayerCommandPacket(Surround.mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
                this.isSneaking = false;
            }
        }
        ++this.totalTicksRunning;
        if (this.selfDisable.getValue() && this.isPlacementComplete(placeTargets)) {
            this.disable();
        }
    }

    private Vec3[] getOffsetPattern() {
        if (this.mode.getValue().equalsIgnoreCase("Full")) {
            return Offsets.FULL;
        }
        if (this.mode.getValue().equalsIgnoreCase("Surround")) {
            return Offsets.SURROUND;
        }
        if (this.mode.getValue().equalsIgnoreCase("Double")) {
            return Offsets.DOUBLE;
        }
        if (this.mode.getValue().equalsIgnoreCase("FullCity")) {
            return Offsets.FULLCITY;
        }
        if (this.mode.getValue().equalsIgnoreCase("SurroundCity")) {
            return Offsets.SURROUNDCITY;
        }
        return new Vec3[0];
    }

    private List<BlockPos> getBasePositions() {
        final List<BlockPos> positions = new ArrayList<BlockPos>();
        if (!this.expand.getValue()) {
            positions.add(new BlockPos(Surround.mc.player.position()));
            return positions;
        }
        final AABB box = Surround.mc.player.getBoundingBox();
        final int minX = Mth.floor(box.minX);
        final int maxX = Mth.floor(box.maxX - 1.0E-7);
        final int minZ = Mth.floor(box.minZ);
        final int maxZ = Mth.floor(box.maxZ - 1.0E-7);
        final int y = Mth.floor(Surround.mc.player.getY());
        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                positions.add(new BlockPos(x, y, z));
            }
        }
        return positions;
    }

    private List<BlockPos> getPlaceTargets(final Vec3[] offsetPattern) {
        final Set<BlockPos> targets = new LinkedHashSet<BlockPos>();
        for (final BlockPos basePos : this.getBasePositions()) {
            for (final Vec3 offset : offsetPattern) {
                final BlockPos targetPos = basePos.offset((int)offset.x, (int)offset.y, (int)offset.z);
                if (Surround.mc.player.getBoundingBox().intersects(new AABB(targetPos))) {
                    continue;
                }
                targets.add(targetPos);
            }
        }
        return new ArrayList<BlockPos>(targets);
    }

    private boolean isPlacementComplete(final List<BlockPos> placeTargets) {
        if (placeTargets.isEmpty()) {
            return false;
        }
        for (final BlockPos pos : placeTargets) {
            final Block block = Surround.mc.level.getBlockState(pos).getBlock();
            if (block instanceof AirBlock || block instanceof LiquidBlock) {
                return false;
            }
        }
        return true;
    }

    private boolean placeBlock(final BlockPos pos) {
        final Block block = Surround.mc.level.getBlockState(pos).getBlock();
        if (!(block instanceof AirBlock) && !(block instanceof LiquidBlock)) {
            return false;
        }
        for (final Entity entity : Surround.mc.level.getEntities((Entity)null, new AABB(pos))) {
            if (!(entity instanceof ItemEntity) && !(entity instanceof ExperienceOrb)) {
                return false;
            }
        }
        final Direction side = BlockInteractionHelper.getPlaceableSide(pos);
        if (side == null) {
            return false;
        }
        final BlockPos neighbour = pos.relative(side);
        final Direction opposite = side.getOpposite();
        if (!BlockInteractionHelper.canBeClicked(neighbour)) {
            return false;
        }
        final Vec3 hitVec = Vec3.atLowerCornerOf(neighbour).add(0.5, 0.5, 0.5).add(Vec3.atLowerCornerOf(opposite.getNormal()).scale(0.5));
        final Block neighbourBlock = Surround.mc.level.getBlockState(neighbour).getBlock();
        final int obiSlot = this.findObiInHotbar();
        if (obiSlot == -1) {
            this.disable();
            return false;
        }
        if (this.lastHotbarSlot != obiSlot) {
            Surround.mc.player.getInventory().selected = obiSlot;
            this.lastHotbarSlot = obiSlot;
        }
        if ((!this.isSneaking && BlockInteractionHelper.blackList.contains(neighbourBlock)) || BlockInteractionHelper.shulkerList.contains(neighbourBlock)) {
            Surround.mc.player.connection.send(new ServerboundPlayerCommandPacket(Surround.mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
            this.isSneaking = true;
        }
        if (this.rotate.getValue()) {
            BlockInteractionHelper.faceVectorPacketInstant(hitVec);
        }
        Surround.mc.gameMode.useItemOn(Surround.mc.player, Surround.mc.level, InteractionHand.MAIN_HAND, new BlockHitResult(hitVec, opposite, neighbour, false));
        Surround.mc.player.swing(InteractionHand.MAIN_HAND);
        ((MixinMinecraft)Surround.mc).setRightClickDelay(4);
        if (this.noGlitchBlocks.getValue() && !Surround.mc.gameMode.getPlayerMode().equals(GameType.CREATIVE)) {
            Surround.mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, neighbour, opposite));
        }
        return true;
    }

    public int findObiInHotbar() {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = Surround.mc.player.getInventory().getItem(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof BlockItem) {
                final Block block = ((BlockItem)stack.getItem()).getBlock();
                if (block == Blocks.OBSIDIAN) {
                    slot = i;
                    break;
                }
            }
        }
        return slot;
    }

    public static boolean isExposed() {
        if (Surround.mc.level == null) {
            return false;
        }
        final BlockPos playerPos = new BlockPos(Surround.mc.player.position());
        return Surround.mc.level.getBlockState(playerPos.offset(1, 0, 0)).getBlock() == Blocks.AIR || Surround.mc.level.getBlockState(playerPos.offset(-1, 0, 0)).getBlock() == Blocks.AIR || Surround.mc.level.getBlockState(playerPos.offset(0, 0, 1)).getBlock() == Blocks.AIR || Surround.mc.level.getBlockState(playerPos.offset(0, 0, -1)).getBlock() == Blocks.AIR || Surround.mc.level.getBlockState(playerPos.offset(0, -1, 0)).getBlock() == Blocks.AIR;
    }

    private static class Offsets
    {
        private static final Vec3[] SURROUND;
        private static final Vec3[] DOUBLE;
        private static final Vec3[] FULL;
        private static final Vec3[] SURROUNDCITY;
        private static final Vec3[] FULLCITY;

        static {
            SURROUND = new Vec3[] { new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), new Vec3(-1.0, 0.0, 0.0), new Vec3(0.0, 0.0, -1.0), new Vec3(1.0, -1.0, 0.0), new Vec3(0.0, -1.0, 1.0), new Vec3(-1.0, -1.0, 0.0), new Vec3(0.0, -1.0, -1.0) };
            DOUBLE = new Vec3[] { new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), new Vec3(-1.0, 0.0, 0.0), new Vec3(0.0, 0.0, -1.0), new Vec3(1.0, -1.0, 0.0), new Vec3(0.0, -1.0, 1.0), new Vec3(-1.0, -1.0, 0.0), new Vec3(0.0, -1.0, -1.0), new Vec3(1.0, 1.0, 0.0), new Vec3(0.0, 1.0, 1.0), new Vec3(-1.0, 1.0, 0.0), new Vec3(0.0, 1.0, -1.0) };
            FULL = new Vec3[] { new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), new Vec3(-1.0, 0.0, 0.0), new Vec3(0.0, 0.0, -1.0), new Vec3(1.0, -1.0, 0.0), new Vec3(0.0, -1.0, 1.0), new Vec3(-1.0, -1.0, 0.0), new Vec3(0.0, -1.0, -1.0), new Vec3(0.0, -1.0, 0.0) };
            SURROUNDCITY = new Vec3[] { new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), new Vec3(-1.0, 0.0, 0.0), new Vec3(0.0, 0.0, -1.0), new Vec3(1.0, -1.0, 0.0), new Vec3(0.0, -1.0, 1.0), new Vec3(-1.0, -1.0, 0.0), new Vec3(0.0, -1.0, -1.0), new Vec3(2.0, 0.0, 0.0), new Vec3(0.0, 0.0, 2.0), new Vec3(-2.0, 0.0, 0.0), new Vec3(0.0, 0.0, -2.0) };
            FULLCITY = new Vec3[] { new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), new Vec3(-1.0, 0.0, 0.0), new Vec3(0.0, 0.0, -1.0), new Vec3(1.0, -1.0, 0.0), new Vec3(0.0, -1.0, 1.0), new Vec3(-1.0, -1.0, 0.0), new Vec3(0.0, -1.0, -1.0), new Vec3(0.0, -1.0, 0.0), new Vec3(2.0, 0.0, 0.0), new Vec3(0.0, 0.0, 2.0), new Vec3(-2.0, 0.0, 0.0), new Vec3(0.0, 0.0, -2.0) };
        }
    }
}
