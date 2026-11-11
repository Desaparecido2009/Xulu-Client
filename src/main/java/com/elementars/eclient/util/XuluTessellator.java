package com.elementars.eclient.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class XuluTessellator
{
    public static XuluTessellator INSTANCE = new XuluTessellator();
    private double translationX;
    private double translationY;
    private double translationZ;

    public static void prepare(final int mode) {
        prepareGL();
    }

    public static void prepareGL() {
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void begin(final int mode) {
        getBufferBuilder().begin(modeToMode(mode), DefaultVertexFormat.POSITION_COLOR);
    }

    public static void release() {
        releaseGL();
    }

    private static void restoreWorldState() {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void render() {
        Tesselator.getInstance().end();
    }

    public static void releaseGL() {
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
    }

    public void setTranslation(final double x, final double y, final double z) {
        this.translationX = x;
        this.translationY = y;
        this.translationZ = z;
    }

    public Vec3 getTranslation() {
        return new Vec3(this.translationX, this.translationY, this.translationZ);
    }

    public static BufferBuilder getBufferBuilder() {
        return Tesselator.getInstance().getBuilder();
    }

    private static VertexFormat.Mode modeToMode(final int mode) {
        switch (mode) {
            case 1: return VertexFormat.Mode.LINES;
            case 2: return VertexFormat.Mode.LINE_STRIP;
            case 3: return VertexFormat.Mode.DEBUG_LINE_STRIP;
            case 4: return VertexFormat.Mode.TRIANGLES;
            case 5: return VertexFormat.Mode.TRIANGLE_STRIP;
            case 6: return VertexFormat.Mode.TRIANGLE_FAN;
            case 7: return VertexFormat.Mode.QUADS;
            default: return VertexFormat.Mode.QUADS;
        }
    }

    private static void beginRect() {
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        getBufferBuilder().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    }

    private static void endRect() {
        Tesselator.getInstance().end();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawRectDouble(double left, double top, double right, double bottom, final int color) {
        if (left < right) {
            final double i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            final double j = top;
            top = bottom;
            bottom = j;
        }
        final float a = (color >> 24 & 0xFF) / 255.0f;
        final float r = (color >> 16 & 0xFF) / 255.0f;
        final float g = (color >> 8 & 0xFF) / 255.0f;
        final float b = (color & 0xFF) / 255.0f;
        beginRect();
        final BufferBuilder bufferbuilder = getBufferBuilder();
        bufferbuilder.vertex(left, bottom, 0.0).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(right, bottom, 0.0).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(right, top, 0.0).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(left, top, 0.0).color(r, g, b, a).endVertex();
        endRect();
    }

    public static void drawRectGradient(double left, double top, double right, double bottom, final int color, final int color2) {
        if (left < right) {
            final double i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            final double j = top;
            top = bottom;
            bottom = j;
        }
        final float a1 = (color >> 24 & 0xFF) / 255.0f;
        final float r1 = (color >> 16 & 0xFF) / 255.0f;
        final float g1 = (color >> 8 & 0xFF) / 255.0f;
        final float b1 = (color & 0xFF) / 255.0f;
        beginRect();
        final BufferBuilder bufferbuilder = getBufferBuilder();
        bufferbuilder.vertex(left, bottom, 0.0).color(r1, g1, b1, a1).endVertex();
        bufferbuilder.vertex(right, bottom, 0.0).color(r1, g1, b1, a1).endVertex();
        bufferbuilder.vertex(right, top, 0.0).color(r1, g1, b1, a1).endVertex();
        bufferbuilder.vertex(left, top, 0.0).color(r1, g1, b1, a1).endVertex();
        endRect();
    }

    public static void drawOutlineLine(double left, double top, double right, double bottom, final float width, final int color) {
        if (left < right) {
            final double i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            final double j = top;
            top = bottom;
            bottom = j;
        }
        final float a = (color >> 24 & 0xFF) / 255.0f;
        final float r = (color >> 16 & 0xFF) / 255.0f;
        final float g = (color >> 8 & 0xFF) / 255.0f;
        final float b = (color & 0xFF) / 255.0f;
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(width);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        final BufferBuilder bufferbuilder = getBufferBuilder();
        bufferbuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(left, bottom, 0.0).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(right, bottom, 0.0).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(right, top, 0.0).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(left, top, 0.0).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(left, bottom, 0.0).color(r, g, b, a).endVertex();
        Tesselator.getInstance().end();
        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawRectOutline(final double left, final double top, final double right, final double bottom, final double width, final int color) {
        drawRectOutline(left - width, top - width, right + width, bottom + width, left, top, right, bottom, color);
    }

    public static void drawRectOutline(final double left, final double top, final double right, final double bottom, final double left2, final double top2, final double right2, final double bottom2, final int color) {
        drawRectDouble(left, top, right, top2, color);
        drawRectDouble(right2, top2, right, bottom, color);
        drawRectDouble(left, bottom2, right2, bottom, color);
        drawRectDouble(left, top2, left2, bottom2, color);
    }

    public static void drawBox(final BlockPos blockPos, final int argb, final int sides) {
        drawBox((float)(blockPos.getX() + INSTANCE.translationX), (float)(blockPos.getY() + INSTANCE.translationY), (float)(blockPos.getZ() + INSTANCE.translationZ), argb, sides);
    }

    public static void drawBox(final float x, final float y, final float z, final int argb, final int sides) {
        drawBox(x, y, z, 1.0f, 1.0f, 1.0f, argb, sides);
    }

    public static void drawBox(final float x, final float y, final float z, final float w, final float h, final float d, final int argb, final int sides) {
        final float a = (argb >> 24 & 0xFF) / 255.0f;
        final float r = (argb >> 16 & 0xFF) / 255.0f;
        final float g = (argb >> 8 & 0xFF) / 255.0f;
        final float b = (argb & 0xFF) / 255.0f;
        beginRect();
        final BufferBuilder buffer = getBufferBuilder();
        if ((sides & 0x1) != 0x0) {
            buffer.vertex(x + w, y, z).color(r, g, b, a).endVertex();
            buffer.vertex(x + w, y, z + d).color(r, g, b, a).endVertex();
            buffer.vertex(x, y, z + d).color(r, g, b, a).endVertex();
            buffer.vertex(x, y, z).color(r, g, b, a).endVertex();
        }
        if ((sides & 0x2) != 0x0) {
            buffer.vertex(x + w, y + h, z).color(r, g, b, a).endVertex();
            buffer.vertex(x, y + h, z).color(r, g, b, a).endVertex();
            buffer.vertex(x, y + h, z + d).color(r, g, b, a).endVertex();
            buffer.vertex(x + w, y + h, z + d).color(r, g, b, a).endVertex();
        }
        if ((sides & 0x4) != 0x0) {
            buffer.vertex(x + w, y, z).color(r, g, b, a).endVertex();
            buffer.vertex(x, y, z).color(r, g, b, a).endVertex();
            buffer.vertex(x, y + h, z).color(r, g, b, a).endVertex();
            buffer.vertex(x + w, y + h, z).color(r, g, b, a).endVertex();
        }
        if ((sides & 0x8) != 0x0) {
            buffer.vertex(x, y, z + d).color(r, g, b, a).endVertex();
            buffer.vertex(x + w, y, z + d).color(r, g, b, a).endVertex();
            buffer.vertex(x + w, y + h, z + d).color(r, g, b, a).endVertex();
            buffer.vertex(x, y + h, z + d).color(r, g, b, a).endVertex();
        }
        if ((sides & 0x10) != 0x0) {
            buffer.vertex(x, y, z).color(r, g, b, a).endVertex();
            buffer.vertex(x, y, z + d).color(r, g, b, a).endVertex();
            buffer.vertex(x, y + h, z + d).color(r, g, b, a).endVertex();
            buffer.vertex(x, y + h, z).color(r, g, b, a).endVertex();
        }
        if ((sides & 0x20) != 0x0) {
            buffer.vertex(x + w, y, z + d).color(r, g, b, a).endVertex();
            buffer.vertex(x + w, y, z).color(r, g, b, a).endVertex();
            buffer.vertex(x + w, y + h, z).color(r, g, b, a).endVertex();
            buffer.vertex(x + w, y + h, z + d).color(r, g, b, a).endVertex();
        }
        endRect();
    }

    public static void drawBox(final BlockPos blockPos, final int r, final int g, final int b, final int a, final int sides) {
        drawBox(blockPos, (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF), sides);
    }

    public static void drawBox2(final AABB bb, final int r, final int g, final int b, final int a, final int sides) {
        drawBox2(bb, (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF), sides);
    }

    public static void drawFullBox(final AABB bb, final BlockPos blockPos, final float width, final int argb, final int alpha2) {
        drawFullBox(bb, blockPos, width, argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, argb >> 24 & 0xFF, alpha2);
    }

    public static void drawFullBox(final AABB bb, final BlockPos blockPos, final float width, final int red, final int green, final int blue, final int alpha, final int alpha2) {
        prepare(7);
        drawBox(blockPos, red, green, blue, alpha, 63);
        release();
        drawBoundingBox(bb, width, red, green, blue, alpha2);
    }

    public static void drawFullBox2(final AABB bb, final BlockPos blockPos, final float width, final int argb) {
        final int a = argb >>> 24 & 0xFF;
        final int r = argb >>> 16 & 0xFF;
        final int g = argb >>> 8 & 0xFF;
        final int b = argb & 0xFF;
        drawFullBox2(bb, blockPos, width, r, g, b, a, 255);
    }

    public static void drawFullBox2(final AABB bb, final BlockPos blockPos, final float width, final int argb, final int alpha2) {
        final int a = argb >>> 24 & 0xFF;
        final int r = argb >>> 16 & 0xFF;
        final int g = argb >>> 8 & 0xFF;
        final int b = argb & 0xFF;
        drawFullBox2(bb, blockPos, width, r, g, b, a, alpha2);
    }

    public static void drawFullBox2(final AABB bb, final BlockPos blockPos, final float width, final int red, final int green, final int blue, final int alpha, final int alpha2) {
        prepare(7);
        drawBox2(bb, red, green, blue, alpha, 63);
        release();
        drawBoundingBox(bb, width, red, green, blue, alpha2);
    }

    public static void drawBoundingBox(final AABB bb, final float width, final int argb) {
        drawBoundingBox(bb, width, argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, argb >> 24 & 0xFF);
    }

    public static void drawBoundingBox(final AABB bb, final float width, final int red, final int green, final int blue, final int alpha) {
        final float rf = (red & 0xFF) / 255.0f;
        final float gf = (green & 0xFF) / 255.0f;
        final float bf = (blue & 0xFF) / 255.0f;
        final float af = (alpha & 0xFF) / 255.0f;
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(width);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        final BufferBuilder bufferbuilder = getBufferBuilder();
        bufferbuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(bb.minX, bb.minY, bb.minZ).color(rf, gf, bf, af).endVertex();
        bufferbuilder.vertex(bb.maxX, bb.minY, bb.minZ).color(rf, gf, bf, af).endVertex();
        bufferbuilder.vertex(bb.maxX, bb.minY, bb.maxZ).color(rf, gf, bf, af).endVertex();
        bufferbuilder.vertex(bb.minX, bb.minY, bb.maxZ).color(rf, gf, bf, af).endVertex();
        bufferbuilder.vertex(bb.minX, bb.minY, bb.minZ).color(rf, gf, bf, af).endVertex();
        bufferbuilder.vertex(bb.minX, bb.maxY, bb.minZ).color(rf, gf, bf, af).endVertex();
        bufferbuilder.vertex(bb.maxX, bb.maxY, bb.minZ).color(rf, gf, bf, af).endVertex();
        bufferbuilder.vertex(bb.maxX, bb.minY, bb.minZ).color(rf, gf, bf, af).endVertex();
        Tesselator.getInstance().end();
        bufferbuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(bb.minX, bb.maxY, bb.minZ).color(rf, gf, bf, af).endVertex();
        bufferbuilder.vertex(bb.minX, bb.maxY, bb.maxZ).color(rf, gf, bf, af).endVertex();
        bufferbuilder.vertex(bb.minX, bb.minY, bb.maxZ).color(rf, gf, bf, af).endVertex();
        Tesselator.getInstance().end();
        bufferbuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(bb.minX, bb.maxY, bb.maxZ).color(rf, gf, bf, af).endVertex();
        bufferbuilder.vertex(bb.maxX, bb.maxY, bb.maxZ).color(rf, gf, bf, af).endVertex();
        bufferbuilder.vertex(bb.maxX, bb.minY, bb.maxZ).color(rf, gf, bf, af).endVertex();
        Tesselator.getInstance().end();
        bufferbuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(bb.maxX, bb.maxY, bb.maxZ).color(rf, gf, bf, af).endVertex();
        bufferbuilder.vertex(bb.maxX, bb.maxY, bb.minZ).color(rf, gf, bf, af).endVertex();
        bufferbuilder.vertex(bb.maxX, bb.minY, bb.minZ).color(rf, gf, bf, af).endVertex();
        Tesselator.getInstance().end();
        RenderSystem.lineWidth(1.0f);
        restoreWorldState();
    }

    public static void drawIndicator(final AABB bb, final int argb, final int sides) {
        final int a = argb >>> 24 & 0xFF;
        final int r = argb >>> 16 & 0xFF;
        final int g = argb >>> 8 & 0xFF;
        final int b = argb & 0xFF;
        drawIndicator(bb, r, g, b, a, sides);
    }

    public static void drawIndicator(final AABB bb, final int r, final int g, final int b, final int a, final int sides) {
        final float rf = (r & 0xFF) / 255.0f;
        final float gf = (g & 0xFF) / 255.0f;
        final float bf = (b & 0xFF) / 255.0f;
        final float af = (a & 0xFF) / 255.0f;
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        final BufferBuilder buffer = getBufferBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        if ((sides & 0x1) != 0x0) {
            buffer.vertex(bb.maxX, bb.minY, bb.minZ).color(rf, gf, bf, af).endVertex();
            buffer.vertex(bb.maxX, bb.minY, bb.maxZ).color(rf, gf, bf, af).endVertex();
            buffer.vertex(bb.minX, bb.minY, bb.maxZ).color(rf, gf, bf, af).endVertex();
            buffer.vertex(bb.minX, bb.minY, bb.minZ).color(rf, gf, bf, af).endVertex();
        }
        if ((sides & 0x4) != 0x0) {
            buffer.vertex(bb.maxX, bb.minY, bb.minZ).color(rf, gf, bf, af).endVertex();
            buffer.vertex(bb.minX, bb.minY, bb.minZ).color(rf, gf, bf, af).endVertex();
            buffer.vertex(bb.minX, bb.maxY, bb.minZ).color(rf, gf, bf, 0.0f).endVertex();
            buffer.vertex(bb.maxX, bb.maxY, bb.minZ).color(rf, gf, bf, 0.0f).endVertex();
        }
        if ((sides & 0x8) != 0x0) {
            buffer.vertex(bb.minX, bb.minY, bb.maxZ).color(rf, gf, bf, af).endVertex();
            buffer.vertex(bb.maxX, bb.minY, bb.maxZ).color(rf, gf, bf, af).endVertex();
            buffer.vertex(bb.maxX, bb.maxY, bb.maxZ).color(rf, gf, bf, 0.0f).endVertex();
            buffer.vertex(bb.minX, bb.maxY, bb.maxZ).color(rf, gf, bf, 0.0f).endVertex();
        }
        if ((sides & 0x10) != 0x0) {
            buffer.vertex(bb.minX, bb.minY, bb.minZ).color(rf, gf, bf, af).endVertex();
            buffer.vertex(bb.minX, bb.minY, bb.maxZ).color(rf, gf, bf, af).endVertex();
            buffer.vertex(bb.minX, bb.maxY, bb.maxZ).color(rf, gf, bf, 0.0f).endVertex();
            buffer.vertex(bb.minX, bb.maxY, bb.minZ).color(rf, gf, bf, 0.0f).endVertex();
        }
        if ((sides & 0x20) != 0x0) {
            buffer.vertex(bb.maxX, bb.minY, bb.maxZ).color(rf, gf, bf, af).endVertex();
            buffer.vertex(bb.maxX, bb.minY, bb.minZ).color(rf, gf, bf, af).endVertex();
            buffer.vertex(bb.maxX, bb.maxY, bb.minZ).color(rf, gf, bf, 0.0f).endVertex();
            buffer.vertex(bb.maxX, bb.maxY, bb.maxZ).color(rf, gf, bf, 0.0f).endVertex();
        }
        Tesselator.getInstance().end();
        restoreWorldState();
    }

    public static void drawBoundingBoxFace(final AABB bb, final float width, final int red, final int green, final int blue, final int alpha) {
        final float rf = (red & 0xFF) / 255.0f;
        final float gf = (green & 0xFF) / 255.0f;
        final float bf = (blue & 0xFF) / 255.0f;
        final float af = (alpha & 0xFF) / 255.0f;
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(width);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        final BufferBuilder buffer = getBufferBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(bb.minX, bb.minY, bb.minZ).color(rf, gf, bf, af).endVertex();
        buffer.vertex(bb.minX, bb.minY, bb.maxZ).color(rf, gf, bf, af).endVertex();
        buffer.vertex(bb.maxX, bb.minY, bb.maxZ).color(rf, gf, bf, af).endVertex();
        buffer.vertex(bb.maxX, bb.minY, bb.minZ).color(rf, gf, bf, af).endVertex();
        buffer.vertex(bb.minX, bb.minY, bb.minZ).color(rf, gf, bf, af).endVertex();
        Tesselator.getInstance().end();
        RenderSystem.lineWidth(1.0f);
        restoreWorldState();
    }

    public static void drawBox2(final AABB bb, final int argb, final int sides) {
        drawBox((float)bb.minX, (float)bb.minY, (float)bb.minZ, (float)(bb.maxX - bb.minX), (float)(bb.maxY - bb.minY), (float)(bb.maxZ - bb.minZ), argb, sides);
    }

    public static void drawFullBox(final AABB bb, final BlockPos blockPos, final float width, final int alpha2) {
        drawBox2(bb, 0x33000000, 0x3F);
    }
}
