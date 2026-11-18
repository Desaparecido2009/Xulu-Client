package com.elementars.eclient.module.misc;

import com.elementars.eclient.mixin.mixins.AccessorMinecraft;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.BlockInteractionHelper;
import com.elementars.eclient.util.EnumUtil;
import dev.xulu.settings.Value;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DeadBushBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AutoWither extends Module
{
    private final Value<String> useMode;
    private final Value<Float> placeRange;
    private final Value<Integer> delay;
    private final Value<Boolean> rotate;
    private final Value<Boolean> debug;
    private BlockPos placeTarget;
    private boolean rotationPlaceableX;
    private boolean rotationPlaceableZ;
    private int bodySlot;
    private int headSlot;
    private int buildStage;
    private int delayStep;
    private boolean isSneaking;

    public AutoWither() {
        super("AutoWither", "Automatically places withers", 0, Category.MISC, true);
        this.useMode = this.register(new Value<String>("UseMode", this, "Spam", EnumUtil.enumConverter(UseMode.class)));
        this.placeRange = this.register(new Value<Float>("PlaceRange", this, 3.5f, 2.0f, 10.0f));
        this.delay = this.register(new Value<Integer>("Delay", this, 20, 12, 100));
        this.rotate = this.register(new Value<Boolean>("Rotate", this, true));
        this.debug = this.register(new Value<Boolean>("Debug", this, false));
    }

    private void placeBlock(final BlockPos pos, final boolean rotate) {
        final Direction side = BlockInteractionHelper.getPlaceableSide(pos);
        if (side == null) {
            return;
        }
        final BlockPos neighbour = pos.relative(side);
        final Direction opposite = side.getOpposite();
        final Vec3 hitVec = Vec3.atLowerCornerOf(neighbour).add(0.5, 0.5, 0.5).add(Vec3.atLowerCornerOf(opposite.getNormal()).scale(0.5));
        final Block neighbourBlock = AutoWither.mc.level.getBlockState(neighbour).getBlock();
        if (!this.isSneaking && (BlockInteractionHelper.blackList.contains(neighbourBlock) || BlockInteractionHelper.shulkerList.contains(neighbourBlock))) {
            AutoWither.mc.player.connection.send(new ServerboundPlayerCommandPacket(AutoWither.mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
            this.isSneaking = true;
        }
        if (rotate) {
            BlockInteractionHelper.faceVectorPacketInstant(hitVec);
        }
        AutoWither.mc.gameMode.useItemOn(AutoWither.mc.player, AutoWither.mc.level, InteractionHand.MAIN_HAND, new BlockHitResult(hitVec, opposite, neighbour, false));
        AutoWither.mc.player.swing(InteractionHand.MAIN_HAND);
        ((AccessorMinecraft)AutoWither.mc).setRightClickDelay(4);
    }

    @Override
    public void onEnable() {
        if (AutoWither.mc.player == null) {
            this.disable();
            return;
        }
        this.buildStage = 1;
        this.delayStep = 1;
        this.isSneaking = false;
    }

    @Override
    public void onDisable() {
        if (this.isSneaking && AutoWither.mc.player != null) {
            AutoWither.mc.player.connection.send(new ServerboundPlayerCommandPacket(AutoWither.mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
            this.isSneaking = false;
        }
    }

    private boolean checkBlocksInHotbar() {
        this.headSlot = -1;
        this.bodySlot = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = AutoWither.mc.player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() == Items.WITHER_SKELETON_SKULL) {
                    if (stack.getCount() >= 3) {
                        this.headSlot = i;
                    }
                }
                else if (stack.getItem() instanceof BlockItem) {
                    final Block block = ((BlockItem)stack.getItem()).getBlock();
                    if (block == Blocks.SOUL_SAND && stack.getCount() >= 4) {
                        this.bodySlot = i;
                    }
                }
            }
        }
        return this.bodySlot != -1 && this.headSlot != -1;
    }

    private boolean testStructure() {
        return this.testWitherStructure();
    }

    private boolean testWitherStructure() {
        boolean noRotationPlaceable = true;
        this.rotationPlaceableX = true;
        this.rotationPlaceableZ = true;
        boolean isShitGrass = false;
        if (AutoWither.mc.level == null || this.placeTarget == null) {
            return false;
        }
        final Block block = AutoWither.mc.level.getBlockState(this.placeTarget).getBlock();
        if (block instanceof TallGrassBlock || block instanceof DoublePlantBlock || block instanceof DeadBushBlock) {
            isShitGrass = true;
        }
        if (BlockInteractionHelper.getPlaceableSide(this.placeTarget.above()) == null) {
            return false;
        }
        for (final BlockPos pos : BodyParts.bodyBase) {
            if (this.placingIsBlocked(this.placeTarget.offset(pos))) {
                noRotationPlaceable = false;
            }
        }
        for (final BlockPos pos : BodyParts.ArmsX) {
            if (this.placingIsBlocked(this.placeTarget.offset(pos)) || this.placingIsBlocked(this.placeTarget.offset(pos).below())) {
                this.rotationPlaceableX = false;
            }
        }
        for (final BlockPos pos : BodyParts.ArmsZ) {
            if (this.placingIsBlocked(this.placeTarget.offset(pos)) || this.placingIsBlocked(this.placeTarget.offset(pos).below())) {
                this.rotationPlaceableZ = false;
            }
        }
        for (final BlockPos pos : BodyParts.headsX) {
            if (this.placingIsBlocked(this.placeTarget.offset(pos))) {
                this.rotationPlaceableX = false;
            }
        }
        for (final BlockPos pos : BodyParts.headsZ) {
            if (this.placingIsBlocked(this.placeTarget.offset(pos))) {
                this.rotationPlaceableZ = false;
            }
        }
        return !isShitGrass && noRotationPlaceable && (this.rotationPlaceableX || this.rotationPlaceableZ);
    }

    @Override
    public void onUpdate() {
        if (AutoWither.mc.player == null || AutoWither.mc.level == null) {
            return;
        }
        if (this.buildStage == 1) {
            this.isSneaking = false;
            this.rotationPlaceableX = false;
            this.rotationPlaceableZ = false;
            if (!this.checkBlocksInHotbar()) {
                this.sendDebugMessage("no blocks in hotbar");
                return;
            }
            final List<BlockPos> blockPosList = BlockInteractionHelper.getSphere(AutoWither.mc.player.blockPosition().below(), this.placeRange.getValue(), this.placeRange.getValue().intValue(), false, true, 0);
            boolean noPositionInArea = true;
            for (final BlockPos pos : blockPosList) {
                this.placeTarget = pos.below();
                if (!this.testStructure()) {
                    continue;
                }
                noPositionInArea = false;
                break;
            }
            if (noPositionInArea) {
                if (this.useMode.getValue().equalsIgnoreCase("Single")) {
                    if (this.debug.getValue()) {
                        this.sendDebugMessage(ChatFormatting.RED.toString() + "Position not valid, disabling.");
                    }
                    this.disable();
                }
                return;
            }
            AutoWither.mc.player.getInventory().selected = this.bodySlot;
            for (final BlockPos pos2 : BodyParts.bodyBase) {
                this.placeBlock(this.placeTarget.offset(pos2), this.rotate.getValue());
            }
            if (this.rotationPlaceableX) {
                for (final BlockPos pos2 : BodyParts.ArmsX) {
                    this.placeBlock(this.placeTarget.offset(pos2), this.rotate.getValue());
                }
            }
            else if (this.rotationPlaceableZ) {
                for (final BlockPos pos2 : BodyParts.ArmsZ) {
                    this.placeBlock(this.placeTarget.offset(pos2), this.rotate.getValue());
                }
            }
            this.buildStage = 2;
        }
        else if (this.buildStage == 2) {
            AutoWither.mc.player.getInventory().selected = this.headSlot;
            if (this.rotationPlaceableX) {
                for (final BlockPos pos : BodyParts.headsX) {
                    this.placeBlock(this.placeTarget.offset(pos), this.rotate.getValue());
                }
            }
            else if (this.rotationPlaceableZ) {
                for (final BlockPos pos : BodyParts.headsZ) {
                    this.placeBlock(this.placeTarget.offset(pos), this.rotate.getValue());
                }
            }
            if (this.isSneaking) {
                AutoWither.mc.player.connection.send(new ServerboundPlayerCommandPacket(AutoWither.mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
                this.isSneaking = false;
            }
            if (this.useMode.getValue().equalsIgnoreCase("Single")) {
                this.disable();
            }
            this.buildStage = 3;
        }
        else if (this.buildStage == 3) {
            if (this.delayStep < this.delay.getValue()) {
                ++this.delayStep;
            }
            else {
                this.delayStep = 1;
                this.buildStage = 1;
            }
        }
    }

    private boolean placingIsBlocked(final BlockPos pos) {
        final Block block = AutoWither.mc.level.getBlockState(pos).getBlock();
        if (!(block instanceof AirBlock)) {
            return true;
        }
        for (final Entity entity : AutoWither.mc.level.getEntities((Entity)null, new AABB(pos))) {
            if (entity instanceof ItemEntity) {
                continue;
            }
            if (entity instanceof ExperienceOrb) {
                continue;
            }
            return true;
        }
        return false;
    }

    private static class BodyParts
    {
        private static final BlockPos[] bodyBase;
        private static final BlockPos[] ArmsX;
        private static final BlockPos[] ArmsZ;
        private static final BlockPos[] headsX;
        private static final BlockPos[] headsZ;

        static {
            bodyBase = new BlockPos[] { new BlockPos(0, 1, 0), new BlockPos(0, 2, 0) };
            ArmsX = new BlockPos[] { new BlockPos(-1, 2, 0), new BlockPos(1, 2, 0) };
            ArmsZ = new BlockPos[] { new BlockPos(0, 2, -1), new BlockPos(0, 2, 1) };
            headsX = new BlockPos[] { new BlockPos(0, 3, 0), new BlockPos(-1, 3, 0), new BlockPos(1, 3, 0) };
            headsZ = new BlockPos[] { new BlockPos(0, 3, 0), new BlockPos(0, 3, -1), new BlockPos(0, 3, 1) };
        }
    }

    private enum UseMode
    {
        SINGLE,
        SPAM;
    }
}
