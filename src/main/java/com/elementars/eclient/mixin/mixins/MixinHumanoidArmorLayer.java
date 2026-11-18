package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.module.render.NoRender;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ HumanoidArmorLayer.class })
public abstract class MixinHumanoidArmorLayer
{
    @Inject(method = { "renderArmorPiece" }, at = { @At("HEAD") }, cancellable = true)
    private void xulu$hideArmor(final PoseStack poseStack, final MultiBufferSource buffer, final LivingEntity entity, final EquipmentSlot slot, final int packedLight, final Model model, final CallbackInfo info) {
        final NoRender noRender = Xulu.MODULE_MANAGER.getModuleT(NoRender.class);
        if (noRender != null && noRender.isToggled() && noRender.armor.getValue()) {
            info.cancel();
        }
    }

    @Redirect(method = { "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IZLnet/minecraft/client/model/Model;FFFLnet/minecraft/resources/ResourceLocation;)V" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    private void xulu$armorTransparency(final Model model, final PoseStack poseStack, final VertexConsumer vertexConsumer, final int packedLight, final int packedOverlay, final float red, final float green, final float blue, final float alpha) {
        final NoRender noRender = Xulu.MODULE_MANAGER.getModuleT(NoRender.class);
        float finalAlpha = alpha;
        if (noRender != null && noRender.isToggled() && noRender.armorTrans.getValue()) {
            finalAlpha = noRender.alpha.getValue() / 255.0f;
        }
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, finalAlpha);
    }
}
