package com.elementars.eclient.font;

import com.elementars.eclient.font.custom.CustomFont;
import com.elementars.eclient.util.Helper;
import com.mojang.blaze3d.vertex.PoseStack;

import java.awt.Font;
import java.util.regex.Pattern;

public class CFontManager
{
    private static final Pattern COLOR_CODE_PATTERN;
    private final int[] colorCodes;
    public static CustomFont customFont;
    public static XFontRenderer xFontRenderer;
    public static RainbowTextRenderer rainbowTextRenderer;

    public CFontManager() {
        this.colorCodes = new int[] { 0, 170, 43520, 43690, 11141120, 11141290, 16755200, 11184810, 5592405, 5592575, 5635925, 5636095, 16733525, 16733695, 16777045, 16777215 };
    }

    private static void ensureFonts() {
        if (customFont == null) {
            customFont = new CustomFont(new Font("Verdana", 0, 18), true, false);
        }
        if (xFontRenderer == null) {
            xFontRenderer = new XFontRenderer(new Font("Verdana", 0, 36), true, 8);
        }
        if (rainbowTextRenderer == null) {
            rainbowTextRenderer = new RainbowTextRenderer();
        }
    }

    public float drawStringWithShadow(final String text, double x, final double y, final int color, final boolean isRainbow) {
        ensureFonts();
        if (com.elementars.eclient.module.core.CustomFont.customFontMode.getValue().equalsIgnoreCase("Normal")) {
            return customFont.drawStringWithShadow(text, x, y - com.elementars.eclient.module.core.CustomFont.fontOffset.getValue(), color);
        }
        if (com.elementars.eclient.module.core.CustomFont.customFontMode.getValue().equalsIgnoreCase("Xdolf")) {
            xFontRenderer.drawStringWithShadow(text, (int)x, (int)y - com.elementars.eclient.module.core.CustomFont.fontOffset.getValue(), color);
            return 0.0f;
        }
        if (com.elementars.eclient.module.core.CustomFont.customFontMode.getValue().equalsIgnoreCase("Rainbow")) {
            int rgb = isRainbow ? color : -1;
            int displayColor = 0;
            final char[] characters = text.toCharArray();
            final String[] parts = CFontManager.COLOR_CODE_PATTERN.split(text);
            int index = 0;
            for (final String s : parts) {
                final String[] parts2 = s.split("");
                for (final String s2 : parts2) {
                    if (displayColor == 0) {
                        rgb = rainbowTextRenderer.drawStringWithShadow(s2, (float)x, (float)y, rgb);
                    }
                    else {
                        rgb = rainbowTextRenderer.updateRainbow(rgb);
                        Helper.fontRenderer.drawShadow(new PoseStack(), s2, (float)x, (float)y, displayColor);
                    }
                    try {
                        x += rainbowTextRenderer.getCharWidth(s2.charAt(0));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    ++index;
                }
                if (index < characters.length) {
                    final char colorCode = characters[index];
                    if (colorCode == '\u00a7') {
                        final char colorChar = characters[index + 1];
                        final int codeIndex = "0123456789abcdef".indexOf(colorChar);
                        if (codeIndex < 0) {
                            if (colorChar == 'r') {
                                displayColor = 0;
                            }
                        }
                        else {
                            displayColor = this.colorCodes[codeIndex];
                        }
                        index += 2;
                    }
                }
            }
            return 0.0f;
        }
        return 0.0f;
    }

    public float drawStringWithShadow(final String text, final double x, final double y, final int color) {
        return this.drawStringWithShadow(text, x, y, color, false);
    }

    public float drawString(final String text, final float x, final float y, final int color) {
        ensureFonts();
        if (com.elementars.eclient.module.core.CustomFont.customFontMode.getValue().equalsIgnoreCase("Normal")) {
            return customFont.drawString(text, x, y - com.elementars.eclient.module.core.CustomFont.fontOffset.getValue(), color);
        }
        xFontRenderer.drawStringWithShadow(text, (int)x, (int)y - com.elementars.eclient.module.core.CustomFont.fontOffset.getValue(), color);
        return 0.0f;
    }

    public float drawCenteredStringWithShadow(final String text, final float x, final float y, final int color) {
        ensureFonts();
        if (com.elementars.eclient.module.core.CustomFont.customFontMode.getValue().equalsIgnoreCase("Normal")) {
            return customFont.drawCenteredStringWithShadow(text, x, y, color);
        }
        xFontRenderer.drawCenteredString(text, (int)x, (int)y, color, true);
        return 0.0f;
    }

    public float drawCenteredString(final String text, final float x, final float y, final int color) {
        ensureFonts();
        if (com.elementars.eclient.module.core.CustomFont.customFontMode.getValue().equalsIgnoreCase("Normal")) {
            return customFont.drawCenteredString(text, x, y, color);
        }
        xFontRenderer.drawCenteredString(text, (int)x, (int)y, color);
        return 0.0f;
    }

    public float drawString(final String text, final double x, final double y, final int color, final boolean shadow) {
        ensureFonts();
        if (com.elementars.eclient.module.core.CustomFont.customFontMode.getValue().equalsIgnoreCase("Normal")) {
            return customFont.drawString(text, x, y, color, shadow);
        }
        if (shadow) {
            xFontRenderer.drawStringWithShadow(text, (int)x, (int)y, color);
        }
        else {
            xFontRenderer.drawString(text, (float)x, (float)y, color);
        }
        return 0.0f;
    }

    public float getHeight() {
        ensureFonts();
        if (com.elementars.eclient.module.core.CustomFont.customFontMode.getValue().equalsIgnoreCase("Normal")) {
            return (float)customFont.getHeight();
        }
        if (com.elementars.eclient.module.core.CustomFont.customFontMode.getValue().equalsIgnoreCase("Rainbow")) {
            return (float)rainbowTextRenderer.getHeight();
        }
        return (float)xFontRenderer.getHeight();
    }

    public int getStringWidth(final String text) {
        ensureFonts();
        if (com.elementars.eclient.module.core.CustomFont.customFontMode.getValue().equalsIgnoreCase("Normal")) {
            return customFont.getStringWidth(text);
        }
        if (com.elementars.eclient.module.core.CustomFont.customFontMode.getValue().equalsIgnoreCase("Rainbow")) {
            return rainbowTextRenderer.getStringWidth(text);
        }
        if (com.elementars.eclient.module.core.CustomFont.customFontMode.getValue().equalsIgnoreCase("Xdolf")) {
            return xFontRenderer.getStringWidth(text);
        }
        return 0;
    }

    static {
        COLOR_CODE_PATTERN = Pattern.compile("\u00a7[0123456789abcdefklmnor]");
    }
}
