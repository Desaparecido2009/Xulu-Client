package com.elementars.eclient.guirewrite.elements;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.guirewrite.Element;
import com.elementars.eclient.module.core.Global;
import com.elementars.eclient.util.ColorUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Position extends Element
{
    public Position() {
        super("Position");
    }

    @Override
    public void onEnable() {
        this.width = 100.0;
        this.height = 22.0;
        super.onEnable();
    }

    @Override
    public void onRender() {
        if (Position.mc.player == null || Position.mc.level == null) {
            return;
        }
        final int color = ColorUtils.changeAlpha(Color.white.getRGB(), Global.hudAlpha.getValue());
        final List<String> lines = new ArrayList<String>();
        if (Global.coordinates.getValue()) {
            final double x = Position.mc.player.getX();
            final double y = Position.mc.player.getY();
            final double z = Position.mc.player.getZ();
            final String coords = this.formatCoords(x, y, z);
            final String converted;
            if (Position.mc.player.level.dimension() == Level.NETHER) {
                converted = this.formatCoords(x * 8.0, y, z * 8.0);
            }
            else if (Position.mc.player.level.dimension() == Level.OVERWORLD) {
                converted = this.formatCoords(x / 8.0, y, z / 8.0);
            }
            else {
                converted = null;
            }
            lines.add(converted == null ? coords : coords + ChatFormatting.GRAY + " [" + ChatFormatting.RESET + converted + ChatFormatting.GRAY + "]");
        }
        if (Global.direction.getValue()) {
            lines.add(this.getFacing());
        }
        if (lines.isEmpty()) {
            return;
        }
        int maxWidth = 0;
        for (final String line : lines) {
            maxWidth = Math.max(maxWidth, this.getStringWidth(line));
        }
        this.width = Math.max(this.width, maxWidth + 2.0);
        this.height = Math.max(this.height, lines.size() * 10.0 + 1.0);
        final boolean rightAligned = this.isRightHalf();
        final boolean bottomAligned = this.isBottomHalf();
        float yCount = bottomAligned ? (float)(this.y + this.height - lines.size() * 10.0 + 1.0) : (float)this.y + 1.0f;
        for (final String line : lines) {
            final double textX = rightAligned ? this.x + this.width - 1.0 - this.getStringWidth(line) : this.x + 1.0;
            this.drawLine(line, textX, yCount, color);
            yCount += 10.0f;
        }
    }

    private String formatCoords(final double x, final double y, final double z) {
        return Xulu.df.format(x) + ChatFormatting.GRAY + ", " + ChatFormatting.RESET
                + Xulu.df.format(y) + ChatFormatting.GRAY + ", " + ChatFormatting.RESET
                + Xulu.df.format(z);
    }

    private String getFacing() {
        float yaw = Mth.wrapDegrees(Position.mc.player.getYRot());
        if (yaw < 0.0f) {
            yaw += 360.0f;
        }
        final String name;
        final String axes;
        if (yaw < 22.5f || yaw >= 337.5f) {
            name = "South";
            axes = "+Z";
        }
        else if (yaw < 67.5f) {
            name = "South-West";
            axes = "-X +Z";
        }
        else if (yaw < 112.5f) {
            name = "West";
            axes = "-X";
        }
        else if (yaw < 157.5f) {
            name = "North-West";
            axes = "-X -Z";
        }
        else if (yaw < 202.5f) {
            name = "North";
            axes = "-Z";
        }
        else if (yaw < 247.5f) {
            name = "North-East";
            axes = "+X -Z";
        }
        else if (yaw < 292.5f) {
            name = "East";
            axes = "+X";
        }
        else {
            name = "South-East";
            axes = "+X +Z";
        }
        return name + " " + ChatFormatting.GRAY + "[" + ChatFormatting.RESET + axes + ChatFormatting.GRAY + "]";
    }

    private int getStringWidth(final String text) {
        return Xulu.CustomFont ? Xulu.cFontRenderer.getStringWidth(text) : Position.fontRenderer.width(text);
    }

    private void drawLine(final String line, final double x, final float y, final int color) {
        if (Xulu.CustomFont) {
            Xulu.cFontRenderer.drawStringWithShadow(line, (float)x, y, color);
        }
        else {
            Position.fontRenderer.drawShadow(new PoseStack(), line, (float)x, y, color);
        }
    }
}
