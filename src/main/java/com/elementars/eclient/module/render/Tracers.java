package com.elementars.eclient.module.render;

import com.elementars.eclient.friend.Friends;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.HueCycler;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Tracers extends Module
{
    private final Value<Boolean> spine;
    private final Value<Boolean> players;
    private final Value<Boolean> friends;
    private final Value<Boolean> animals;
    private final Value<Boolean> mobs;
    private final Value<Float> range;
    private final Value<Float> opacity;
    private final HueCycler cycler;
    public static Tracers INSTANCE;

    public Tracers() {
        super("Tracers", "Draws a line to entities in render distance", 0, Category.RENDER, true);
        this.spine = this.register(new Value<Boolean>("Spine", this, false));
        this.players = this.register(new Value<Boolean>("Players", this, true));
        this.friends = this.register(new Value<Boolean>("Friends", this, true));
        this.animals = this.register(new Value<Boolean>("Animals", this, false));
        this.mobs = this.register(new Value<Boolean>("Mobs", this, false));
        this.range = this.register(new Value<Float>("Range", this, 200.0f, 1.0f, 1000.0f));
        this.opacity = this.register(new Value<Float>("Opacity", this, 1.0f, 0.0f, 1.0f));
        this.cycler = new HueCycler(3600);
        Tracers.INSTANCE = this;
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public void onUpdate() {
        this.cycler.next();
    }

    @SubscribeEvent
    public void onWorldLast(final RenderLevelLastEvent event) {
        if (Tracers.mc.level == null || Tracers.mc.player == null) {
            return;
        }
        final float pt = event.getPartialTick();
        final Camera camera = Tracers.mc.gameRenderer.getMainCamera();
        final Vec3 cam = camera.getPosition();

        final PoseStack poseStack = new PoseStack();
        poseStack.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0f));
        final Matrix4f m = poseStack.last().pose();

        // Punto de inicio: 1 bloque delante de la cámara (relativo a la cámara)
        final Vec3 look = Vec3.directionFromRotation(camera.getXRot(), camera.getYRot());
        final float sx = (float)look.x;
        final float sy = (float)look.y;
        final float sz = (float)look.z;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(1.5f);

        final Tesselator tesselator = Tesselator.getInstance();
        final BufferBuilder buf = tesselator.getBuilder();
        buf.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        boolean any = false;
        final float alpha = this.opacity.getValue();

        for (final Entity entity : Tracers.mc.level.entitiesForRendering()) {
            if (entity == Tracers.mc.player || !(entity instanceof LivingEntity) || !entity.isAlive()) {
                continue;
            }
            if (entity instanceof Player) {
                if (!this.players.getValue()) {
                    continue;
                }
            }
            else if (entity instanceof Enemy) {
                if (!this.mobs.getValue()) {
                    continue;
                }
            }
            else if (!this.animals.getValue()) {
                continue;
            }
            if (Tracers.mc.player.distanceTo(entity) >= this.range.getValue()) {
                continue;
            }

            final float[] c = this.getColour(entity);
            if (c == null) {
                continue;
            }

            final float ex = (float)(Mth.lerp(pt, entity.xo, entity.getX()) - cam.x);
            final float ey = (float)(Mth.lerp(pt, entity.yo, entity.getY()) - cam.y);
            final float ez = (float)(Mth.lerp(pt, entity.zo, entity.getZ()) - cam.z);

            buf.vertex(m, sx, sy, sz).color(c[0], c[1], c[2], alpha).endVertex();
            buf.vertex(m, ex, ey, ez).color(c[0], c[1], c[2], alpha).endVertex();
            if (this.spine.getValue()) {
                buf.vertex(m, ex, ey, ez).color(c[0], c[1], c[2], alpha).endVertex();
                buf.vertex(m, ex, ey + entity.getBbHeight(), ez).color(c[0], c[1], c[2], alpha).endVertex();
            }
            any = true;
        }

        if (any) {
            tesselator.end();
        }
        else {
            buf.discard();
        }

        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    /** Devuelve {r,g,b} o null si la entidad no se debe dibujar. */
    private float[] getColour(final Entity entity) {
        if (entity instanceof Player) {
            if (Friends.isFriend(entity.getName().getString())) {
                if (!this.friends.getValue()) {
                    return null;
                }
                final int rgb = this.cycler.current();
                return new float[] { (rgb >> 16 & 0xFF) / 255.0f, (rgb >> 8 & 0xFF) / 255.0f, (rgb & 0xFF) / 255.0f };
            }
            final float distance = Tracers.mc.player.distanceTo(entity);
            if (distance <= 32.0f) {
                return new float[] { 1.0f - distance / 32.0f / 2.0f, distance / 32.0f, 0.0f };
            }
            return new float[] { 0.0f, 0.9f, 0.0f };
        }
        if (entity instanceof Enemy) {
            return new float[] { 1.0f, 0.0f, 0.0f };
        }
        return new float[] { 0.0f, 1.0f, 0.0f };
    }
}