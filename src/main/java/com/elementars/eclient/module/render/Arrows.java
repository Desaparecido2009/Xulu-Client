package com.elementars.eclient.module.render;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.friend.Friends;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
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
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Arrows extends Module
{
    public final Value<Mode> mode;
    public final Value<Integer> sizeX;
    public final Value<Integer> sizeY;
    public final Value<Float> sizeT;
    public final Value<Boolean> rainbow;
    public final Value<String> colorMode;
    public final Value<Integer> red;
    public final Value<Integer> green;
    public final Value<Integer> blue;
    public final Value<Integer> Fr;
    public final Value<Integer> Fg;
    public final Value<Integer> Fb;
    public final Value<Integer> alpha;
    public final Value<Boolean> outline;
    public final Value<Boolean> black;
    public final Value<Boolean> antialias;
    public final Value<Boolean> players;
    public final Value<Boolean> hostile;
    public final Value<Boolean> friendly;

    public Arrows() {
        super("Arrows", "2d Tracers", 0, Category.RENDER, true);
        this.mode = this.register(new Value<Mode>("Mode", this, Mode.ARROWS, Mode.values()));
        this.sizeX = this.register(new Value<Integer>("Dimension X", this, 450, 0, 1000));
        this.sizeY = this.register(new Value<Integer>("Dimension Y", this, 285, 0, 1000));
        this.sizeT = this.register(new Value<Float>("Triangle Size", this, 2.0f, 1.0f, 5.0f));
        this.rainbow = this.register(new Value<Boolean>("Rainbow Players", this, false));
        this.colorMode = this.register(new Value<String>("Color Mode", this, "RGB", new String[] { "RGB", "Tracers" }));
        this.red = this.register(new Value<Integer>("Player Red", this, 0, 0, 255)).visibleWhen(t -> this.colorMode.getValue().equalsIgnoreCase("RGB"));
        this.green = this.register(new Value<Integer>("Player Green", this, 255, 0, 255)).visibleWhen(t -> this.colorMode.getValue().equalsIgnoreCase("RGB"));
        this.blue = this.register(new Value<Integer>("Player Blue", this, 0, 0, 255)).visibleWhen(t -> this.colorMode.getValue().equalsIgnoreCase("RGB"));
        this.Fr = this.register(new Value<Integer>("Friend Red", this, 0, 0, 255));
        this.Fg = this.register(new Value<Integer>("Friend Green", this, 200, 0, 255));
        this.Fb = this.register(new Value<Integer>("Friend Blue", this, 255, 0, 255));
        this.alpha = this.register(new Value<Integer>("Alpha", this, 255, 0, 255));
        this.outline = this.register(new Value<Boolean>("Outline", this, false));
        this.black = this.register(new Value<Boolean>("Black Outline", this, true)).visibleWhen(aBoolean -> this.outline.getValue());
        this.antialias = this.register(new Value<Boolean>("Antialias", this, true));
        this.players = this.register(new Value<Boolean>("Players", this, true));
        this.hostile = this.register(new Value<Boolean>("Mobs", this, true));
        this.friendly = this.register(new Value<Boolean>("Animals", this, true));
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onOverlay(final RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        this.draw(event.getPartialTicks());
    }

    private void draw(final float partialTick) {
        if (Arrows.mc.level == null || Arrows.mc.player == null) {
            return;
        }
        final int width = Arrows.mc.getWindow().getGuiScaledWidth();
        final int height = Arrows.mc.getWindow().getGuiScaledHeight();
        final double cx = width / 2.0;
        final double cy = height / 2.0;
        final double aspect = (double)Arrows.mc.getWindow().getWidth() / (double)Arrows.mc.getWindow().getHeight();
        final double tanHalfFov = Math.tan(Math.toRadians(Arrows.mc.options.fov) / 2.0);

        final Camera camera = Arrows.mc.gameRenderer.getMainCamera();
        final Vec3 camPos = camera.getPosition();
        final Vector3f look = camera.getLookVector();
        final Vector3f up = camera.getUpVector();
        final Vector3f left = camera.getLeftVector();

        final List<EntityRelations> entities = new ArrayList<EntityRelations>();
        for (final Entity entity : Arrows.mc.level.entitiesForRendering()) {
            if (entity == Arrows.mc.player || !(entity instanceof LivingEntity)) {
                continue;
            }
            if (!entity.isAlive() || ((LivingEntity)entity).isDeadOrDying()) {
                continue;
            }
            final EntityRelations relations = new EntityRelations(entity);
            if (!relations.isOptionEnabled()) {
                continue;
            }
            entities.add(relations);
        }
        entities.sort(Comparator.comparing((EntityRelations r) -> r.relationship));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(1.0f);
        if (this.antialias.getValue()) {
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        }

        final Mode renderMode = this.mode.getValue();
        final Tesselator tesselator = Tesselator.getInstance();
        final double marginX = Math.max(10.0, Math.min(cx - 10.0, this.sizeX.getValue() / 2.0));
        final double marginY = Math.max(10.0, Math.min(cy - 10.0, this.sizeY.getValue() / 2.0));

        for (final EntityRelations relations : entities) {
            final Entity entity = relations.entity;
            final double ex = Mth.lerp(partialTick, entity.xo, entity.getX());
            final double ey = Mth.lerp(partialTick, entity.yo, entity.getY()) + entity.getBbHeight() * 0.8;
            final double ez = Mth.lerp(partialTick, entity.zo, entity.getZ());

            // Posición relativa a la cámara, descompuesta en los ejes de la cámara
            final double rx = ex - camPos.x;
            final double ry = ey - camPos.y;
            final double rz = ez - camPos.z;
            final double fwd = rx * look.x() + ry * look.y() + rz * look.z();
            final double upD = rx * up.x() + ry * up.y() + rz * up.z();
            final double rightD = -(rx * left.x() + ry * left.y() + rz * left.z());

            double dx;
            double dy;
            double sx;
            double sy;
            boolean visible = false;

            if (fwd > 0.05) {
                final double ndcX = rightD / (fwd * tanHalfFov * aspect);
                final double ndcY = upD / (fwd * tanHalfFov);
                sx = (ndcX * 0.5 + 0.5) * width;
                sy = (0.5 - ndcY * 0.5) * height;
                visible = sx >= 0 && sx <= width && sy >= 0 && sy <= height;
                dx = sx - cx;
                dy = sy - cy;
            }
            else {
                // Detrás de la cámara: la flecha apunta hacia el lado donde hay que girar
                dx = Math.abs(rightD) < 1.0E-3 ? 1.0 : Math.signum(rightD) * 1000.0;
                dy = 0.0;
                sx = cx + dx;
                sy = cy;
            }

            final float[] color = this.getColor(relations);
            final float a = this.alpha.getValue() / 255.0f;

            // Punto en el borde del rectángulo configurable
            double x = sx;
            double y = sy;
            if (!visible) {
                if (Math.abs(dx) < 1.0E-4 && Math.abs(dy) < 1.0E-4) {
                    continue;
                }
                final double scaleX = Math.abs(dx) < 1.0E-4 ? Double.MAX_VALUE : marginX / Math.abs(dx);
                final double scaleY = Math.abs(dy) < 1.0E-4 ? Double.MAX_VALUE : marginY / Math.abs(dy);
                final double factor = Math.min(scaleX, scaleY);
                x = cx + dx * factor;
                y = cy + dy * factor;
            }

            if ((renderMode == Mode.ARROWS || renderMode == Mode.BOTH) && !visible) {
                final double angle = Math.atan2(y - cy, x - cx);
                final double cos = Math.cos(angle);
                final double sin = Math.sin(angle);
                final int size = relations.relationship == RelationState.PLAYER ? 8 : 5;
                final double v1x = -size;
                final double v1y = -size / (double)this.sizeT.getValue();
                final double v2x = -size;
                final double v2y = size / (double)this.sizeT.getValue();
                final float r1x = (float)(v1x * cos - v1y * sin);
                final float r1y = (float)(v1x * sin + v1y * cos);
                final float r2x = (float)(v2x * cos - v2y * sin);
                final float r2y = (float)(v2x * sin + v2y * cos);
                final float fx = (float)x;
                final float fy = (float)y;

                BufferBuilder buffer = tesselator.getBuilder();
                buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
                buffer.vertex(fx, fy, 0.0).color(color[0], color[1], color[2], a).endVertex();
                buffer.vertex(fx + r1x, fy + r1y, 0.0).color(color[0], color[1], color[2], a).endVertex();
                buffer.vertex(fx + r2x, fy + r2y, 0.0).color(color[0], color[1], color[2], a).endVertex();
                tesselator.end();

                if (this.outline.getValue()) {
                    final float[] oc = this.black.getValue() ? new float[] { 0.0f, 0.0f, 0.0f } : color;
                    buffer = tesselator.getBuilder();
                    buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                    buffer.vertex(fx, fy, 0.0).color(oc[0], oc[1], oc[2], 1.0f).endVertex();
                    buffer.vertex(fx + r1x, fy + r1y, 0.0).color(oc[0], oc[1], oc[2], 1.0f).endVertex();
                    buffer.vertex(fx + r1x, fy + r1y, 0.0).color(oc[0], oc[1], oc[2], 1.0f).endVertex();
                    buffer.vertex(fx + r2x, fy + r2y, 0.0).color(oc[0], oc[1], oc[2], 1.0f).endVertex();
                    buffer.vertex(fx + r2x, fy + r2y, 0.0).color(oc[0], oc[1], oc[2], 1.0f).endVertex();
                    buffer.vertex(fx, fy, 0.0).color(oc[0], oc[1], oc[2], 1.0f).endVertex();
                    tesselator.end();
                }
            }

            if (renderMode == Mode.LINES || renderMode == Mode.BOTH) {
                final BufferBuilder buffer = tesselator.getBuilder();
                buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                buffer.vertex(cx, cy, 0.0).color(color[0], color[1], color[2], a).endVertex();
                buffer.vertex(x, y, 0.0).color(color[0], color[1], color[2], a).endVertex();
                tesselator.end();
            }
        }

        if (this.antialias.getValue()) {
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        }
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private float[] getColor(final EntityRelations relations) {
        if (relations.relationship == RelationState.PLAYER) {
            if (Friends.isFriend(relations.entity.getName().getString())) {
                return new float[] { this.Fr.getValue() / 255.0f, this.Fg.getValue() / 255.0f, this.Fb.getValue() / 255.0f };
            }
            if (this.rainbow.getValue()) {
                final Color rainbowColor = new Color(Xulu.rgb);
                return new float[] { rainbowColor.getRed() / 255.0f, rainbowColor.getGreen() / 255.0f, rainbowColor.getBlue() / 255.0f };
            }
        }
        if (this.colorMode.getValue().equalsIgnoreCase("Tracers")) {
            final double distance = Arrows.mc.player.distanceTo(relations.entity);
            if (distance <= 32.0) {
                return new float[] { (float)(1.0 - distance / 32.0 / 2.0), (float)(distance / 32.0), 0.0f };
            }
            return new float[] { 0.0f, 0.9f, 0.0f };
        }
        if (relations.relationship == RelationState.PLAYER) {
            return new float[] { this.red.getValue() / 255.0f, this.green.getValue() / 255.0f, this.blue.getValue() / 255.0f };
        }
        if (relations.relationship == RelationState.HOSTILE) {
            return new float[] { 1.0f, 0.0f, 0.0f };
        }
        return new float[] { 1.0f, 1.0f, 0.0f };
    }

    public enum Mode
    {
        ARROWS,
        LINES,
        BOTH
    }

    public enum RelationState
    {
        PLAYER,
        HOSTILE,
        FRIENDLY
    }

    private class EntityRelations
    {
        private final Entity entity;
        private final RelationState relationship;

        public EntityRelations(final Entity entity) {
            this.entity = entity;
            if (entity instanceof Player) {
                this.relationship = RelationState.PLAYER;
            }
            else if (entity instanceof Enemy) {
                this.relationship = RelationState.HOSTILE;
            }
            else {
                this.relationship = RelationState.FRIENDLY;
            }
        }

        public boolean isOptionEnabled() {
            switch (this.relationship) {
                case PLAYER:
                    return Arrows.this.players.getValue();
                case HOSTILE:
                    return Arrows.this.hostile.getValue();
                default:
                    return Arrows.this.friendly.getValue();
            }
        }
    }
}