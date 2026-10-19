package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.module.core.TitleScreenShader;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraftforge.internal.BrandingControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(TitleScreen.class)
public class TitleScreenMixin
{
    @Inject(method = "init", at = @At("HEAD"))
    private void xulu$resetRandomShader(final CallbackInfo ci) {
        Xulu.randomShader = null;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/internal/BrandingControl;forEachLine(ZZLjava/util/function/BiConsumer;)V", remap = false))
    private void xulu$shiftForgeBranding(final boolean showGpu, final boolean showVersion, final BiConsumer<Integer, String> consumer) {
        BrandingControl.forEachLine(showGpu, showVersion, (lineIndex, line) -> consumer.accept(lineIndex + 1, line));
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PanoramaRenderer;render(FF)V"))
    private void xulu$panoramaOrShader(final PanoramaRenderer panorama, final float partialTicks, final float alpha) {
        if (!TitleScreenShader.renderBackground()) {
            panorama.render(partialTicks, alpha);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIFFIIII)V", ordinal = 0))
    private void xulu$skipOverlay(final PoseStack poseStack, final int x, final int y, final int width, final int height, final float uOffset, final float vOffset, final int uWidth, final int vHeight, final int textureWidth, final int textureHeight) {
        final TitleScreenShader module = Xulu.MODULE_MANAGER.getModuleT(TitleScreenShader.class);
        if (module != null && module.isToggled()) {
            return;
        }
        GuiComponent.blit(poseStack, x, y, width, height, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight);
    }
}
