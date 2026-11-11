package com.elementars.eclient.util;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class DualVertexConsumer implements VertexConsumer
{
    private final VertexConsumer first;
    private final int[] firstColor;
    private final VertexConsumer second;
    private final int[] secondColor;

    public DualVertexConsumer(final VertexConsumer first, final int firstRed, final int firstGreen, final int firstBlue, final int firstAlpha, final VertexConsumer second, final int secondRed, final int secondGreen, final int secondBlue, final int secondAlpha) {
        this.first = first;
        this.firstColor = new int[] { firstRed, firstGreen, firstBlue, firstAlpha };
        this.second = second;
        this.secondColor = new int[] { secondRed, secondGreen, secondBlue, secondAlpha };
    }

    @Override
    public VertexConsumer vertex(final double x, final double y, final double z) {
        this.first.vertex(x, y, z);
        this.second.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(final int red, final int green, final int blue, final int alpha) {
        this.first.color(this.firstColor[0], this.firstColor[1], this.firstColor[2], this.firstColor[3]);
        this.second.color(this.secondColor[0], this.secondColor[1], this.secondColor[2], this.secondColor[3]);
        return this;
    }

    @Override
    public VertexConsumer uv(final float u, final float v) {
        this.first.uv(u, v);
        this.second.uv(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(final int u, final int v) {
        this.first.overlayCoords(u, v);
        this.second.overlayCoords(u, v);
        return this;
    }

    @Override
    public VertexConsumer uv2(final int u, final int v) {
        this.first.uv2(u, v);
        this.second.uv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(final float x, final float y, final float z) {
        this.first.normal(x, y, z);
        this.second.normal(x, y, z);
        return this;
    }

    @Override
    public void endVertex() {
        this.first.endVertex();
        this.second.endVertex();
    }

    @Override
    public void defaultColor(final int red, final int green, final int blue, final int alpha) {
        this.first.defaultColor(red, green, blue, alpha);
        this.second.defaultColor(red, green, blue, alpha);
    }

    @Override
    public void unsetDefaultColor() {
        this.first.unsetDefaultColor();
        this.second.unsetDefaultColor();
    }
}
