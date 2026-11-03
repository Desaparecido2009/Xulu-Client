package com.elementars.eclient.util;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class OutlineUtils
{
    private static boolean stencilRequested;

    public static void renderOne(final float width) {
        checkSetupFBO();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(width);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glClearStencil(15);
        GL11.glStencilFunc(GL11.GL_NEVER, 1, 15);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
    }

    public static void renderTwo() {
        GL11.glStencilFunc(GL11.GL_NEVER, 0, 15);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
    }

    public static void renderThree() {
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 15);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
    }

    public static void renderFour(final Color c) {
        setColor(c);
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_LINE);
        GL11.glPolygonOffset(1.0f, -2000000.0f);
    }

    public static void renderFive() {
        GL11.glPolygonOffset(1.0f, 2000000.0f);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_LINE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.lineWidth(1.0f);
    }

    public static void setColor(final Color c) {
        RenderSystem.setShaderColor(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, c.getAlpha() / 255.0f);
    }

    public static void checkSetupFBO() {
        final RenderTarget fbo = Minecraft.getInstance().getMainRenderTarget();
        if (fbo == null) {
            return;
        }
        if (fbo.isStencilEnabled()) {
            stencilRequested = false;
            return;
        }
        if (stencilRequested) {
            return;
        }
        stencilRequested = true;
        Minecraft.getInstance().tell(fbo::enableStencil);
    }
}
