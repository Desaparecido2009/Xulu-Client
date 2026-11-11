package com.elementars.eclient.module.render;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.command.Command;
import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventPlayerConnect;
import com.elementars.eclient.event.events.LocalPlayerUpdateEvent;
import com.elementars.eclient.event.events.RenderEvent;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.ColorTextUtils;
import com.elementars.eclient.util.Pair;
import com.elementars.eclient.util.SpotSet;
import com.elementars.eclient.util.XuluTessellator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import dev.xulu.settings.Value;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.Color;
import java.util.UUID;

public class LogoutSpots extends Module
{
    private final Value<Boolean> cf;
    private final Value<Boolean> render;
    private final Value<Boolean> box;
    private final Value<Boolean> coords;
    private final Value<Integer> max_distance;
    private final Value<Boolean> print_message;
    private final Value<Boolean> watermark;
    private final Value<String> color;
    private final Value<Integer> red;
    private final Value<Integer> green;
    private final Value<Integer> blue;
    private final SpotSet spots;

    public LogoutSpots() {
        super("LogoutSpot", "show where a player logs out", 0, Category.RENDER, true);
        this.cf = this.register(new Value<Boolean>("Custom Font", this, false));
        this.render = this.register(new Value<Boolean>("Render", this, true));
        this.box = this.register(new Value<Boolean>("Box", this, false));
        this.coords = this.register(new Value<Boolean>("Coordinates", this, true));
        this.max_distance = this.register(new Value<Integer>("Max Distance", this, 320, 0, 1000));
        this.print_message = this.register(new Value<Boolean>("Print Message", this, true));
        this.watermark = this.register(new Value<Boolean>("Watermark", this, true));
        this.color = this.register(new Value<String>("Color", this, "White", ColorTextUtils.colors));
        this.red = this.register(new Value<Integer>("Red", this, 255, 0, 255));
        this.green = this.register(new Value<Integer>("Green", this, 0, 0, 255));
        this.blue = this.register(new Value<Integer>("Blue", this, 0, 0, 255));
        this.spots = new SpotSet();
    }

    private void reset() {
        synchronized (this.spots) {
            this.spots.clear();
        }
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        this.reset();
    }

    @EventTarget
    public void onPlayerConnect(final EventPlayerConnect.Join event) {
        LogoutSpots.mc.execute(() -> {
            synchronized (this.spots) {
                final Pair<Boolean, LogoutPos> check = this.spots.removeIfReturn(spot -> spot.getId().equals(event.getUuid()));
                if (check.getKey() && this.print_message.getValue()) {
                    final double x = check.getValue().lastTickPosX;
                    final double y = check.getValue().lastTickPosY;
                    final double z = check.getValue().lastTickPosZ;
                    final String message = ColorTextUtils.getColor(this.color.getValue()) + String.format("%s has joined (%s, %s, %s)!", event.getName(), (int)x, (int)y, (int)z);
                    if (this.watermark.getValue()) {
                        Command.sendChatMessage(message);
                    }
                    else {
                        Command.sendRawChatMessage(message);
                    }
                }
            }
        });
    }

    @EventTarget
    public void onPlayerDisconnect(final EventPlayerConnect.Leave event) {
        if (LogoutSpots.mc.level == null || LogoutSpots.mc.player == null) {
            return;
        }
        Player player = null;
        for (final Player candidate : LogoutSpots.mc.level.players()) {
            if (candidate.getUUID().equals(event.getUuid())) {
                player = candidate;
                break;
            }
        }
        if (player == null || LogoutSpots.mc.player.equals(player)) {
            return;
        }
        final AABB bb = player.getBoundingBox();
        synchronized (this.spots) {
            if (this.spots.add(new LogoutPos(event.getUuid(), player.getName().getString(), bb, player)) && this.print_message.getValue()) {
                final String message = ColorTextUtils.getColor(this.color.getValue()) + String.format("%s has disconnected!", player.getName().getString());
                if (this.watermark.getValue()) {
                    Command.sendChatMessage(message);
                }
                else {
                    Command.sendRawChatMessage(message);
                }
            }
        }
    }

    private void renderNametag(final LogoutPos spot, final Vec3 renderPos) {
        final String name = spot.name + " " + (this.coords.getValue()
            ? "\u00a77(" + (int)spot.posX + ", " + (int)spot.posY + ", " + (int)spot.posZ + ")"
            : "\u00a77" + Math.round(LogoutSpots.mc.player.position().distanceTo(spot.getTopVec())));
        final float distance = (float)LogoutSpots.mc.player.position().distanceTo(spot.getTopVec());
        final float scale = ((distance / 5.0f <= 2.0f) ? 2.0f : (distance / 5.0f * 1.5075471f)) * 2.5f * 0.005075472f;
        final double yOffset = 2.5 + ((distance / 5.0f > 2.0f) ? (distance / 12.0f - 0.7) : 0.0);
        final double x = spot.posX - renderPos.x;
        final double y = spot.posY - renderPos.y;
        final double z = spot.posZ - renderPos.z;
        final PoseStack poseStack = new PoseStack();
        poseStack.translate(x, y + yOffset, z);
        poseStack.mulPose(LogoutSpots.mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-scale, -scale, scale);
        int width;
        if (this.cf.getValue()) {
            width = Xulu.cFontRenderer.getStringWidth(name) / 2 + 1;
        }
        else {
            width = LogoutSpots.mc.font.width(name) / 2;
        }
        final int textColor = new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue()).getRGB();
        if (this.cf.getValue()) {
            RenderSystem.getModelViewStack().pushPose();
            RenderSystem.getModelViewStack().mulPoseMatrix(poseStack.last().pose());
            RenderSystem.applyModelViewMatrix();
            if (this.box.getValue()) {
                XuluTessellator.drawRectDouble(-width - 2, 10, width + 1, 20, 0x78000000);
            }
            Xulu.cFontRenderer.drawStringWithShadow(name, -width, 10.0, textColor);
            RenderSystem.getModelViewStack().popPose();
            RenderSystem.applyModelViewMatrix();
        }
        else {
            if (this.box.getValue()) {
                RenderSystem.getModelViewStack().pushPose();
                RenderSystem.getModelViewStack().mulPoseMatrix(poseStack.last().pose());
                RenderSystem.applyModelViewMatrix();
                XuluTessellator.drawRectDouble(-width - 2, 10, width + 1, 20, 0x78000000);
                RenderSystem.getModelViewStack().popPose();
                RenderSystem.applyModelViewMatrix();
            }
            final MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            LogoutSpots.mc.font.drawInBatch(name, (float)(-width), 11.0f, textColor, true, poseStack.last().pose(), buffer, false, 0, 15728880);
            buffer.endBatch();
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public void onWorldRender(final RenderEvent event) {
        if (!this.render.getValue() || LogoutSpots.mc.player == null) {
            return;
        }
        final Vec3 renderPos = event.getRenderPos();
        synchronized (this.spots) {
            for (final LogoutPos spot : this.spots) {
                final AABB bb = new AABB(
                    spot.bb.minX - 0.05 - renderPos.x,
                    spot.bb.minY - renderPos.y,
                    spot.bb.minZ - 0.05 - renderPos.z,
                    spot.bb.maxX + 0.05 - renderPos.x,
                    spot.bb.maxY + 0.15 - renderPos.y,
                    spot.bb.maxZ + 0.05 - renderPos.z
                );
                XuluTessellator.drawBoundingBox(bb, 1.5f, this.red.getValue(), this.green.getValue(), this.blue.getValue(), 255);
                this.renderNametag(spot, renderPos);
            }
        }
    }

    @EventTarget
    public void onPlayerUpdate(final LocalPlayerUpdateEvent event) {
        if (this.max_distance.getValue() > 0 && LogoutSpots.mc.player != null) {
            synchronized (this.spots) {
                this.spots.removeIf(pos -> LogoutSpots.mc.player.position().distanceTo(pos.getTopVec()) > this.max_distance.getValue());
            }
        }
    }

    @SubscribeEvent
    public void onWorldUnload(final WorldEvent.Unload event) {
        this.reset();
    }

    @SubscribeEvent
    public void onWorldLoad(final WorldEvent.Load event) {
        this.reset();
    }

    public class LogoutPos
    {
        final UUID id;
        final String name;
        final AABB bb;
        final Player player;
        final double posX;
        final double posY;
        final double posZ;
        final double lastTickPosX;
        final double lastTickPosY;
        final double lastTickPosZ;

        private LogoutPos(final UUID uuid, final String name, final AABB bb, final Player player) {
            this.id = uuid;
            this.name = name;
            this.bb = bb;
            this.player = player;
            this.posX = player.getX();
            this.posY = player.getY();
            this.posZ = player.getZ();
            this.lastTickPosX = player.xOld;
            this.lastTickPosY = player.yOld;
            this.lastTickPosZ = player.zOld;
        }

        public UUID getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public Vec3 getTopVec() {
            return new Vec3((this.bb.minX + this.bb.maxX) / 2.0, this.bb.maxY, (this.bb.minZ + this.bb.maxZ) / 2.0);
        }

        @Override
        public boolean equals(final Object other) {
            return this == other || (other instanceof LogoutSpots.LogoutPos && this.getId().equals(((LogoutSpots.LogoutPos)other).getId()));
        }

        @Override
        public int hashCode() {
            return this.getId().hashCode();
        }
    }
}
