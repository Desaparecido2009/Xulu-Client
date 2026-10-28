package com.elementars.eclient.util;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class RenderUtils
{
    private final Map<Integer, Boolean> glCapMap;

    public RenderUtils() {
        this.glCapMap = new HashMap<Integer, Boolean>();
    }

    public void glColor(final int red, final int green, final int blue, final int alpha) {
        RenderSystem.setShaderColor(red / 255.0f, green / 255.0f, blue / 255.0f, alpha / 255.0f);
    }

    public void glColor(final Color color) {
        final float red = color.getRed() / 255.0f;
        final float green = color.getGreen() / 255.0f;
        final float blue = color.getBlue() / 255.0f;
        final float alpha = color.getAlpha() / 255.0f;
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    private void glColor(final int hex) {
        final float alpha = (hex >> 24 & 0xFF) / 255.0f;
        final float red = (hex >> 16 & 0xFF) / 255.0f;
        final float green = (hex >> 8 & 0xFF) / 255.0f;
        final float blue = (hex & 0xFF) / 255.0f;
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    public void resetCaps() {
        this.glCapMap.forEach(this::setGlState);
    }

    public void enableGlCap(final int cap) {
        this.setGlCap(cap, true);
    }

    public void enableGlCap(final int... caps) {
        for (final int cap : caps) {
            this.setGlCap(cap, true);
        }
    }

    public void disableGlCap(final int cap) {
        this.setGlCap(cap, false);
    }

    public void disableGlCap(final int... caps) {
        for (final int cap : caps) {
            this.setGlCap(cap, false);
        }
    }

    public void setGlCap(final int cap, final boolean state) {
        this.glCapMap.put(cap, isGlCapEnabled(cap));
        this.setGlState(cap, state);
    }

    public void setGlState(final int cap, final boolean state) {
        if (cap == GL11.GL_LIGHTING) {
            return;
        }
        if (cap == GL11.GL_DEPTH_TEST) {
            if (state) {
                RenderSystem.enableDepthTest();
            }
            else {
                RenderSystem.disableDepthTest();
            }
        }
        else if (cap == GL11.GL_BLEND) {
            if (state) {
                RenderSystem.enableBlend();
            }
            else {
                RenderSystem.disableBlend();
            }
        }
        else if (cap == GL11.GL_CULL_FACE) {
            if (state) {
                RenderSystem.enableCull();
            }
            else {
                RenderSystem.disableCull();
            }
        }
        else if (cap == GL11.GL_TEXTURE_2D) {
            if (state) {
                RenderSystem.enableTexture();
            }
            else {
                RenderSystem.disableTexture();
            }
        }
        else if (state) {
            GL11.glEnable(cap);
        }
        else {
            GL11.glDisable(cap);
        }
    }

    private static boolean isGlCapEnabled(final int cap) {
        if (cap == GL11.GL_LIGHTING) {
            return false;
        }
        return GL11.glIsEnabled(cap);
    }
}
