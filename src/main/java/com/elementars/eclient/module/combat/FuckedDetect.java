package com.elementars.eclient.module.combat;

import com.elementars.eclient.friend.Friends;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import dev.xulu.settings.Value;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FuckedDetect extends Module
{
    private final Value<Integer> red;
    private final Value<Integer> green;
    private final Value<Integer> blue;
    private final Value<Integer> alpha;
    private final Value<Integer> distance;
    private final Value<Boolean> drawFriends;
    private final Value<Boolean> drawOwn;
    private final Value<String> renderMode;
    public Set<Player> fuckedPlayers;

    public FuckedDetect() {
        super("FuckedDetector", "Detects when people are fucked", 0, Category.COMBAT, true);
        this.red = this.register(new Value<Integer>("Red", this, 255, 0, 255));
        this.green = this.register(new Value<Integer>("Green", this, 255, 0, 255));
        this.blue = this.register(new Value<Integer>("Blue", this, 255, 0, 255));
        this.alpha = this.register(new Value<Integer>("Alpha", this, 70, 0, 255));
        this.distance = this.register(new Value<Integer>("Draw Distance", this, 20, 0, 30));
        this.drawFriends = this.register(new Value<Boolean>("Draw Friends", this, false));
        this.drawOwn = this.register(new Value<Boolean>("Draw Own", this, false));
        this.renderMode = this.register(new Value<String>("RenderMode", this, "Solid", new ArrayList<String>(Arrays.asList("Solid", "Flat", "Outline", "Full"))));
        this.fuckedPlayers = new HashSet<Player>();
    }

    @Override
    public void onEnable() {
        this.fuckedPlayers = new HashSet<Player>();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        this.fuckedPlayers.clear();
    }

    @Override
    public void onUpdate() {
        if (FuckedDetect.mc.player == null || FuckedDetect.mc.level == null || FuckedDetect.mc.player.isDeadOrDying()) {
            this.fuckedPlayers.clear();
            return;
        }
        this.getList();
    }

    @SubscribeEvent
    public void onWorldLast(final RenderLevelLastEvent event) {
        if (this.fuckedPlayers.isEmpty() || FuckedDetect.mc.level == null) {
            return;
        }
        final Camera camera = FuckedDetect.mc.gameRenderer.getMainCamera();
        final Vec3 cam = camera.getPosition();
        final PoseStack poseStack = new PoseStack();
        poseStack.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0f));
        poseStack.translate(-cam.x, -cam.y, -cam.z);
        final Matrix4f matrix = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(1.5f);

        final Tesselator tesselator = Tesselator.getInstance();
        for (final Player player : this.fuckedPlayers) {
            this.drawBlock(tesselator, matrix, player.blockPosition());
        }

        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawBlock(final Tesselator tesselator, final Matrix4f m, final BlockPos pos) {
        final AABB bb = new AABB(pos).inflate(0.002);
        final int r = this.red.getValue();
        final int g = this.green.getValue();
        final int b = this.blue.getValue();
        final int a = this.alpha.getValue();
        final String mode = this.renderMode.getValue();
        if (mode.equalsIgnoreCase("Solid")) {
            this.fillBox(tesselator, m, bb, r, g, b, a, false);
        }
        else if (mode.equalsIgnoreCase("Flat")) {
            this.fillBox(tesselator, m, bb, r, g, b, a, true);
        }
        else if (mode.equalsIgnoreCase("Outline")) {
            this.lineBox(tesselator, m, bb, r, g, b, a);
        }
        else if (mode.equalsIgnoreCase("Full")) {
            this.fillBox(tesselator, m, bb, r, g, b, a, false);
            this.lineBox(tesselator, m, bb, r, g, b, 255);
        }
    }

    private static void v(final BufferBuilder buf, final Matrix4f m, final float x, final float y, final float z, final int r, final int g, final int b, final int a) {
        buf.vertex(m, x, y, z).color(r, g, b, a).endVertex();
    }

    private void fillBox(final Tesselator tesselator, final Matrix4f m, final AABB bb, final int r, final int g, final int b, final int a, final boolean bottomOnly) {
        final float x0 = (float)bb.minX, y0 = (float)bb.minY, z0 = (float)bb.minZ;
        final float x1 = (float)bb.maxX, y1 = (float)bb.maxY, z1 = (float)bb.maxZ;
        final BufferBuilder buf = tesselator.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        // abajo
        v(buf, m, x0, y0, z0, r, g, b, a); v(buf, m, x1, y0, z0, r, g, b, a); v(buf, m, x1, y0, z1, r, g, b, a); v(buf, m, x0, y0, z1, r, g, b, a);
        if (!bottomOnly) {
            // arriba
            v(buf, m, x0, y1, z0, r, g, b, a); v(buf, m, x0, y1, z1, r, g, b, a); v(buf, m, x1, y1, z1, r, g, b, a); v(buf, m, x1, y1, z0, r, g, b, a);
            // norte
            v(buf, m, x0, y0, z0, r, g, b, a); v(buf, m, x0, y1, z0, r, g, b, a); v(buf, m, x1, y1, z0, r, g, b, a); v(buf, m, x1, y0, z0, r, g, b, a);
            // sur
            v(buf, m, x0, y0, z1, r, g, b, a); v(buf, m, x1, y0, z1, r, g, b, a); v(buf, m, x1, y1, z1, r, g, b, a); v(buf, m, x0, y1, z1, r, g, b, a);
            // oeste
            v(buf, m, x0, y0, z0, r, g, b, a); v(buf, m, x0, y0, z1, r, g, b, a); v(buf, m, x0, y1, z1, r, g, b, a); v(buf, m, x0, y1, z0, r, g, b, a);
            // este
            v(buf, m, x1, y0, z0, r, g, b, a); v(buf, m, x1, y1, z0, r, g, b, a); v(buf, m, x1, y1, z1, r, g, b, a); v(buf, m, x1, y0, z1, r, g, b, a);
        }
        tesselator.end();
    }

    private void lineBox(final Tesselator tesselator, final Matrix4f m, final AABB bb, final int r, final int g, final int b, final int a) {
        final float x0 = (float)bb.minX, y0 = (float)bb.minY, z0 = (float)bb.minZ;
        final float x1 = (float)bb.maxX, y1 = (float)bb.maxY, z1 = (float)bb.maxZ;
        final BufferBuilder buf = tesselator.getBuilder();
        buf.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        // base inferior
        v(buf, m, x0, y0, z0, r, g, b, a); v(buf, m, x1, y0, z0, r, g, b, a);
        v(buf, m, x1, y0, z0, r, g, b, a); v(buf, m, x1, y0, z1, r, g, b, a);
        v(buf, m, x1, y0, z1, r, g, b, a); v(buf, m, x0, y0, z1, r, g, b, a);
        v(buf, m, x0, y0, z1, r, g, b, a); v(buf, m, x0, y0, z0, r, g, b, a);
        // base superior
        v(buf, m, x0, y1, z0, r, g, b, a); v(buf, m, x1, y1, z0, r, g, b, a);
        v(buf, m, x1, y1, z0, r, g, b, a); v(buf, m, x1, y1, z1, r, g, b, a);
        v(buf, m, x1, y1, z1, r, g, b, a); v(buf, m, x0, y1, z1, r, g, b, a);
        v(buf, m, x0, y1, z1, r, g, b, a); v(buf, m, x0, y1, z0, r, g, b, a);
        // verticales
        v(buf, m, x0, y0, z0, r, g, b, a); v(buf, m, x0, y1, z0, r, g, b, a);
        v(buf, m, x1, y0, z0, r, g, b, a); v(buf, m, x1, y1, z0, r, g, b, a);
        v(buf, m, x1, y0, z1, r, g, b, a); v(buf, m, x1, y1, z1, r, g, b, a);
        v(buf, m, x0, y0, z1, r, g, b, a); v(buf, m, x0, y1, z1, r, g, b, a);
        tesselator.end();
    }

    private boolean canPlaceCrystal(final BlockPos pos) {
        final BlockPos boost = pos.above();
        final BlockPos boost2 = pos.above(2);
        return (FuckedDetect.mc.level.getBlockState(pos).getBlock() == Blocks.BEDROCK || FuckedDetect.mc.level.getBlockState(pos).getBlock() == Blocks.OBSIDIAN)
                && FuckedDetect.mc.level.getBlockState(boost).isAir()
                && FuckedDetect.mc.level.getBlockState(boost2).isAir()
                && FuckedDetect.mc.level.getEntitiesOfClass(Entity.class, new AABB(boost)).isEmpty()
                && FuckedDetect.mc.level.getEntitiesOfClass(Entity.class, new AABB(boost2)).isEmpty();
    }

    public Boolean checkHole(final Player player) {
        final BlockPos pos = new BlockPos((int)Math.floor(player.getX()), (int)Math.floor(player.getY() - 1.0), (int)Math.floor(player.getZ()));
        if (FuckedDetect.mc.level.getBlockState(pos).isAir()) {
            return false;
        }
        for (final Direction dir : new Direction[] { Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.NORTH }) {
            if (this.canPlaceCrystal(pos.relative(dir))
                    || (this.canPlaceCrystal(pos.relative(dir).relative(dir))
                    && FuckedDetect.mc.level.getBlockState(pos.above().relative(dir)).isAir())) {
                return true;
            }
        }
        return false;
    }

    public Set<Player> getList() {
        this.fuckedPlayers.clear();
        for (final Player player : FuckedDetect.mc.level.players()) {
            if (player == null || player.getHealth() <= 0.0f) {
                continue;
            }
            if (!this.checkHole(player)) {
                continue;
            }
            if (!this.drawOwn.getValue() && player == FuckedDetect.mc.player) {
                continue;
            }
            if (!this.drawFriends.getValue() && Friends.isFriend(player.getName().getString())) {
                continue;
            }
            if (FuckedDetect.mc.player.distanceTo(player) > this.distance.getValue()) {
                continue;
            }
            this.fuckedPlayers.add(player);
        }
        return this.fuckedPlayers;
    }
}