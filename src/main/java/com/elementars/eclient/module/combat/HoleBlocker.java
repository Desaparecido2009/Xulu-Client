package com.elementars.eclient.module.combat;

import com.elementars.eclient.mixin.mixins.MixinMinecraft;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.module.ModuleManager;
import com.elementars.eclient.util.BlockInteractionHelper;
import dev.xulu.settings.Value;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
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

public class HoleBlocker extends Module
{
    private final Value<Boolean> triggerable;
    private final Value<Integer> timeoutTicks;
    private final Value<Integer> blocksPerTick;
    private final Value<Integer> tickDelay;
    private final Value<Boolean> rotate;
    private final Value<Boolean> noGlitchBlocks;
    private final Value<Boolean> announceUsage;
    private int playerHotbarSlot;
    private int lastHotbarSlot;
    private int offsetStep;
    private int delayStep;
    private int totalTicksRunning;
    private boolean firstRun;
    private boolean isSneaking;
    double oldY;

    public HoleBlocker() {
        super("HoleBlocker", "Blocks others from entering your hole", 0, Category.COMBAT, true);
        this.triggerable = this.register(new Value<Boolean>("Triggerable", this, true));
        this.timeoutTicks = this.register(new Value<Integer>("TimeoutTicks", this, 40, 1, 100));
        this.blocksPerTick = this.register(new Value<Integer>("BlocksPerTick", this, 4, 1, 9));
        this.tickDelay = this.register(new Value<Integer>("TickDelay", this, 0, 0, 10));
        this.rotate = this.register(new Value<Boolean>("Rotate", this, true));
        this.noGlitchBlocks = this.register(new Value<Boolean>("NoGlitchBlocks", this, true));
        this.announceUsage = this.register(new Value<Boolean>("AnnounceUsage", this, true));
        this.playerHotbarSlot = -1;
        this.lastHotbarSlot = -1;
        this.offsetStep = 0;
        this.delayStep = 0;
        this.totalTicksRunning = 0;
        this.isSneaking = false;
    }

    @Override
    public void onEnable() {
        if (HoleBlocker.mc.player == null) {
            this.disable();
            return;
        }
        this.oldY = HoleBlocker.mc.player.getY();
        this.firstRun = true;
        this.playerHotbarSlot = HoleBlocker.mc.player.getInventory().selected;
        this.lastHotbarSlot = -1;
        if (this.announceUsage.getValue()) {
            this.sendDebugMessage(ChatFormatting.GREEN.toString() + "Enabled!");
        }
    }

    @Override
    public void onDisable() {
        if (HoleBlocker.mc.player == null) {
            return;
        }
        if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
            HoleBlocker.mc.player.getInventory().selected = this.playerHotbarSlot;
        }
        if (this.isSneaking) {
            HoleBlocker.mc.player.connection.send(new ServerboundPlayerCommandPacket(HoleBlocker.mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
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
        if (HoleBlocker.mc.player == null || ModuleManager.isModuleEnabled("Freecam")) {
            return;
        }
        if (this.triggerable.getValue() && this.totalTicksRunning >= this.timeoutTicks.getValue()) {
            this.totalTicksRunning = 0;
            this.disable();
            return;
        }
        if (HoleBlocker.mc.player.getY() != this.oldY) {
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
        int blocksPlaced = 0;
        while (blocksPlaced < this.blocksPerTick.getValue()) {
            Vec3[] offsetPattern = new Vec3[0];
            int maxSteps = 0;
            final String facing = HoleBlocker.mc.player.getDirection().getName().toUpperCase();
            if (facing.equalsIgnoreCase("NORTH")) {
                offsetPattern = Offsets.NORTH;
                maxSteps = Offsets.NORTH.length;
            }
            else if (facing.equalsIgnoreCase("EAST")) {
                offsetPattern = Offsets.EAST;
                maxSteps = Offsets.EAST.length;
            }
            else if (facing.equalsIgnoreCase("SOUTH")) {
                offsetPattern = Offsets.SOUTH;
                maxSteps = Offsets.SOUTH.length;
            }
            else if (facing.equalsIgnoreCase("WEST")) {
                offsetPattern = Offsets.WEST;
                maxSteps = Offsets.WEST.length;
            }
            else {
                offsetPattern = Offsets.EAST;
                maxSteps = Offsets.EAST.length;
            }
            if (this.offsetStep >= maxSteps) {
                this.offsetStep = 0;
                break;
            }
            final BlockPos offsetPos = new BlockPos(offsetPattern[this.offsetStep]);
            final BlockPos targetPos = new BlockPos(HoleBlocker.mc.player.position()).offset(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ());
            if (this.placeBlock(targetPos)) {
                ++blocksPlaced;
            }
            ++this.offsetStep;
        }
        if (blocksPlaced > 0) {
            if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
                HoleBlocker.mc.player.getInventory().selected = this.playerHotbarSlot;
                this.lastHotbarSlot = this.playerHotbarSlot;
            }
            if (this.isSneaking) {
                HoleBlocker.mc.player.connection.send(new ServerboundPlayerCommandPacket(HoleBlocker.mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
                this.isSneaking = false;
            }
        }
        ++this.totalTicksRunning;
    }

    private boolean placeBlock(final BlockPos pos) {
        final Block block = HoleBlocker.mc.level.getBlockState(pos).getBlock();
        if (!(block instanceof AirBlock) && !(block instanceof LiquidBlock)) {
            return false;
        }
        for (final Entity entity : HoleBlocker.mc.level.getEntities((Entity)null, new AABB(pos))) {
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
        final Block neighbourBlock = HoleBlocker.mc.level.getBlockState(neighbour).getBlock();
        final int obiSlot = this.findObiInHotbar();
        if (obiSlot == -1) {
            this.disable();
        }
        if (this.lastHotbarSlot != obiSlot) {
            HoleBlocker.mc.player.getInventory().selected = obiSlot;
            this.lastHotbarSlot = obiSlot;
        }
        if ((!this.isSneaking && BlockInteractionHelper.blackList.contains(neighbourBlock)) || BlockInteractionHelper.shulkerList.contains(neighbourBlock)) {
            HoleBlocker.mc.player.connection.send(new ServerboundPlayerCommandPacket(HoleBlocker.mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
            this.isSneaking = true;
        }
        if (this.rotate.getValue()) {
            BlockInteractionHelper.faceVectorPacketInstant(hitVec);
        }
        HoleBlocker.mc.gameMode.useItemOn(HoleBlocker.mc.player, HoleBlocker.mc.level, InteractionHand.MAIN_HAND, new BlockHitResult(hitVec, opposite, neighbour, false));
        HoleBlocker.mc.player.swing(InteractionHand.MAIN_HAND);
        ((MixinMinecraft)HoleBlocker.mc).setRightClickDelay(4);
        if (this.noGlitchBlocks.getValue() && !HoleBlocker.mc.gameMode.getPlayerMode().equals(GameType.CREATIVE)) {
            HoleBlocker.mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, neighbour, opposite));
        }
        return true;
    }

    private int findObiInHotbar() {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = HoleBlocker.mc.player.getInventory().getItem(i);
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
        if (HoleBlocker.mc.level == null) {
            return false;
        }
        final BlockPos playerPos = new BlockPos(HoleBlocker.mc.player.position());
        switch (HoleBlocker.mc.player.getDirection()) {
            case NORTH: {
                for (final Vec3 vec3d : Offsets.NORTH) {
                    if (HoleBlocker.mc.level.getBlockState(playerPos.offset((int)vec3d.x, (int)vec3d.y, (int)vec3d.z)).getBlock() == Blocks.AIR) {
                        return true;
                    }
                }
                break;
            }
            case SOUTH: {
                for (final Vec3 vec3d : Offsets.SOUTH) {
                    if (HoleBlocker.mc.level.getBlockState(playerPos.offset((int)vec3d.x, (int)vec3d.y, (int)vec3d.z)).getBlock() == Blocks.AIR) {
                        return true;
                    }
                }
                break;
            }
            case EAST: {
                for (final Vec3 vec3d : Offsets.EAST) {
                    if (HoleBlocker.mc.level.getBlockState(playerPos.offset((int)vec3d.x, (int)vec3d.y, (int)vec3d.z)).getBlock() == Blocks.AIR) {
                        return true;
                    }
                }
                break;
            }
            case WEST: {
                for (final Vec3 vec3d : Offsets.WEST) {
                    if (HoleBlocker.mc.level.getBlockState(playerPos.offset((int)vec3d.x, (int)vec3d.y, (int)vec3d.z)).getBlock() == Blocks.AIR) {
                        return true;
                    }
                }
                break;
            }
        }
        return false;
    }

    private static class Offsets
    {
        private static final Vec3[] NORTH;
        private static final Vec3[] EAST;
        private static final Vec3[] SOUTH;
        private static final Vec3[] WEST;

        static {
            NORTH = new Vec3[] { new Vec3(0.0, 1.0, 1.0), new Vec3(0.0, 2.0, 1.0), new Vec3(0.0, 2.0, 0.0) };
            EAST = new Vec3[] { new Vec3(-1.0, 1.0, 0.0), new Vec3(-1.0, 2.0, 0.0), new Vec3(0.0, 2.0, 0.0) };
            SOUTH = new Vec3[] { new Vec3(0.0, 1.0, -1.0), new Vec3(0.0, 2.0, -1.0), new Vec3(0.0, 2.0, 0.0) };
            WEST = new Vec3[] { new Vec3(1.0, 1.0, 0.0), new Vec3(1.0, 2.0, 0.0), new Vec3(0.0, 2.0, 0.0) };
        }
    }
}
