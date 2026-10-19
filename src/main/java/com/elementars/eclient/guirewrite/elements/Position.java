package com.elementars.eclient.guirewrite.elements;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.guirewrite.Element;
import com.elementars.eclient.module.core.Global;
import com.elementars.eclient.util.ColorUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;

import java.awt.Color;

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
        float yCount = (float)this.y + 1.0f;
        final int color = ColorUtils.changeAlpha(Color.white.getRGB(), Global.hudAlpha.getValue());
        if (Global.coordinates.getValue()) {
            final String coords = Xulu.df.format(Position.mc.player.getX()) + ChatFormatting.GRAY + ", " + ChatFormatting.RESET + Xulu.df.format(Position.mc.player.getY()) + ChatFormatting.GRAY + ", " + ChatFormatting.RESET + Xulu.df.format(Position.mc.player.getZ()) + ChatFormatting.GRAY + " [" + ChatFormatting.RESET + ((Position.mc.player.level.dimension() == Level.NETHER) ? (Xulu.df.format(Position.mc.player.getX() * 8.0) + ChatFormatting.GRAY + ", " + ChatFormatting.RESET + Xulu.df.format(Position.mc.player.getY()) + ChatFormatting.GRAY + ", " + ChatFormatting.RESET + Xulu.df.format(Position.mc.player.getZ() * 8.0)) : (Xulu.df.format(Position.mc.player.getX() / 8.0) + ChatFormatting.GRAY + ", " + ChatFormatting.RESET + Xulu.df.format(Position.mc.player.getY()) + ChatFormatting.GRAY + ", " + ChatFormatting.RESET + Xulu.df.format(Position.mc.player.getZ() / 8.0))) + ChatFormatting.GRAY + "]";
            this.drawLine(coords, yCount, color);
            yCount += 10.0f;
        }
        if (Global.direction.getValue()) {
            this.drawLine(Xulu.getFacing(Position.mc.player.getDirection().getName().toUpperCase()), yCount, color);
        }
    }

    private void drawLine(final String line, final float y, final int color) {
        if (Xulu.CustomFont) {
            Xulu.cFontRenderer.drawStringWithShadow(line, this.x + 1.0, y, color);
        }
        else {
            Position.fontRenderer.drawShadow(new PoseStack(), line, (float)this.x + 1.0f, y, color);
        }
    }
}
