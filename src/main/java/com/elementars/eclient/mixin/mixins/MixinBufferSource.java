package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.module.render.EnchantColor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

@Mixin(MultiBufferSource.BufferSource.class)
public class MixinBufferSource
{
    @Unique
    private float[] xulu$previousShaderColor;

    @Inject(method = "endBatch(Lnet/minecraft/client/renderer/RenderType;)V", at = @At("HEAD"))
    private void xulu$tintGlint(final RenderType renderType, final CallbackInfo info) {
        if (!xulu$isGlint(renderType)) {
            return;
        }
        final EnchantColor module = (Xulu.MODULE_MANAGER == null) ? null : Xulu.MODULE_MANAGER.getModuleT(EnchantColor.class);
        if (module == null || !module.isToggled()) {
            return;
        }
        final Color color = EnchantColor.getColor(0L, 1.0f);
        this.xulu$previousShaderColor = RenderSystem.getShaderColor().clone();
        RenderSystem.setShaderColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, 1.0f);
    }

    @Inject(method = "endBatch(Lnet/minecraft/client/renderer/RenderType;)V", at = @At("RETURN"))
    private void xulu$restoreGlintColor(final RenderType renderType, final CallbackInfo info) {
        if (this.xulu$previousShaderColor == null) {
            return;
        }
        final float[] color = this.xulu$previousShaderColor;
        RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
        this.xulu$previousShaderColor = null;
    }

    @Unique
    private static boolean xulu$isGlint(final RenderType renderType) {
        return renderType == RenderType.glint()
                || renderType == RenderType.entityGlint()
                || renderType == RenderType.armorGlint()
                || renderType == RenderType.armorEntityGlint()
                || renderType == RenderType.glintDirect()
                || renderType == RenderType.entityGlintDirect()
                || renderType == RenderType.glintTranslucent();
    }
}
