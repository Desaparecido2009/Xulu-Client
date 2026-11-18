package com.elementars.eclient.module.movement;

import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.MotionEvent;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Step extends Module
{
    private final Value<String> mode;
    private final double[] oneblockPositions;
    private final double[] onehalfblockPositions;
    private final double[] twoblockPositions;
    private int packets;

    public Step() {
        super("Step", "Step up blocks", 0, Category.MOVEMENT, true);
        this.mode = this.register(new Value<String>("Mode", this, "Normal", new String[] { "Normal" }));
        this.oneblockPositions = new double[] { 0.41999998688698, 0.7531999805212 };
        this.onehalfblockPositions = new double[] { 0.41999998688698, 0.7531999805212, 1.00133597911214, 1.16610926093821, 1.24918707874468, 1.1707870772188 };
        this.twoblockPositions = new double[] { 0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43 };
    }

    @Override
    public void onDisable() {
        this.packets = 0;
        if (Step.mc.player != null) {
            Step.mc.player.maxUpStep = 0.6f;
        }
    }

    @EventTarget
    public void onWalkingUpdate(final MotionEvent event) {
        if (Step.mc.player == null || Step.mc.level == null) {
            return;
        }
        if (!Step.mc.player.horizontalCollision && this.mode.getValue().equalsIgnoreCase("Normal")) {
            return;
        }
        if (!Step.mc.player.isOnGround() || Step.mc.player.onClimbable() || Step.mc.player.isInWater() || Step.mc.player.isInLava() || Step.mc.player.input.jumping || Step.mc.player.noPhysics) {
            return;
        }
        if (Step.mc.player.zza == 0.0f && Step.mc.player.xxa == 0.0f) {
            return;
        }
        if (Step.mc.player.horizontalCollision && Step.mc.player.isOnGround()) {
            ++this.packets;
        }
        final double n = this.get_n_normal();
        if (this.mode.getValue().equalsIgnoreCase("Normal")) {
            if (n < 0.0 || n > 2.0) {
                return;
            }
            if (n == 2.0 && this.packets > this.twoblockPositions.length - 2) {
                for (final double pos : this.twoblockPositions) {
                    Step.mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(Step.mc.player.getX(), Step.mc.player.getY() + pos, Step.mc.player.getZ(), true));
                }
                Step.mc.player.setPos(Step.mc.player.getX(), Step.mc.player.getY() + 2.0, Step.mc.player.getZ());
                this.packets = 0;
            }
            if (n == 1.5 && this.packets > this.onehalfblockPositions.length - 2) {
                for (final double pos : this.onehalfblockPositions) {
                    Step.mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(Step.mc.player.getX(), Step.mc.player.getY() + pos, Step.mc.player.getZ(), true));
                }
                Step.mc.player.setPos(Step.mc.player.getX(), Step.mc.player.getY() + this.onehalfblockPositions[this.onehalfblockPositions.length - 1], Step.mc.player.getZ());
                this.packets = 0;
            }
            if (n == 1.0 && this.packets > this.oneblockPositions.length - 2) {
                for (final double pos : this.oneblockPositions) {
                    Step.mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(Step.mc.player.getX(), Step.mc.player.getY() + pos, Step.mc.player.getZ(), true));
                }
                Step.mc.player.setPos(Step.mc.player.getX(), Step.mc.player.getY() + this.oneblockPositions[this.oneblockPositions.length - 1], Step.mc.player.getZ());
                this.packets = 0;
            }
        }
    }

    public double get_n_normal() {
        Step.mc.player.maxUpStep = 0.5f;
        double max_y = -1.0;
        final AABB grow = Step.mc.player.getBoundingBox().move(0.0, 0.05, 0.0).inflate(0.05);
        if (Step.mc.level.getBlockCollisions(Step.mc.player, grow.move(0.0, 2.0, 0.0)).iterator().hasNext()) {
            return 100.0;
        }
        for (final VoxelShape shape : Step.mc.level.getBlockCollisions(Step.mc.player, grow)) {
            final double shapeMax = shape.max(Direction.Axis.Y);
            if (shapeMax > max_y) {
                max_y = shapeMax;
            }
        }
        return max_y - Step.mc.player.getY();
    }
}