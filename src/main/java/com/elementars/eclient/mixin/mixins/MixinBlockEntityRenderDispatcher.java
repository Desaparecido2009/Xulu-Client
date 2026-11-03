package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.module.render.StorageESP;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher
{
    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", at = @At("HEAD"))
    public <E extends BlockEntity> void xulu$render(final E blockEntity, final float partialTicks, final PoseStack poseStack, final MultiBufferSource bufferSource, final CallbackInfo callback) {
        final StorageESP storageESP = StorageESP.INSTANCE;
        if (storageESP == null || !storageESP.isToggled() || !storageESP.isShaderMode()) {
            return;
        }
        if (!blockEntity.hasLevel() || !blockEntity.getType().isValid(blockEntity.getBlockState())) {
            return;
        }
        if (!storageESP.isShaderTarget(blockEntity)) {
            return;
        }
        if (!(bufferSource instanceof MultiBufferSource.BufferSource)) {
            return;
        }
        final BlockEntityRenderDispatcher dispatcher = (BlockEntityRenderDispatcher)(Object)this;
        final BlockEntityRenderer<E> renderer = dispatcher.getRenderer(blockEntity);
        if (renderer == null || dispatcher.camera == null || !renderer.shouldRender(blockEntity, dispatcher.camera.getPosition())) {
            return;
        }
        StorageESP.renderShaderOutline(blockEntity, renderer, partialTicks, poseStack, bufferSource);
    }
}
