package com.elementars.eclient.module.render;

import com.elementars.eclient.event.events.RenderEvent;
import com.elementars.eclient.friend.Friends;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.ColorUtils;
import com.elementars.eclient.util.EntityUtil;
import com.elementars.eclient.util.HueCycler;
import com.elementars.eclient.util.XuluTessellator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import dev.xulu.settings.Value;
import java.awt.Color;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

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
    public void onUpdate() {
        this.cycler.next();
    }

    @Override
    public void onWorldRender(final RenderEvent event) {
        if (Tracers.mc.level == null || Tracers.mc.player == null) {
            return;
        }
        for (final Entity entity : Tracers.mc.level.entitiesForRendering()) {
            if (!EntityUtil.isLiving(entity) || EntityUtil.isFakeLocalPlayer(entity)) {
                continue;
            }
            if (entity instanceof Player) {
                if (!this.players.getValue() || Tracers.mc.player == entity) {
                    continue;
                }
            }
            else if (!(EntityUtil.isPassive(entity) ? this.animals.getValue() : this.mobs.getValue())) {
                continue;
            }
            if (Tracers.mc.player.distanceTo(entity) >= this.range.getValue()) {
                continue;
            }
            int colour = this.getColour(entity);
            if (colour == Integer.MIN_VALUE) {
                if (!this.friends.getValue()) {
                    continue;
                }
                colour = this.cycler.current();
            }
            final float red = (colour >> 16 & 0xFF) / 255.0f;
            final float green = (colour >> 8 & 0xFF) / 255.0f;
            final float blue = (colour & 0xFF) / 255.0f;
            drawLineToEntity(entity, red, green, blue, this.opacity.getValue());
        }
    }

    private int getColour(final Entity entity) {
        if (entity instanceof Player) {
            if (Friends.isFriend(entity.getName().getString())) {
                return Integer.MIN_VALUE;
            }
            final float distance = Tracers.mc.player.distanceTo(entity);
            if (distance <= 32.0f) {
                return new Color(1.0f - distance / 32.0f / 2.0f, distance / 32.0f, 0.0f).getRGB();
            }
            return new Color(0.0f, 0.9f, 0.0f).getRGB();
        }
        if (EntityUtil.isPassive(entity)) {
            return ColorUtils.Colors.GREEN;
        }
        return ColorUtils.Colors.RED;
    }

    public static double interpolate(final double now, final double then) {
        return then + (now - then) * Tracers.mc.getFrameTime();
    }

    public static double[] interpolate(final Entity entity) {
        final Vec3 renderPos = Tracers.mc.gameRenderer.getMainCamera().getPosition();
        final Vec3 pos = EntityUtil.getInterpolatedPos(entity, Tracers.mc.getFrameTime());
        return new double[] { pos.x - renderPos.x, pos.y - renderPos.y, pos.z - renderPos.z };
    }

    public static void drawLineToEntity(final Entity entity, final float red, final float green, final float blue, final float opacity) {
        final double[] xyz = interpolate(entity);
        drawLine(xyz[0], xyz[1], xyz[2], entity.getBbHeight(), red, green, blue, opacity);
    }

    public static void drawLine(final double posx, final double posy, final double posz, final double up, final float red, final float green, final float blue, final float opacity) {
        final Vec3 eyes = Vec3.directionFromRotation(Tracers.mc.player.getXRot(), Tracers.mc.player.getYRot());
        final Vec3 eye = Tracers.mc.player.getEyePosition(Tracers.mc.getFrameTime()).subtract(Tracers.mc.gameRenderer.getMainCamera().getPosition());
        drawLineFromPosToPos(eye.x + eyes.x, eye.y + eyes.y, eye.z + eyes.z, posx, posy, posz, up, red, green, blue, opacity);
    }

    public static void drawLineFromPosToPos(final double posx, final double posy, final double posz, final double posx2, final double posy2, final double posz2, final double up, final float red, final float green, final float blue, final float opacity) {
        RenderSystem.lineWidth(1.5f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        XuluTessellator.prepare(1);
        XuluTessellator.begin(1);
        final BufferBuilder buffer = XuluTessellator.getBufferBuilder();
        buffer.vertex(posx, posy, posz).color(red, green, blue, opacity).endVertex();
        buffer.vertex(posx2, posy2, posz2).color(red, green, blue, opacity).endVertex();
        if (Tracers.INSTANCE != null && Tracers.INSTANCE.spine.getValue()) {
            buffer.vertex(posx2, posy2, posz2).color(red, green, blue, opacity).endVertex();
            buffer.vertex(posx2, posy2 + up, posz2).color(red, green, blue, opacity).endVertex();
        }
        Tesselator.getInstance().end();
        RenderSystem.lineWidth(1.0f);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
