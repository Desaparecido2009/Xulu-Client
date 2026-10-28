package com.elementars.eclient.util;

import com.elementars.eclient.mixin.mixins.MixinMinecraft;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockInteractionHelper
{
    public static final List<Block> blackList;
    public static final List<Block> shulkerList;
    private static final Minecraft mc;

    public static boolean hotbarSlotCheckEmpty(final ItemStack stack) {
        return stack != ItemStack.EMPTY;
    }

    public static boolean blockCheckNonBlock(final ItemStack stack) {
        return stack.getItem() instanceof BlockItem;
    }

    public static AABB getBlockBounds(final BlockState state, final Level level, final BlockPos pos) {
        final VoxelShape shape = state.getShape(level, pos);
        return shape.isEmpty() ? new AABB(pos) : shape.bounds().move(pos);
    }

    public static void rotate(final float yaw, final float pitch) {
        Minecraft.getInstance().player.setYRot(yaw);
        Minecraft.getInstance().player.setXRot(pitch);
    }

    public static void rotate(final double[] rotations) {
        Minecraft.getInstance().player.setYRot((float)rotations[0]);
        Minecraft.getInstance().player.setXRot((float)rotations[1]);
    }

    public static double[] calculateLookAt(final double px, final double py, final double pz, final Player me) {
        double dirx = me.getX() - px;
        double diry = me.getY() - py;
        double dirz = me.getZ() - pz;
        final double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);
        dirx /= len;
        diry /= len;
        dirz /= len;
        double pitch = Math.asin(diry);
        double yaw = Math.atan2(dirz, dirx);
        pitch = pitch * 180.0 / 3.141592653589793;
        yaw = yaw * 180.0 / 3.141592653589793;
        yaw += 90.0;
        return new double[] { yaw, pitch };
    }

    public static void lookAtBlock(final BlockPos blockToLookAt) {
        rotate(calculateLookAt(blockToLookAt.getX(), blockToLookAt.getY(), blockToLookAt.getZ(), Wrapper.getPlayer()));
    }

    public static void placeBlockScaffold(final BlockPos pos) {
        final Vec3 eyesPos = new Vec3(Wrapper.getPlayer().getX(), Wrapper.getPlayer().getY() + Wrapper.getPlayer().getEyeHeight(), Wrapper.getPlayer().getZ());
        for (final Direction side : Direction.values()) {
            final BlockPos neighbor = pos.relative(side);
            final Direction side2 = side.getOpposite();
            if (canBeClicked(neighbor)) {
                final Vec3 hitVec = Vec3.atLowerCornerOf(neighbor).add(0.5, 0.5, 0.5).add(Vec3.atLowerCornerOf(side2.getNormal()).xRot(0.5f));
                if (eyesPos.distanceToSqr(hitVec) <= 18.0625) {
                    faceVectorPacketInstant(hitVec);
                    processRightClickBlock(neighbor, side2, hitVec);
                    Wrapper.getPlayer().swing(InteractionHand.MAIN_HAND);
                    ((MixinMinecraft)BlockInteractionHelper.mc).setRightClickDelay(4);
                    return;
                }
            }
        }
    }

    private static float[] getLegitRotations(final Vec3 vec) {
        final Vec3 eyesPos = getEyesPos();
        final double diffX = vec.x - eyesPos.x;
        final double diffY = vec.y - eyesPos.y;
        final double diffZ = vec.z - eyesPos.z;
        final double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        final float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        final float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[] { Wrapper.getPlayer().getYRot() + Mth.wrapDegrees(yaw - Wrapper.getPlayer().getYRot()), Wrapper.getPlayer().getXRot() + Mth.wrapDegrees(pitch - Wrapper.getPlayer().getXRot()) };
    }

    private static Vec3 getEyesPos() {
        return new Vec3(Wrapper.getPlayer().getX(), Wrapper.getPlayer().getY() + Wrapper.getPlayer().getEyeHeight(), Wrapper.getPlayer().getZ());
    }

    public static void faceVectorPacketInstant(final Vec3 vec) {
        final float[] rotations = getLegitRotations(vec);
        Wrapper.getPlayer().connection.send(new ServerboundMovePlayerPacket.Rot(rotations[0], rotations[1], Wrapper.getPlayer().isOnGround()));
    }

    private static void processRightClickBlock(final BlockPos pos, final Direction side, final Vec3 hitVec) {
        BlockInteractionHelper.mc.gameMode.useItemOn(Wrapper.getPlayer(), BlockInteractionHelper.mc.level, InteractionHand.MAIN_HAND, new BlockHitResult(hitVec, side, pos, false));
    }

    public static boolean canBeClicked(final BlockPos pos) {
        final BlockState state = getState(pos);
        return !state.isAir() && !state.getMaterial().isLiquid();
    }

    private static BlockState getState(final BlockPos pos) {
        return Wrapper.getWorld().getBlockState(pos);
    }

    public static boolean checkForNeighbours(final BlockPos blockPos) {
        if (!hasNeighbour(blockPos)) {
            for (final Direction side : Direction.values()) {
                final BlockPos neighbour = blockPos.relative(side);
                if (hasNeighbour(neighbour)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public static List<BlockPos> getSphere(final BlockPos loc, final float r, final int h, final boolean hollow, final boolean sphere, final int plus_y) {
        final List<BlockPos> circleblocks = new ArrayList<BlockPos>();
        final int cx = loc.getX();
        final int cy = loc.getY();
        final int cz = loc.getZ();
        for (int x = cx - (int)r; x <= cx + r; ++x) {
            for (int z = cz - (int)r; z <= cz + r; ++z) {
                for (int y = sphere ? (cy - (int)r) : cy; y < (sphere ? (cy + r) : ((float)(cy + h))); ++y) {
                    final double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? ((cy - y) * (cy - y)) : 0);
                    if (dist < r * r && (!hollow || dist >= (r - 1.0f) * (r - 1.0f))) {
                        final BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                }
            }
        }
        return circleblocks;
    }

    public static List<BlockPos> getCircle(final BlockPos loc, final int y, final float r, final boolean hollow) {
        final List<BlockPos> circleblocks = new ArrayList<BlockPos>();
        final int cx = loc.getX();
        final int cz = loc.getZ();
        for (int x = cx - (int)r; x <= cx + r; ++x) {
            for (int z = cz - (int)r; z <= cz + r; ++z) {
                final double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z);
                if (dist < r * r && (!hollow || dist >= (r - 1.0f) * (r - 1.0f))) {
                    final BlockPos l = new BlockPos(x, y, z);
                    circleblocks.add(l);
                }
            }
        }
        return circleblocks;
    }

    private static boolean hasNeighbour(final BlockPos blockPos) {
        for (final Direction side : Direction.values()) {
            final BlockPos neighbour = blockPos.relative(side);
            if (!Wrapper.getWorld().getBlockState(neighbour).getMaterial().isReplaceable()) {
                return true;
            }
        }
        return false;
    }

    public static Direction getPlaceableSide(final BlockPos pos) {
        for (final Direction side : Direction.values()) {
            final BlockPos neighbour = pos.relative(side);
            final BlockState blockState = BlockInteractionHelper.mc.level.getBlockState(neighbour);
            if (!blockState.isAir() && !blockState.getMaterial().isLiquid() && !blockState.getMaterial().isReplaceable()) {
                return side;
            }
        }
        return null;
    }

    public static float[] getDirectionToBlock(final int var0, final int var1, final int var2, final Direction var3) {
        final ThrownEgg var4 = new ThrownEgg(BlockInteractionHelper.mc.level, var0 + 0.5, var1 + 0.5, var2 + 0.5);
        var4.setPos(var4.getX() + var3.getNormal().getX() * 0.25, var4.getY() + var3.getNormal().getY() * 0.25, var4.getZ() + var3.getNormal().getZ() * 0.25);
        return getDirectionToEntity(var4);
    }

    private static float[] getDirectionToEntity(final Entity var0) {
        return new float[] { getYaw(var0) + BlockInteractionHelper.mc.player.getYRot(), getPitch(var0) + BlockInteractionHelper.mc.player.getXRot() };
    }

    public static float[] getRotationNeededForBlock(final Player paramEntityPlayer, final BlockPos pos) {
        final double d1 = pos.getX() - paramEntityPlayer.getX();
        final double d2 = pos.getY() + 0.5 - (paramEntityPlayer.getY() + paramEntityPlayer.getEyeHeight());
        final double d3 = pos.getZ() - paramEntityPlayer.getZ();
        final double d4 = Math.sqrt(d1 * d1 + d3 * d3);
        final float f1 = (float)(Math.atan2(d3, d1) * 180.0 / 3.141592653589793) - 90.0f;
        final float f2 = (float)(-(Math.atan2(d2, d4) * 180.0 / 3.141592653589793));
        return new float[] { f1, f2 };
    }

    public static float getYaw(final Entity var0) {
        final double var = var0.getX() - BlockInteractionHelper.mc.player.getX();
        final double var2 = var0.getZ() - BlockInteractionHelper.mc.player.getZ();
        double var3;
        if (var2 < 0.0 && var < 0.0) {
            var3 = 90.0 + Math.toDegrees(Math.atan(var2 / var));
        }
        else if (var2 < 0.0 && var > 0.0) {
            var3 = -90.0 + Math.toDegrees(Math.atan(var2 / var));
        }
        else {
            var3 = Math.toDegrees(-Math.atan(var / var2));
        }
        return Mth.wrapDegrees(-(BlockInteractionHelper.mc.player.getYRot() - (float)var3));
    }

    public static float getPitch(final Entity var0) {
        final double var = var0.getX() - BlockInteractionHelper.mc.player.getX();
        final double var2 = var0.getZ() - BlockInteractionHelper.mc.player.getZ();
        final double var3 = var0.getY() - 1.6 + var0.getEyeHeight() - BlockInteractionHelper.mc.player.getY();
        final double var4 = Mth.sqrt((float)(var * var + var2 * var2));
        final double var5 = -Math.toDegrees(Math.atan(var3 / var4));
        return -Mth.wrapDegrees(BlockInteractionHelper.mc.player.getXRot() - (float)var5);
    }

    static {
        blackList = Arrays.asList(Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE, Blocks.ANVIL, Blocks.BREWING_STAND, Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER);
        shulkerList = Arrays.asList(Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX);
        mc = Minecraft.getInstance();
    }
}
