package com.elementars.eclient.module.render;

import com.elementars.eclient.event.events.RenderEvent;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.EntityUtil;
import com.elementars.eclient.util.XuluTessellator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import dev.xulu.settings.Value;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ESP extends Module
{
    private final Value<Boolean> players;
    private final Value<Boolean> animals;
    private final Value<Boolean> mobs;

    public ESP() {
        super("ESP", "Highlights entities", 0, Category.RENDER, true);
        this.players = this.register(new Value<Boolean>("Players", this, true));
        this.animals = this.register(new Value<Boolean>("Animals", this, false));
        this.mobs = this.register(new Value<Boolean>("Mobs", this, false));
    }

    @Override
    public void onWorldRender(final RenderEvent event) {
        if (ESP.mc.level == null || ESP.mc.player == null) {
            return;
        }
        final float partialTicks = ESP.mc.getFrameTime();
        final Vec3 renderPos = event.getRenderPos();
        final Camera camera = ESP.mc.gameRenderer.getMainCamera();
        final double yaw = Math.toRadians(camera.getYRot());
        final double rightX = -Math.cos(yaw);
        final double rightZ = -Math.sin(yaw);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        XuluTessellator.prepareGL();
        RenderSystem.lineWidth(2.0f);
        XuluTessellator.begin(1);
        final BufferBuilder buffer = XuluTessellator.getBufferBuilder();
        for (final Entity entity : ESP.mc.level.entitiesForRendering()) {
            if (entity == ESP.mc.player || EntityUtil.isFakeLocalPlayer(entity) || !(entity instanceof LivingEntity)) {
                continue;
            }
            final LivingEntity living = (LivingEntity)entity;
            if (!living.isAlive() || living.isDeadOrDying()) {
                continue;
            }
            if (living instanceof Player) {
                if (!this.players.getValue()) {
                    continue;
                }
            }
            else if (EntityUtil.isPassive(living)) {
                if (!this.animals.getValue()) {
                    continue;
                }
            }
            else if (!this.mobs.getValue()) {
                continue;
            }
            final Vec3 pos = EntityUtil.getInterpolatedPos(entity, partialTicks);
            final double halfWidth = entity.getBbWidth() / 2.0;
            final double height = entity.getBbHeight();
            float r = 0.9f;
            float g = 0.1f;
            float b = 0.1f;
            if (living instanceof Player) {
                r = 1.0f;
                g = 1.0f;
                b = 1.0f;
            }
            else if (EntityUtil.isPassive(living)) {
                r = 0.11f;
                g = 0.9f;
                b = 0.11f;
            }
            final double x = pos.x - renderPos.x;
            final double y = pos.y - renderPos.y;
            final double z = pos.z - renderPos.z;
            final double x1 = x - rightX * halfWidth;
            final double z1 = z - rightZ * halfWidth;
            final double x2 = x + rightX * halfWidth;
            final double z2 = z + rightZ * halfWidth;
            buffer.vertex(x1, y, z1).color(r, g, b, 1.0f).endVertex();
            buffer.vertex(x1, y + height, z1).color(r, g, b, 1.0f).endVertex();
            buffer.vertex(x1, y + height, z1).color(r, g, b, 1.0f).endVertex();
            buffer.vertex(x2, y + height, z2).color(r, g, b, 1.0f).endVertex();
            buffer.vertex(x2, y + height, z2).color(r, g, b, 1.0f).endVertex();
            buffer.vertex(x2, y, z2).color(r, g, b, 1.0f).endVertex();
            buffer.vertex(x2, y, z2).color(r, g, b, 1.0f).endVertex();
            buffer.vertex(x1, y, z1).color(r, g, b, 1.0f).endVertex();
        }
        XuluTessellator.render();
        RenderSystem.lineWidth(1.0f);
        XuluTessellator.releaseGL();
    }
}
