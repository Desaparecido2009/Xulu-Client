package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.module.core.Global;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Font.class)
public class MixinFontRenderer
{
    @Shadow
    private float renderText(final String text, final float x, final float y, final int color, final boolean dropShadow, final Matrix4f matrix, final MultiBufferSource buffer, final boolean transparent, final int backgroundColor, final int packedLight) {
        return 0.0f;
    }

    @Redirect(method = "drawInternal(Ljava/lang/String;FFILcom/mojang/math/Matrix4f;ZZ)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Ljava/lang/String;FFIZLcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;ZIIZ)I"))
    private int xulu$drawStringMaybeWithShadow(final Font font, final String text, final float x, final float y, final int color, final boolean dropShadow, final Matrix4f matrix, final MultiBufferSource buffer, final boolean transparent, final int backgroundColor, final int packedLight, final boolean filter) {
        return font.drawInBatch(text, x, y, color, Global.textShadow.getValue() && dropShadow, matrix, buffer, transparent, backgroundColor, packedLight, filter);
    }

    @Redirect(method = "drawInternal(Ljava/lang/String;FFIZLcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;ZIIZ)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;renderText(Ljava/lang/String;FFIZLcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;ZII)F", ordinal = 0))
    private float xulu$drawStringMore(final Font font, final String text, float x, float y, final int color, final boolean dropShadow, final Matrix4f matrix, final MultiBufferSource buffer, final boolean transparent, final int backgroundColor, final int packedLight) {
        if (Global.shortShadow != null && Global.shortShadow.getValue()) {
            x -= 0.4f;
            y -= 0.4f;
        }
        return this.renderText(text, x, y, color, dropShadow, matrix, buffer, transparent, backgroundColor, packedLight);
    }
}
