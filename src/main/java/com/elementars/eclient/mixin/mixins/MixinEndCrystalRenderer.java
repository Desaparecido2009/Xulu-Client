package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.module.render.Chams;
import com.elementars.eclient.util.ChamsRenderTypes;
import com.elementars.eclient.util.DualVertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

@Mixin(net.minecraft.client.renderer.entity.EndCrystalRenderer.class)
public class MixinEndCrystalRenderer
{
    @Unique
    private static final String XULU$RENDER_CRYSTAL = "render(Lnet/minecraft/world/entity/boss/enderdragon/EndCrystal;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V";

    @Redirect(method = XULU$RENDER_CRYSTAL, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private VertexConsumer xulu$crystalConsumer(final MultiBufferSource bufferSource, final RenderType renderType) {
        final Chams chams = Xulu.MODULE_MANAGER.getModuleT(Chams.class);
        if (chams != null && chams.isToggled() && Chams.crystals.getValue() && Chams.mode.getValue().equalsIgnoreCase("ESP")) {
            return bufferSource.getBuffer(ChamsRenderTypes.SOLID_NO_DEPTH);
        }
        if (chams != null && chams.isToggled() && Chams.crystals.getValue() && Chams.mode.getValue().equalsIgnoreCase("Walls")) {
            return new DualVertexConsumer(bufferSource.getBuffer(ChamsRenderTypes.SOLID_NO_DEPTH), chams.Wr.getValue(), chams.Wg.getValue(), chams.Wb.getValue(), 255, bufferSource.getBuffer(ChamsRenderTypes.SOLID_DEPTH), chams.Vr.getValue(), chams.Vg.getValue(), chams.Vb.getValue(), 255);
        }
        return bufferSource.getBuffer(renderType);
    }

    @Redirect(method = XULU$RENDER_CRYSTAL, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private void xulu$renderCrystalPart(final ModelPart modelPart, final PoseStack poseStack, final VertexConsumer vertexConsumer, final int packedLight, final int packedOverlay) {
        final Chams chams = Xulu.MODULE_MANAGER.getModuleT(Chams.class);
        if (chams == null || !chams.isToggled() || !Chams.crystals.getValue() || !(Chams.mode.getValue().equalsIgnoreCase("ESP") || Chams.mode.getValue().equalsIgnoreCase("Walls"))) {
            modelPart.render(poseStack, vertexConsumer, packedLight, packedOverlay);
            return;
        }
        if (Chams.mode.getValue().equalsIgnoreCase("ESP")) {
            final Color c = chams.rainbow.getValue() ? new Color(Xulu.rgb) : new Color(chams.r.getValue(), chams.g.getValue(), chams.b.getValue());
            modelPart.render(poseStack, vertexConsumer, 0xF000F0, packedOverlay, c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, chams.a.getValue() / 255.0f);
        }
        else {
            modelPart.render(poseStack, vertexConsumer, 0xF000F0, packedOverlay);
        }
    }

    @Inject(method = XULU$RENDER_CRYSTAL, at = @At("RETURN"))
    private void xulu$flushCrystalPasses(final EndCrystal entity, final float entityYaw, final float partialTicks, final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight, final CallbackInfo ci) {
        final Chams chams = Xulu.MODULE_MANAGER.getModuleT(Chams.class);
        if (chams == null || !chams.isToggled() || !Chams.crystals.getValue()) {
            return;
        }
        if (Chams.mode.getValue().equalsIgnoreCase("ESP")) {
            ChamsRenderTypes.beginLines(chams.lines.getValue(), chams.width.getValue());
            ChamsRenderTypes.endBatch(bufferSource, ChamsRenderTypes.SOLID_NO_DEPTH);
            ChamsRenderTypes.endLines(chams.lines.getValue());
        }
        else if (Chams.mode.getValue().equalsIgnoreCase("Walls")) {
            ChamsRenderTypes.endBatch(bufferSource, ChamsRenderTypes.SOLID_NO_DEPTH);
            ChamsRenderTypes.endBatch(bufferSource, ChamsRenderTypes.SOLID_DEPTH);
        }
    }
}
