package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.module.render.ItemESP;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public class MixinItemEntityRenderer
{
    @Inject(method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void xulu$chamsBegin(final ItemEntity entity, final float entityYaw, final float partialTicks, final PoseStack poseStack, final MultiBufferSource buffer, final int packedLight, final CallbackInfo info) {
        if (ItemESP.shouldChams(entity)) {
            RenderSystem.enablePolygonOffset();
            RenderSystem.polygonOffset(1.0f, -1000000.0f);
        }
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    private void xulu$chamsEnd(final ItemEntity entity, final float entityYaw, final float partialTicks, final PoseStack poseStack, final MultiBufferSource buffer, final int packedLight, final CallbackInfo info) {
        if (ItemESP.shouldChams(entity)) {
            RenderSystem.polygonOffset(1.0f, 1000000.0f);
            RenderSystem.disablePolygonOffset();
        }
    }
}
