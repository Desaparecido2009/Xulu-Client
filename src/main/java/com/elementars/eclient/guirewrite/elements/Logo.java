package com.elementars.eclient.guirewrite.elements;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.guirewrite.Element;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xulu.newgui.util.ColorUtil;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;

public class Logo extends Element
{
    private ResourceLocation logo;
    private int textureWidth;
    private int textureHeight;
    private boolean loadAttempted;

    public Logo() {
        super("Logo");
    }

    @Override
    public void onEnable() {
        this.width = 32.0;
        this.height = 32.0;
        super.onEnable();
    }

    @Override
    public void onRender() {
        this.ensureLogo();
        if (this.logo != null) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShaderTexture(0, this.logo);
            GuiComponent.blit(new PoseStack(), (int)this.x + 4, (int)this.y + 4, (int)this.width - 8, (int)this.height - 8, 0.0f, 0.0f, this.textureWidth, this.textureHeight, this.textureWidth, this.textureHeight);
            return;
        }
        final String text = "XULU";
        final int color = ColorUtil.getClickGUIColor().getRGB();
        if (Xulu.CustomFont) {
            final float textX = (float)(this.x + (this.width - Xulu.cFontRenderer.getStringWidth(text)) / 2.0);
            final float textY = (float)(this.y + (this.height - Xulu.cFontRenderer.getHeight()) / 2.0);
            Xulu.cFontRenderer.drawStringWithShadow(text, textX, textY, color);
        }
        else {
            final float textX = (float)(this.x + (this.width - Logo.fontRenderer.width(text)) / 2.0);
            final float textY = (float)(this.y + (this.height - Logo.fontRenderer.lineHeight) / 2.0);
            Logo.fontRenderer.drawShadow(new PoseStack(), text, textX, textY, color);
        }
    }

    private void ensureLogo() {
        if (this.loadAttempted) {
            return;
        }
        this.loadAttempted = true;
        final InputStream stream = Logo.class.getResourceAsStream("/images/xulutransparent.png");
        if (stream == null) {
            return;
        }
        try (InputStream input = stream) {
            final NativeImage image = NativeImage.read(input);
            if (image == null) {
                return;
            }
            this.textureWidth = image.getWidth();
            this.textureHeight = image.getHeight();
            final DynamicTexture dynamicTexture = new DynamicTexture(image);
            this.logo = Logo.mc.getTextureManager().register("xulu_logo_transparent", dynamicTexture);
        }
        catch (IOException | RuntimeException e) {
            this.logo = null;
            this.textureWidth = 0;
            this.textureHeight = 0;
        }
    }
}
