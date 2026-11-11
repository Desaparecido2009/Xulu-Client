package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.module.render.Chams;
import com.elementars.eclient.util.ChamsRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

@Mixin(net.minecraft.client.renderer.entity.player.PlayerRenderer.class)
public class MixinPlayerRenderer
{
    @Unique
    private static final String XULU$RENDER_HAND = "renderHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;)V";

    @Unique
    private MultiBufferSource xulu$handBufferSource;

    @Inject(method = XULU$RENDER_HAND, at = @At("HEAD"))
    private void xulu$captureHandContext(final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight, final AbstractClientPlayer player, final ModelPart arm, final ModelPart sleeve, final CallbackInfo ci) {
        this.xulu$handBufferSource = bufferSource;
    }

    @Redirect(method = XULU$RENDER_HAND, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private void xulu$renderHandPart(final ModelPart modelPart, final PoseStack poseStack, final VertexConsumer vertexConsumer, final int packedLight, final int packedOverlay) {
        final Chams chams = Xulu.MODULE_MANAGER.getModuleT(Chams.class);
        if (chams == null || !chams.isToggled() || !chams.hand.getValue() || !Chams.mode.getValue().equalsIgnoreCase("ESP")) {
            modelPart.render(poseStack, vertexConsumer, packedLight, packedOverlay);
            return;
        }
        final Color c = chams.rainbow.getValue() ? new Color(Xulu.rgb) : new Color(chams.r.getValue(), chams.g.getValue(), chams.b.getValue());
        final RenderType type = ChamsRenderTypes.SOLID_NO_DEPTH;
        final VertexConsumer consumer = this.xulu$handBufferSource.getBuffer(type);
        ChamsRenderTypes.beginLines(chams.lines.getValue(), chams.width.getValue());
        modelPart.render(poseStack, consumer, 0xF000F0, packedOverlay, c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, chams.a.getValue() / 255.0f);
        ChamsRenderTypes.endBatch(this.xulu$handBufferSource, type);
        ChamsRenderTypes.endLines(chams.lines.getValue());
    }
}
