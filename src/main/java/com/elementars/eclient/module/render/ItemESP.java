package com.elementars.eclient.module.render;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.event.events.RenderEvent;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.ColorUtils;
import com.elementars.eclient.util.EntityUtil;
import com.elementars.eclient.util.RainbowUtils;
import com.elementars.eclient.util.XuluTessellator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import dev.xulu.settings.Value;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;

public class ItemESP extends Module
{
    public final Value<String> mode;
    private final Value<Boolean> throughWalls;
    private final Value<Integer> outlinewidth;
    private final Value<Boolean> rainbow;
    private final Value<Boolean> items;
    private final Value<Boolean> xpbottles;
    private final Value<Boolean> pearls;
    private final Value<Integer> red;
    private final Value<Integer> green;
    private final Value<Integer> blue;
    private final Value<Integer> alpha;
    private final Value<Integer> alphaF;
    private final Value<Integer> pred;
    private final Value<Integer> pgreen;
    private final Value<Integer> pblue;
    private final Value<Integer> palpha;
    private final Value<RenderMode> outline;
    private final Value<Integer> width;
    public static ItemESP INSTANCE;

    public ItemESP() {
        super("ItemESP", "Highlights items", 0, Category.RENDER, true);
        this.mode = this.register(new Value<String>("Mode", this, "Box", new ArrayList<String>(Arrays.asList("Box", "Text", "Shader", "Chams"))));
        this.throughWalls = this.register(new Value<Boolean>("Through Walls", this, true)).visibleWhen(value -> this.mode.getValue().equalsIgnoreCase("Text"));
        this.outlinewidth = this.register(new Value<Integer>("Shader Width", this, 1, 1, 10));
        this.rainbow = this.register(new Value<Boolean>("Rainbow", this, false));
        this.items = this.register(new Value<Boolean>("Items", this, true));
        this.xpbottles = this.register(new Value<Boolean>("EXP Bottles", this, true));
        this.pearls = this.register(new Value<Boolean>("Pearls", this, true));
        this.red = this.register(new Value<Integer>("Red", this, 255, 0, 255));
        this.green = this.register(new Value<Integer>("Green", this, 0, 0, 255));
        this.blue = this.register(new Value<Integer>("Blue", this, 0, 0, 255));
        this.alpha = this.register(new Value<Integer>("Alpha", this, 80, 0, 255));
        this.alphaF = this.register(new Value<Integer>("Full Alpha", this, 80, 0, 255));
        this.pred = this.register(new Value<Integer>("Pearl Red", this, 255, 0, 255));
        this.pgreen = this.register(new Value<Integer>("Pearl Green", this, 255, 0, 255));
        this.pblue = this.register(new Value<Integer>("Pearl Blue", this, 255, 0, 255));
        this.palpha = this.register(new Value<Integer>("Pearl Alpha", this, 255, 0, 255));
        this.outline = this.register(new Value<RenderMode>("Render Mode", this, RenderMode.SOLID, RenderMode.values()));
        this.width = this.register(new Value<Integer>("Width", this, 1, 1, 10));
        ItemESP.INSTANCE = this;
    }

    public static boolean shouldChams(final Entity entity) {
        return ItemESP.INSTANCE != null && ItemESP.INSTANCE.isToggled()
            && ItemESP.INSTANCE.mode.getValue().equalsIgnoreCase("Chams")
            && ItemESP.INSTANCE.shouldRender(entity);
    }

    private boolean shouldRender(final Entity entity) {
        return (entity instanceof ItemEntity && this.items.getValue())
            || (entity instanceof ThrownExperienceBottle && this.xpbottles.getValue())
            || (entity instanceof ThrownEnderpearl && this.pearls.getValue());
    }

    private static boolean isTracked(final Entity entity) {
        return entity instanceof ItemEntity || entity instanceof ThrownExperienceBottle || entity instanceof ThrownEnderpearl;
    }

    @Override
    public void onDisable() {
        if (ItemESP.mc.level == null) {
            return;
        }
        for (final Entity entity : ItemESP.mc.level.entitiesForRendering()) {
            if (isTracked(entity)) {
                entity.setGlowingTag(false);
            }
        }
    }

    @Override
    public void onUpdate() {
        if (ItemESP.mc.level == null || ItemESP.mc.player == null) {
            return;
        }
        final boolean shader = this.mode.getValue().equalsIgnoreCase("Shader");
        for (final Entity entity : ItemESP.mc.level.entitiesForRendering()) {
            if (isTracked(entity)) {
                entity.setGlowingTag(shader && this.shouldRender(entity));
            }
        }
    }

    @Override
    public void onWorldRender(final RenderEvent event) {
        if (ItemESP.mc.level == null || ItemESP.mc.player == null) {
            return;
        }
        final float partialTicks = ItemESP.mc.getFrameTime();
        final Vec3 renderPos = event.getRenderPos();
        for (final Entity entity : ItemESP.mc.level.entitiesForRendering()) {
            if (!this.shouldRender(entity)) {
                continue;
            }
            if (this.mode.getValue().equalsIgnoreCase("Text")) {
                this.drawText(entity, renderPos, partialTicks);
            }
            else if (this.mode.getValue().equalsIgnoreCase("Box")) {
                this.drawBox(entity, renderPos, partialTicks);
            }
            else if (this.mode.getValue().equalsIgnoreCase("Shader")) {
                this.drawShaderOutline(entity, renderPos, partialTicks);
            }
            else if (this.mode.getValue().equalsIgnoreCase("Chams")
                && (entity instanceof ThrownEnderpearl || entity instanceof ThrownExperienceBottle)) {
                this.drawThrownChams(entity, renderPos, partialTicks);
            }
        }
    }

    private void drawThrownChams(final Entity entity, final Vec3 renderPos, final float partialTicks) {
        final ItemStack stack = entity instanceof ThrownEnderpearl
            ? ((ThrownEnderpearl)entity).getItem()
            : ((ThrownExperienceBottle)entity).getItem();
        if (stack.isEmpty()) {
            return;
        }
        final Vec3 interp = EntityUtil.getInterpolatedPos(entity, partialTicks).subtract(renderPos);
        final PoseStack poseStack = new PoseStack();
        poseStack.translate(interp.x, interp.y + entity.getBbHeight() / 2.0, interp.z);
        poseStack.mulPose(ItemESP.mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f));
        final MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        ItemESP.mc.getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.GROUND, 15728880, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.getId());
        buffer.endBatch();
    }

    private int[] getColor(final Entity entity) {
        if (this.rainbow.getValue()) {
            return new int[] { RainbowUtils.r, RainbowUtils.g, RainbowUtils.b };
        }
        if (entity instanceof ThrownEnderpearl) {
            return new int[] { this.pred.getValue(), this.pgreen.getValue(), this.pblue.getValue() };
        }
        return new int[] { this.red.getValue(), this.green.getValue(), this.blue.getValue() };
    }

    private int getAlpha(final Entity entity) {
        return entity instanceof ThrownEnderpearl ? this.palpha.getValue() : this.alpha.getValue();
    }

    private AABB getBox(final Entity entity, final Vec3 renderPos, final float partialTicks) {
        final Vec3 interp = EntityUtil.getInterpolatedPos(entity, partialTicks);
        final double x = interp.x - renderPos.x;
        final double y = interp.y - renderPos.y;
        final double z = interp.z - renderPos.z;
        final AABB entityBox = entity.getBoundingBox();
        return new AABB(
            entityBox.minX - entity.getX() + x - 0.05,
            entityBox.minY - entity.getY() + y,
            entityBox.minZ - entity.getZ() + z - 0.05,
            entityBox.maxX - entity.getX() + x + 0.05,
            entityBox.maxY - entity.getY() + y + 0.15,
            entityBox.maxZ - entity.getZ() + z + 0.05
        );
    }

    private void drawBox(final Entity entity, final Vec3 renderPos, final float partialTicks) {
        final AABB box = this.getBox(entity, renderPos, partialTicks);
        final int[] color = this.getColor(entity);
        final int alpha = this.getAlpha(entity);
        switch (this.outline.getValue()) {
            case SOLID:
                XuluTessellator.drawBox2(box, color[0], color[1], color[2], alpha, 63);
                break;
            case OUTLINE:
                XuluTessellator.drawBoundingBox(box, this.width.getValue(), color[0], color[1], color[2], alpha);
                break;
            case FULL:
                XuluTessellator.drawBox2(box, color[0], color[1], color[2], alpha, 63);
                XuluTessellator.drawBoundingBox(box, this.width.getValue(), color[0], color[1], color[2], this.alphaF.getValue());
                break;
        }
    }

    private void drawShaderOutline(final Entity entity, final Vec3 renderPos, final float partialTicks) {
        final AABB box = this.getBox(entity, renderPos, partialTicks);
        final int[] color = this.getColor(entity);
        XuluTessellator.drawBoundingBox(box, this.outlinewidth.getValue(), color[0], color[1], color[2], this.getAlpha(entity));
    }

    private void drawText(final Entity entity, final Vec3 renderPos, final float partialTicks) {
        final String name = entity instanceof ItemEntity
            ? ((ItemEntity)entity).getItem().getHoverName().getString()
            : (entity instanceof ThrownEnderpearl ? "Thrown Ender Pearl" : "Thrown Exp Bottle");
        final String text = name + (entity instanceof ItemEntity ? " x" + ((ItemEntity)entity).getItem().getCount() : "");
        final Vec3 interp = EntityUtil.getInterpolatedPos(entity, partialTicks).subtract(renderPos);
        final double yAdd = entity.getBbHeight() / 2.0 + 0.5;
        final float distance = ItemESP.mc.player.distanceTo(entity);
        final float scale = Math.max(0.1f, distance / 8.0f * 1.2589254f) * 0.025f;
        final PoseStack poseStack = new PoseStack();
        poseStack.translate(interp.x, interp.y + yAdd, interp.z);
        poseStack.mulPose(ItemESP.mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-scale, -scale, scale);
        final boolean depth = !this.throughWalls.getValue();
        if (depth) {
            RenderSystem.enableDepthTest();
        }
        if (Xulu.CustomFont) {
            RenderSystem.getModelViewStack().pushPose();
            RenderSystem.getModelViewStack().mulPoseMatrix(poseStack.last().pose());
            RenderSystem.applyModelViewMatrix();
            Xulu.cFontRenderer.drawStringWithShadow(text, -Xulu.cFontRenderer.getStringWidth(text) / 2.0, 9.0, ColorUtils.Colors.WHITE);
            RenderSystem.getModelViewStack().popPose();
            RenderSystem.applyModelViewMatrix();
        }
        else {
            final Matrix4f matrix = poseStack.last().pose();
            final MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            ItemESP.mc.font.drawInBatch(text, -ItemESP.mc.font.width(text) / 2.0f, 9.0f, ColorUtils.Colors.WHITE, true, matrix, buffer, false, 0, 15728880);
            buffer.endBatch();
        }
        if (depth) {
            RenderSystem.disableDepthTest();
        }
    }

    private enum RenderMode
    {
        SOLID,
        OUTLINE,
        FULL;
    }
}
