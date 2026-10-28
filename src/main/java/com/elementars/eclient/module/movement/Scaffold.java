package com.elementars.eclient.module.movement;

import com.elementars.eclient.event.Event;
import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventSendPacket;
import com.elementars.eclient.event.events.PlayerMoveEvent;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.BlockInteractionHelper;
import dev.xulu.settings.Value;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class Scaffold extends Module {
    private BlockPos block;
    private Direction side;
    private boolean rotated;
    private boolean isSpoofingAngles;
    private double yaw;
    private double pitch;
    private final Value<Integer> delay;
    int delayT;

    public Scaffold() {
        super("Scaffold", "Automatically places blocks below you", 0, Category.MOVEMENT, true);
        this.delay = this.register(new Value<Integer>("Delay", this, 0, 0, 20));
    }

    @Override
    public void onUpdate() {
        if (this.delayT > 0) {
            --this.delayT;
        }
    }

    @EventTarget
    public void onMove(final PlayerMoveEvent event) {
        if (event.getEventState() == Event.State.PRE) {
            this.rotated = false;
            this.block = null;
            this.side = null;
            final BlockPos pos = new BlockPos(Scaffold.mc.player.blockPosition()).below();
            if (Scaffold.mc.level.getBlockState(pos).isAir()) {
                this.setBlockAndFacing(pos);
                if (this.block != null) {
                    final float[] facing = BlockInteractionHelper.getDirectionToBlock(this.block.getX(), this.block.getY(), this.block.getZ(), this.side);
                    final float yaw = facing[0];
                    final float pitch = Math.min(90.0f, facing[1] + 9.0f);
                    this.rotated = true;
                    this.yaw = yaw;
                    this.pitch = pitch;
                    this.isSpoofingAngles = true;
                }
            }
        }
        if (event.getEventState() == Event.State.POST && this.block != null && this.delayT == 0 && Scaffold.mc.player.getMainHandItem() != null && Scaffold.mc.player.getMainHandItem().getItem() instanceof BlockItem) {
            final Vec3 hitVec = new Vec3(this.block.getX() + 0.5, this.block.getY() + 0.5, this.block.getZ() + 0.5);
            final var result = Scaffold.mc.gameMode.useItemOn(Scaffold.mc.player, Scaffold.mc.level, InteractionHand.MAIN_HAND, new net.minecraft.world.phys.BlockHitResult(hitVec, this.side, this.block, false));
            if (result.consumesAction()) {
                this.delayT = this.delay.getValue();
                Scaffold.mc.player.swing(InteractionHand.MAIN_HAND);
                Scaffold.mc.player.setDeltaMovement(0.0, Scaffold.mc.player.getDeltaMovement().y, 0.0);
            }
        }
    }

    @EventTarget
    public void onSend(final EventSendPacket event) {
        if (event.getPacket() instanceof ServerboundMovePlayerPacket && this.isSpoofingAngles) {
            final var packet = (ServerboundMovePlayerPacket) event.getPacket();
            // Note: ServerboundMovePlayerPacket is immutable in 1.18.2
            // We need to replace the packet instead of modifying it
            if (packet instanceof ServerboundMovePlayerPacket.Rot) {
                final var rotPacket = (ServerboundMovePlayerPacket.Rot) packet;
                event.setPacket(new ServerboundMovePlayerPacket.Rot((float) this.yaw, (float) this.pitch, rotPacket.isOnGround()));
            } else if (packet instanceof ServerboundMovePlayerPacket.PosRot) {
                final var posRotPacket = (ServerboundMovePlayerPacket.PosRot) packet;
                event.setPacket(new ServerboundMovePlayerPacket.PosRot(posRotPacket.getX(0), posRotPacket.getY(0), posRotPacket.getZ(0), (float) this.yaw, (float) this.pitch, posRotPacket.isOnGround()));
            }
            this.isSpoofingAngles = false;
        }
    }

    private void setBlockAndFacing(final BlockPos pos) {
        if (!Scaffold.mc.level.getBlockState(pos.below()).isAir()) {
            this.block = pos.below();
            this.side = Direction.UP;
        } else if (!Scaffold.mc.level.getBlockState(pos.west()).isAir()) {
            this.block = pos.west();
            this.side = Direction.EAST;
        } else if (!Scaffold.mc.level.getBlockState(pos.east()).isAir()) {
            this.block = pos.east();
            this.side = Direction.WEST;
        } else if (!Scaffold.mc.level.getBlockState(pos.north()).isAir()) {
            this.block = pos.north();
            this.side = Direction.SOUTH;
        } else if (!Scaffold.mc.level.getBlockState(pos.south()).isAir()) {
            this.block = pos.south();
            this.side = Direction.NORTH;
        } else {
            this.block = null;
            this.side = null;
        }
    }
}