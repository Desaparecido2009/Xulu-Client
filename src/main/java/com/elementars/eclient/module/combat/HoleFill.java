package com.elementars.eclient.module.combat;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.enemy.Enemies;
import com.elementars.eclient.friend.Friends;
import com.elementars.eclient.mixin.mixins.MixinMinecraft;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.BlockInteractionHelper;
import com.elementars.eclient.util.BoolSwitch;
import com.elementars.eclient.util.TargetPlayers;
import dev.xulu.settings.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class HoleFill extends Module
{
    private final Value<Integer> range;
    private final Value<Integer> yRange;
    private final Value<Boolean> rotate;
    private final Value<Boolean> triggerable;
    private final Value<Integer> waitTick;
    private final Value<Boolean> useEC;
    public final Value<Boolean> noGlitchBlocks;
    private final Value<Boolean> pre;
    private final Value<Boolean> chat;
    private ArrayList<BlockPos> holes;
    private List<Block> whiteList;
    BlockPos pos;
    private int waitCounter;
    int delay;

    public HoleFill() {
        super("HoleFill", "Fills holes", 0, Category.COMBAT, true);
        this.range = this.register(new Value<Integer>("Range", this, 5, 1, 10));
        this.yRange = this.register(new Value<Integer>("YRange", this, 2, 1, 10));
        this.rotate = this.register(new Value<Boolean>("Rotate", this, true));
        this.triggerable = this.register(new Value<Boolean>("Triggerable", this, true));
        this.waitTick = this.register(new Value<Integer>("TickDelay", this, 1, 0, 10));
        this.useEC = this.register(new Value<Boolean>("UseEnderchests", this, false));
        this.noGlitchBlocks = this.register(new Value<Boolean>("NoGlitchBlocks", this, true));
        this.pre = this.register(new Value<Boolean>("Prioritize Enemies", this, false));
        this.chat = this.register(new Value<Boolean>("Chat", this, false));
        this.holes = new ArrayList<BlockPos>();
        this.whiteList = new ArrayList<Block>(Arrays.asList(Blocks.OBSIDIAN));
    }

    @Override
    public void onUpdate() {
        if (this.triggerable.getValue()) {
            if (this.delay > 0) {
                --this.delay;
            }
            else {
                this.toggle();
            }
        }
        this.holes = new ArrayList<BlockPos>();
        if (this.useEC.getValue()) {
            if (!this.whiteList.contains(Blocks.ENDER_CHEST)) {
                this.whiteList.add(Blocks.ENDER_CHEST);
            }
        }
        else {
            this.whiteList.remove(Blocks.ENDER_CHEST);
        }
        final Iterable<BlockPos> blocks = BlockPos.betweenClosed(HoleFill.mc.player.blockPosition().offset(-this.range.getValue(), -this.yRange.getValue(), -this.range.getValue()), HoleFill.mc.player.blockPosition().offset(this.range.getValue(), this.yRange.getValue(), this.range.getValue()));
        for (final BlockPos pos : blocks) {
            if (!HoleFill.mc.level.getBlockState(pos).getMaterial().blocksMotion() && !HoleFill.mc.level.getBlockState(pos.offset(0, 1, 0)).getMaterial().blocksMotion()) {
                final boolean solidNeighbours = (HoleFill.mc.level.getBlockState(pos.offset(1, 0, 0)).getBlock() == Blocks.BEDROCK | HoleFill.mc.level.getBlockState(pos.offset(1, 0, 0)).getBlock() == Blocks.OBSIDIAN) && (HoleFill.mc.level.getBlockState(pos.offset(0, 0, 1)).getBlock() == Blocks.BEDROCK | HoleFill.mc.level.getBlockState(pos.offset(0, 0, 1)).getBlock() == Blocks.OBSIDIAN) && (HoleFill.mc.level.getBlockState(pos.offset(-1, 0, 0)).getBlock() == Blocks.BEDROCK | HoleFill.mc.level.getBlockState(pos.offset(-1, 0, 0)).getBlock() == Blocks.OBSIDIAN) && (HoleFill.mc.level.getBlockState(pos.offset(0, 0, -1)).getBlock() == Blocks.BEDROCK | HoleFill.mc.level.getBlockState(pos.offset(0, 0, -1)).getBlock() == Blocks.OBSIDIAN) && HoleFill.mc.level.getBlockState(pos.offset(0, 0, 0)).getMaterial() == Material.AIR && HoleFill.mc.level.getBlockState(pos.offset(0, 1, 0)).getMaterial() == Material.AIR && HoleFill.mc.level.getBlockState(pos.offset(0, 2, 0)).getMaterial() == Material.AIR;
                if (!solidNeighbours) {
                    continue;
                }
                this.holes.add(pos.immutable());
            }
        }
        int newSlot = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = HoleFill.mc.player.getInventory().getItem(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof BlockItem) {
                final Block block = ((BlockItem)stack.getItem()).getBlock();
                if (this.whiteList.contains(block)) {
                    newSlot = i;
                    break;
                }
            }
        }
        if (newSlot == -1) {
            return;
        }
        final int oldSlot = HoleFill.mc.player.getInventory().selected;
        final BoolSwitch test = new BoolSwitch(false);
        TargetPlayers.targettedplayers.keySet().stream().map(s -> HoleFill.mc.level.players().stream().filter(player -> player.getName().getString().equals(s)).findFirst().orElse(null)).filter(Objects::nonNull).filter(player -> HoleFill.mc.player.distanceTo(player) <= this.range.getValue()).min(Comparator.comparing(player -> HoleFill.mc.player.distanceTo(player))).ifPresent(closest -> {
            test.setValue(true);
            this.holes.sort(Comparator.comparing((BlockPos p) -> closest.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5)));
        });
        if (!test.isValue()) {
            HoleFill.mc.level.players().stream().filter(player -> !Friends.isFriend(player.getName().getString())).filter(player -> HoleFill.mc.player.distanceTo(player) <= this.range.getValue()).min(Comparator.comparing(player -> HoleFill.mc.player.distanceTo(player))).ifPresent(closest -> this.holes.sort(Comparator.comparing((BlockPos p) -> closest.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5))));
            if (this.pre.getValue()) {
                HoleFill.mc.level.players().stream().filter(player -> !Friends.isFriend(player.getName().getString()) && Enemies.isEnemy(player.getName().getString())).filter(player -> HoleFill.mc.player.distanceTo(player) <= this.range.getValue()).min(Comparator.comparing(player -> HoleFill.mc.player.distanceTo(player))).ifPresent(closest -> this.holes.sort(Comparator.comparing((BlockPos p) -> closest.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5))));
            }
        }
        if (this.waitTick.getValue() > 0) {
            if (this.waitCounter < this.waitTick.getValue()) {
                HoleFill.mc.player.getInventory().selected = newSlot;
                this.holes.forEach(this::place);
                HoleFill.mc.player.getInventory().selected = oldSlot;
                return;
            }
            this.waitCounter = 0;
        }
    }

    @Override
    public void onEnable() {
        this.delay = 20;
        if (HoleFill.mc.player != null && this.chat.getValue()) {
            this.sendDebugMessage(ChatFormatting.GREEN + "Enabled!");
        }
    }

    @Override
    public void onDisable() {
        if (HoleFill.mc.player != null && this.chat.getValue()) {
            this.sendDebugMessage(ChatFormatting.RED + "Disabled!");
        }
    }

    private void place(final BlockPos blockPos) {
        for (final Entity entity : HoleFill.mc.level.getEntities((Entity)null, new AABB(blockPos))) {
            if (entity instanceof LivingEntity) {
                return;
            }
        }
        placeBlockScaffold(blockPos, this.rotate.getValue());
        ++this.waitCounter;
    }

    public static boolean placeBlockScaffold(final BlockPos pos, final boolean rotate) {
        final Vec3 eyesPos = new Vec3(HoleFill.mc.player.getX(), HoleFill.mc.player.getY() + HoleFill.mc.player.getEyeHeight(), HoleFill.mc.player.getZ());
        for (final Direction side : Direction.values()) {
            final BlockPos neighbor = pos.relative(side);
            final Direction side2 = side.getOpposite();
            if (BlockInteractionHelper.canBeClicked(neighbor)) {
                final Vec3 hitVec = Vec3.atLowerCornerOf(neighbor).add(0.5, 0.5, 0.5).add(Vec3.atLowerCornerOf(side2.getNormal()).scale(0.5));
                if (rotate) {
                    BlockInteractionHelper.faceVectorPacketInstant(hitVec);
                }
                HoleFill.mc.player.connection.send(new ServerboundPlayerCommandPacket(HoleFill.mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
                processRightClickBlock(neighbor, side2, hitVec);
                HoleFill.mc.player.swing(InteractionHand.MAIN_HAND);
                ((MixinMinecraft)HoleFill.mc).setRightClickDelay(0);
                HoleFill.mc.player.connection.send(new ServerboundPlayerCommandPacket(HoleFill.mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
                if (Xulu.MODULE_MANAGER.getModuleT(HoleFill.class).noGlitchBlocks.getValue() && !HoleFill.mc.gameMode.getPlayerMode().equals(GameType.CREATIVE)) {
                    HoleFill.mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, neighbor, side2));
                }
                return true;
            }
        }
        return false;
    }

    public static void processRightClickBlock(final BlockPos pos, final Direction side, final Vec3 hitVec) {
        HoleFill.mc.gameMode.useItemOn(HoleFill.mc.player, HoleFill.mc.level, InteractionHand.MAIN_HAND, new BlockHitResult(hitVec, side, pos, false));
    }
}
