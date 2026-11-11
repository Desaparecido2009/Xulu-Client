package com.elementars.eclient.module.combat;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

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
        if (PearlPhase.mc.player == null || PearlPhase.mc.level == null || PearlPhase.mc.getConnection() == null) {
            this.disable();
            return;
        }
        if (!this.isCollidingWithBlock()) {
            this.announce(ChatFormatting.RED + "Not colliding with a block!");
            this.disable();
            return;
        }
        final float yaw = PearlPhase.mc.player.getYRot();
        final float pitch = this.pitch.getValue().floatValue();
        final String mode = this.switchMode.getValue();

        if (PearlPhase.mc.player.getOffhandItem().getItem() == Items.ENDER_PEARL) {
            this.throwPearl(InteractionHand.OFF_HAND, yaw, pitch);
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
            this.throwPearl(InteractionHand.MAIN_HAND, yaw, pitch);
            this.announce(ChatFormatting.GREEN + "Pearl thrown!");
            this.disable();
            return;
        }

        final int pearlSlot = this.findPearlInHotbar();
        if (pearlSlot == -1) {
            this.announce(ChatFormatting.RED + "No pearl found!");
            this.disable();
            return;
        }
        final int oldSlot = PearlPhase.mc.player.getInventory().selected;
        final boolean silent = mode.equalsIgnoreCase("Silent");
        final boolean switched = pearlSlot != oldSlot;

        if (switched) {
            PearlPhase.mc.getConnection().send(new ServerboundSetCarriedItemPacket(pearlSlot));
            if (!silent) {
                PearlPhase.mc.player.getInventory().selected = pearlSlot;
            }
        }
        this.throwPearl(InteractionHand.MAIN_HAND, yaw, pitch);
        if (switched) {
            PearlPhase.mc.getConnection().send(new ServerboundSetCarriedItemPacket(oldSlot));
            if (!silent) {
                PearlPhase.mc.player.getInventory().selected = oldSlot;
            }
        }
        this.announce(ChatFormatting.GREEN + "Pearl thrown!");
        this.disable();
    }

    private void throwPearl(final InteractionHand hand, final float yaw, final float pitch) {
        PearlPhase.mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, PearlPhase.mc.player.isOnGround()));
        PearlPhase.mc.getConnection().send(new ServerboundUseItemPacket(hand));
        PearlPhase.mc.player.swing(hand);
        if (this.rotateBack.getValue()) {
            PearlPhase.mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(PearlPhase.mc.player.getYRot(), PearlPhase.mc.player.getXRot(), PearlPhase.mc.player.isOnGround()));
        }
    }

    private boolean isCollidingWithBlock() {
        final AABB playerBox = PearlPhase.mc.player.getBoundingBox().inflate(0.02, 0.0, 0.02);
        return PearlPhase.mc.level.getBlockCollisions(PearlPhase.mc.player, playerBox).iterator().hasNext();
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

    private void announce(final String message) {
        if (this.announceUsage.getValue() && PearlPhase.mc.player != null) {
            PearlPhase.mc.player.displayClientMessage(new TextComponent(message), false);
        }
    }
}