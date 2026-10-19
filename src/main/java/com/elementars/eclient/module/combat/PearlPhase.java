package com.elementars.eclient.module.combat;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PearlPhase extends Module
{
    private final Value<String> switchMode;
    private final Value<Integer> pitch;
    private final Value<Boolean> rotateBack;
    private final Value<Boolean> announceUsage;

    public PearlPhase() {
        super("PearlPhase", "Phases you through the block you are colliding with", 0, Category.COMBAT, true);
        this.switchMode = this.register(new Value<String>("Switch", this, "Normal", new String[] { "Normal", "None", "Silent" }));
        this.pitch = this.register(new Value<Integer>("Pitch", this, 80, 0, 90));
        this.rotateBack = this.register(new Value<Boolean>("Rotate Back", this, true));
        this.announceUsage = this.register(new Value<Boolean>("AnnounceUsage", this, true));
    }

    @Override
    public void onUpdate() {
        if (PearlPhase.mc.player == null) {
            this.disable();
            return;
        }
        final int[] direction = this.findPhaseDirection();
        if (direction == null) {
            this.announce(ChatFormatting.RED + "Not colliding with a block!");
            this.disable();
            return;
        }
        final float yaw = (float)Math.toDegrees(Math.atan2(-direction[0], direction[1]));
        final float pitch = this.pitch.getValue().floatValue();
        final String mode = this.switchMode.getValue();
        final int pearlSlot = this.findPearlInHotbar();
        if (PearlPhase.mc.player.getOffhandItem().getItem() == Items.ENDER_PEARL) {
            this.rotate(yaw, pitch);
            PearlPhase.mc.player.connection.send(new ServerboundUseItemPacket(InteractionHand.OFF_HAND));
            PearlPhase.mc.player.swing(InteractionHand.OFF_HAND);
            this.rotateBack();
            this.announce(ChatFormatting.GREEN + "Pearl thrown!");
            this.disable();
            return;
        }
        if (mode.equalsIgnoreCase("None")) {
            if (PearlPhase.mc.player.getMainHandItem().getItem() != Items.ENDER_PEARL) {
                this.announce(ChatFormatting.RED + "No pearl found!");
                this.disable();
                return;
            }
            this.rotate(yaw, pitch);
            PearlPhase.mc.gameMode.useItem(PearlPhase.mc.player, PearlPhase.mc.level, InteractionHand.MAIN_HAND);
            PearlPhase.mc.player.swing(InteractionHand.MAIN_HAND);
            this.rotateBack();
            this.announce(ChatFormatting.GREEN + "Pearl thrown!");
            this.disable();
            return;
        }
        if (pearlSlot == -1) {
            this.announce(ChatFormatting.RED + "No pearl found!");
            this.disable();
            return;
        }
        final int oldSlot = PearlPhase.mc.player.getInventory().selected;
        final boolean silent = mode.equalsIgnoreCase("Silent");
        this.switchToHotbarSlot(pearlSlot, silent);
        this.rotate(yaw, pitch);
        if (silent) {
            PearlPhase.mc.player.connection.send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND));
        }
        else {
            PearlPhase.mc.gameMode.useItem(PearlPhase.mc.player, PearlPhase.mc.level, InteractionHand.MAIN_HAND);
        }
        this.rotateBack();
        this.switchToHotbarSlot(oldSlot, silent);
        PearlPhase.mc.player.swing(InteractionHand.MAIN_HAND);
        this.announce(ChatFormatting.GREEN + "Pearl thrown!");
        this.disable();
    }

    private int[] findPhaseDirection() {
        final BlockPos feet = new BlockPos(PearlPhase.mc.player.position());
        final int dx = (this.isColliding(feet.east()) || this.isColliding(feet.east().above()) ? 1 : 0) - (this.isColliding(feet.west()) || this.isColliding(feet.west().above()) ? 1 : 0);
        final int dz = (this.isColliding(feet.south()) || this.isColliding(feet.south().above()) ? 1 : 0) - (this.isColliding(feet.north()) || this.isColliding(feet.north().above()) ? 1 : 0);
        if (dx != 0 || dz != 0) {
            return new int[] { dx, dz };
        }
        if (this.isColliding(feet.east().south()) || this.isColliding(feet.east().south().above())) {
            return new int[] { 1, 1 };
        }
        if (this.isColliding(feet.east().north()) || this.isColliding(feet.east().north().above())) {
            return new int[] { 1, -1 };
        }
        if (this.isColliding(feet.west().south()) || this.isColliding(feet.west().south().above())) {
            return new int[] { -1, 1 };
        }
        if (this.isColliding(feet.west().north()) || this.isColliding(feet.west().north().above())) {
            return new int[] { -1, -1 };
        }
        return null;
    }

    private boolean isColliding(final BlockPos pos) {
        final VoxelShape shape = PearlPhase.mc.level.getBlockState(pos).getCollisionShape(PearlPhase.mc.level, pos);
        if (shape.isEmpty()) {
            return false;
        }
        final AABB playerBox = PearlPhase.mc.player.getBoundingBox().inflate(0.02);
        for (final AABB box : shape.toAabbs()) {
            if (playerBox.intersects(box.move(pos.getX(), pos.getY(), pos.getZ()))) {
                return true;
            }
        }
        return false;
    }

    private int findPearlInHotbar() {
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = PearlPhase.mc.player.getInventory().getItem(i);
            if (stack.getItem() == Items.ENDER_PEARL) {
                return i;
            }
        }
        return -1;
    }

    private void switchToHotbarSlot(final int slot, final boolean silent) {
        if (PearlPhase.mc.player.getInventory().selected == slot || slot < 0) {
            return;
        }
        PearlPhase.mc.player.connection.send(new ServerboundSetCarriedItemPacket(slot));
        if (!silent) {
            PearlPhase.mc.player.getInventory().selected = slot;
        }
    }

    private void rotate(final float yaw, final float pitch) {
        PearlPhase.mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, PearlPhase.mc.player.isOnGround()));
    }

    private void rotateBack() {
        if (this.rotateBack.getValue()) {
            PearlPhase.mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(PearlPhase.mc.player.getYRot(), PearlPhase.mc.player.getXRot(), PearlPhase.mc.player.isOnGround()));
        }
    }

    private void announce(final String message) {
        if (this.announceUsage.getValue()) {
            this.sendDebugMessage(message);
        }
    }
}
