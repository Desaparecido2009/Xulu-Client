package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.event.Event;
import com.elementars.eclient.event.events.EventModelPlayerRender;
import com.elementars.eclient.event.events.EventModelRender;
import com.elementars.eclient.event.events.EventPostRenderLayers;
import com.elementars.eclient.module.render.Chams;
import com.elementars.eclient.util.ChamsRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LivingEntityRenderer.class, priority = 9999)
public class MixinLivingEntityRenderer
{
    @Unique
    private static final String XULU$RENDER = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V";

    @Unique
    private LivingEntity xulu$renderedEntity;

    @Unique
    private MultiBufferSource xulu$bufferSource;

    @Inject(method = XULU$RENDER, at = @At("HEAD"))
    private void xulu$captureRenderContext(final LivingEntity entity, final float entityYaw, final float partialTicks, final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight, final CallbackInfo ci) {
        this.xulu$renderedEntity = entity;
        this.xulu$bufferSource = bufferSource;
    }

    @Redirect(method = XULU$RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    private void xulu$renderToBuffer(final EntityModel<?> model, final PoseStack poseStack, final VertexConsumer vertexConsumer, final int packedLight, final int packedOverlay, final float red, final float green, final float blue, final float alpha) {
        final EventModelRender modelRenderPre = new EventModelRender(model, this.xulu$renderedEntity, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0625f, poseStack, this.xulu$bufferSource, packedLight, packedOverlay);
        modelRenderPre.setState(Event.State.PRE);
        modelRenderPre.call();
        if (modelRenderPre.isCancelled()) {
            return;
        }
        EventModelPlayerRender playerRenderPre = null;
        if (model instanceof PlayerModel) {
            playerRenderPre = new EventModelPlayerRender(model, this.xulu$renderedEntity, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0625f, poseStack, this.xulu$bufferSource, packedLight, packedOverlay);
            playerRenderPre.setState(Event.State.PRE);
            playerRenderPre.call();
        }
        if (playerRenderPre == null || !playerRenderPre.isCancelled()) {
            model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
            this.xulu$normalModePass(model, poseStack, packedLight, packedOverlay);
            if (playerRenderPre != null) {
                final EventModelPlayerRender playerRenderPost = new EventModelPlayerRender(model, this.xulu$renderedEntity, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0625f, poseStack, this.xulu$bufferSource, packedLight, packedOverlay);
                playerRenderPost.setState(Event.State.POST);
                playerRenderPost.call();
            }
        }
        final EventModelRender modelRenderPost = new EventModelRender(model, this.xulu$renderedEntity, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0625f, poseStack, this.xulu$bufferSource, packedLight, packedOverlay);
        modelRenderPost.setState(Event.State.POST);
        modelRenderPost.call();
    }

    @Unique
    private void xulu$normalModePass(final EntityModel<?> model, final PoseStack poseStack, final int packedLight, final int packedOverlay) {
        final Chams chams = Xulu.MODULE_MANAGER.getModuleT(Chams.class);
        if (chams == null || !chams.isToggled() || !Chams.mode.getValue().equalsIgnoreCase("Normal") || this.xulu$renderedEntity == null) {
            return;
        }
        if (!Chams.renderChams(this.xulu$renderedEntity)) {
            return;
        }
        final ResourceLocation texture = ((LivingEntityRenderer)(Object)this).getTextureLocation(this.xulu$renderedEntity);
        if (texture == null) {
            return;
        }
        final RenderType type = ChamsRenderTypes.textured(texture);
        final VertexConsumer consumer = this.xulu$bufferSource.getBuffer(type);
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(1.0f, -1000000.0f);
        model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
        ChamsRenderTypes.endBatch(this.xulu$bufferSource, type);
        GL11.glPolygonOffset(1.0f, 1000000.0f);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
    }

    @Inject(method = XULU$RENDER, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void xulu$postRenderLayers(final LivingEntity entity, final float entityYaw, final float partialTicks, final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight, final CallbackInfo ci) {
        final LivingEntityRenderer<?, ?> renderer = (LivingEntityRenderer<?, ?>)(Object)this;
        final EventPostRenderLayers event = new EventPostRenderLayers(renderer, renderer.getModel(), entity, 0.0f, 0.0f, partialTicks, 0.0f, 0.0f, 0.0f, 0.0625f, poseStack, bufferSource, packedLight);
        event.call();
    }
}
