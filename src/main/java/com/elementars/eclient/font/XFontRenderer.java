package com.elementars.eclient.font;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.GameRenderer;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class XFontRenderer
{
    public final Random fontRandom;
    private final Color[] customColorCodes;
    private final int[] colorCode;
    private XFont font;
    private XFont boldFont;
    private XFont italicFont;
    private XFont boldItalicFont;
    private String colorcodeIdentifiers;
    private boolean bidi;
    public int FONT_HEIGHT;

    public XFontRenderer(final Font font, final boolean antiAlias, final int charOffset) {
        this.fontRandom = new Random();
        this.customColorCodes = new Color[256];
        this.colorCode = new int[32];
        this.colorcodeIdentifiers = "0123456789abcdefklmnor";
        this.setFont(font, antiAlias, charOffset);
        this.customColorCodes[113] = new Color(0, 90, 163);
        this.colorcodeIdentifiers = this.setupColorcodeIdentifier();
        this.setupMinecraftColorcodes();
        this.FONT_HEIGHT = this.getHeight();
    }

    public int drawString(final String s, final float x, final float y, final int color) {
        return this.drawString(s, x, y, color, false);
    }

    public void drawStringWithShadow(final String s, final int x, final int y, final int color) {
        this.drawString(s, x + 0.75f, y + 0.75f, color, true);
        this.drawString(s, (float)x, (float)y, color, false);
    }

    public void drawCenteredString(final String s, final int x, final int y, final int color, final boolean shadow) {
        if (shadow) {
            this.drawStringWithShadow(s, x - this.getStringWidth(s) / 2, y, color);
        }
        else {
            this.drawString(s, x - this.getStringWidth(s) / 2, y, color);
        }
    }

    public void drawCenteredStringXY(final String s, final int x, final int y, final int color, final boolean shadow) {
        this.drawCenteredString(s, x, y - this.getHeight() / 2, color, shadow);
    }

    public void drawCenteredString(final String s, final int x, final int y, final int color) {
        this.drawStringWithShadow(s, x - this.getStringWidth(s) / 2, y, color);
    }

    public int drawString(final String text, final float x, final float y, final int color, final boolean shadow) {
        int result = 0;
        final String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; ++i) {
            result = this.drawLine(lines[i], x, y + i * this.getHeight(), color, shadow);
        }
        return result;
    }

    private int drawLine(final String text, final float x, final float y, int color, final boolean shadow) {
        if (text == null) {
            return 0;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().translate(x - 1.5, y + 0.5, 0.0);
        RenderSystem.applyModelViewMatrix();
        if ((color & 0xFC000000) == 0x0) {
            color |= 0xFF000000;
        }
        final float red = (color >> 16 & 0xFF) / 255.0f;
        final float green = (color >> 8 & 0xFF) / 255.0f;
        final float blue = (color & 0xFF) / 255.0f;
        final float alpha = (color >> 24 & 0xFF) / 255.0f;
        final Color c = new Color(red, green, blue, alpha);
        if (text.contains("\u00a7")) {
            final String[] parts = text.split("\u00a7");
            Color currentColor = c;
            XFont currentFont = this.font;
            int width = 0;
            boolean randomCase = false;
            boolean bold = false;
            boolean italic = false;
            boolean strikethrough = false;
            boolean underline = false;
            for (int index = 0; index < parts.length; ++index) {
                if (parts[index].length() > 0) {
                    if (index == 0) {
                        currentFont.drawString(parts[index], width, 0.0, currentColor, shadow);
                        width += currentFont.getStringWidth(parts[index]);
                    }
                    else {
                        final String words = parts[index].substring(1);
                        final char type = parts[index].charAt(0);
                        final int colorIndex = this.colorcodeIdentifiers.indexOf(type);
                        if (colorIndex != -1) {
                            if (colorIndex < 16) {
                                final int colorcode = this.colorCode[colorIndex];
                                currentColor = this.getColor(colorcode, alpha);
                                bold = false;
                                italic = false;
                                randomCase = false;
                                underline = false;
                                strikethrough = false;
                            }
                            else if (colorIndex == 16) {
                                randomCase = true;
                            }
                            else if (colorIndex == 17) {
                                bold = true;
                            }
                            else if (colorIndex == 18) {
                                strikethrough = true;
                            }
                            else if (colorIndex == 19) {
                                underline = true;
                            }
                            else if (colorIndex == 20) {
                                italic = true;
                            }
                            else if (colorIndex == 21) {
                                bold = false;
                                italic = false;
                                randomCase = false;
                                underline = false;
                                strikethrough = false;
                                currentColor = c;
                            }
                            else if (colorIndex > 21) {
                                final Color customColor = this.customColorCodes[type];
                                currentColor = new Color(customColor.getRed() / 255.0f, customColor.getGreen() / 255.0f, customColor.getBlue() / 255.0f, alpha);
                            }
                        }
                        if (bold && italic) {
                            this.boldItalicFont.drawString(randomCase ? this.toRandom(currentFont, words) : words, width, 0.0, currentColor, shadow);
                            currentFont = this.boldItalicFont;
                        }
                        else if (bold) {
                            this.boldFont.drawString(randomCase ? this.toRandom(currentFont, words) : words, width, 0.0, currentColor, shadow);
                            currentFont = this.boldFont;
                        }
                        else if (italic) {
                            this.italicFont.drawString(randomCase ? this.toRandom(currentFont, words) : words, width, 0.0, currentColor, shadow);
                            currentFont = this.italicFont;
                        }
                        else {
                            this.font.drawString(randomCase ? this.toRandom(currentFont, words) : words, width, 0.0, currentColor, shadow);
                            currentFont = this.font;
                        }
                        final float u = this.font.getHeight() / 16.0f;
                        final int h = currentFont.getStringHeight(words);
                        if (strikethrough) {
                            this.drawLine(width / 2.0 + 1.0, h / 3, (width + currentFont.getStringWidth(words)) / 2.0 + 1.0, h / 3, u, currentColor);
                        }
                        if (underline) {
                            this.drawLine(width / 2.0 + 1.0, h / 2, (width + currentFont.getStringWidth(words)) / 2.0 + 1.0, h / 2, u, currentColor);
                        }
                        width += currentFont.getStringWidth(words);
                    }
                }
            }
        }
        else {
            this.font.drawString(text, 0.0, 0.0, c, shadow);
        }
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
        return (int)(x + this.getStringWidth(text));
    }

    private Color getColor(final int colorcode, final float alpha) {
        final int r = colorcode >> 16 & 0xFF;
        final int g = colorcode >> 8 & 0xFF;
        final int b = colorcode & 0xFF;
        return new Color(r / 255.0f, g / 255.0f, b / 255.0f, alpha);
    }

    private String toRandom(final XFont font, final String text) {
        String newText = "";
        final String allowedCharacters = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";
        for (final char c : text.toCharArray()) {
            if (SharedConstants.isAllowedChatCharacter(c)) {
                final int index = this.fontRandom.nextInt(allowedCharacters.length());
                newText += allowedCharacters.toCharArray()[index];
            }
        }
        return newText;
    }

    public int getStringHeight(final String text) {
        if (text == null) {
            return 0;
        }
        return this.font.getStringHeight(text) / 2;
    }

    public int getHeight() {
        return this.font.getHeight() / 2;
    }

    public static String getFormatFromString(final String text) {
        String var1 = "";
        int var2 = -1;
        final int var3 = text.length();
        while ((var2 = text.indexOf(167, var2 + 1)) != -1) {
            if (var2 < var3 - 1) {
                final char var4 = text.charAt(var2 + 1);
                if (isFormatColor(var4)) {
                    var1 = "\u00a7" + var4;
                }
                else {
                    if (!isFormatSpecial(var4)) {
                        continue;
                    }
                    var1 = var1 + "\u00a7" + var4;
                }
            }
        }
        return var1;
    }

    private static boolean isFormatSpecial(final char formatChar) {
        return (formatChar >= 'k' && formatChar <= 'o') || (formatChar >= 'K' && formatChar <= 'O') || formatChar == 'r' || formatChar == 'R';
    }

    public int getColorCode(final char character) {
        return this.colorCode["0123456789abcdef".indexOf(character)];
    }

    public void setBidiFlag(final boolean state) {
        this.bidi = state;
    }

    public boolean getBidiFlag() {
        return this.bidi;
    }

    private int sizeStringToWidth(final String str, final int wrapWidth) {
        final int var3 = str.length();
        int var4 = 0;
        int var5 = 0;
        int var6 = -1;
        boolean var7 = false;
        while (var5 < var3) {
            final char var8 = str.charAt(var5);
            if (var8 == '\n') {
                --var5;
            }
            else if (var8 == '\u00a7') {
                if (var5 < var3 - 1) {
                    ++var5;
                    final char var9 = str.charAt(var5);
                    if (var9 != 'l' && var9 != 'L') {
                        if (var9 == 'r' || var9 == 'R' || isFormatColor(var9)) {
                            var7 = false;
                        }
                    }
                    else {
                        var7 = true;
                    }
                }
            }
            else {
                if (var8 == ' ') {
                    var6 = var5;
                }
                var4 += this.getStringWidth(Character.toString(var8));
                if (var7) {
                    ++var4;
                }
            }
            if (var8 == '\n') {
                var6 = ++var5;
                break;
            }
            if (var4 > wrapWidth) {
                break;
            }
            ++var5;
        }
        return (var5 != var3 && var6 != -1 && var6 < var5) ? var6 : var5;
    }

    private static boolean isFormatColor(final char colorChar) {
        return (colorChar >= '0' && colorChar <= '9') || (colorChar >= 'a' && colorChar <= 'f') || (colorChar >= 'A' && colorChar <= 'F');
    }

    public int getCharWidth(final char c) {
        return this.getStringWidth(Character.toString(c));
    }

    public int getStringWidth(final String text) {
        if (text == null) {
            return 0;
        }
        if (text.contains("\u00a7")) {
            final String[] parts = text.split("\u00a7");
            XFont currentFont = this.font;
            int width = 0;
            boolean bold = false;
            boolean italic = false;
            for (int index = 0; index < parts.length; ++index) {
                if (parts[index].length() > 0) {
                    if (index == 0) {
                        width += currentFont.getStringWidth(parts[index]);
                    }
                    else {
                        final String words = parts[index].substring(1);
                        final char type = parts[index].charAt(0);
                        final int colorIndex = this.colorcodeIdentifiers.indexOf(type);
                        if (colorIndex != -1) {
                            if (colorIndex < 16) {
                                bold = false;
                                italic = false;
                            }
                            else if (colorIndex != 16) {
                                if (colorIndex == 17) {
                                    bold = true;
                                }
                                else if (colorIndex != 18) {
                                    if (colorIndex != 19) {
                                        if (colorIndex == 20) {
                                            italic = true;
                                        }
                                        else if (colorIndex == 21) {
                                            bold = false;
                                            italic = false;
                                        }
                                    }
                                }
                            }
                        }
                        if (bold && italic) {
                            currentFont = this.boldItalicFont;
                        }
                        else if (bold) {
                            currentFont = this.boldFont;
                        }
                        else if (italic) {
                            currentFont = this.italicFont;
                        }
                        else {
                            currentFont = this.font;
                        }
                        width += currentFont.getStringWidth(words);
                    }
                }
            }
            return width / 2;
        }
        return this.font.getStringWidth(text) / 2;
    }

    public void setFont(final Font font, final boolean antiAlias, final int charOffset) {
        synchronized (this) {
            this.font = new XFont(font, antiAlias, charOffset);
            this.boldFont = new XFont(font.deriveFont(1), antiAlias, charOffset);
            this.italicFont = new XFont(font.deriveFont(2), antiAlias, charOffset);
            this.boldItalicFont = new XFont(font.deriveFont(3), antiAlias, charOffset);
            this.FONT_HEIGHT = this.getHeight();
        }
    }

    public XFont getFont() {
        return this.font;
    }

    public String getFontName() {
        return this.font.getFont().getFontName();
    }

    public int getSize() {
        return this.font.getFont().getSize();
    }

    public List<String> wrapWords(final String text, final double width) {
        final List<String> finalWords = new ArrayList<String>();
        if (this.getStringWidth(text) > width) {
            final String[] words = text.split(" ");
            String currentWord = "";
            char lastColorCode = '\uffff';
            for (final String word : words) {
                for (int i = 0; i < word.toCharArray().length; ++i) {
                    final char c = word.toCharArray()[i];
                    if (c == '\u00a7' && i < word.toCharArray().length - 1) {
                        lastColorCode = word.toCharArray()[i + 1];
                    }
                }
                if (this.getStringWidth(currentWord + word + " ") < width) {
                    currentWord = currentWord + word + " ";
                }
                else {
                    finalWords.add(currentWord);
                    currentWord = ((lastColorCode == -1) ? (word + " ") : ("\u00a7" + lastColorCode + word + " "));
                }
            }
            if (!currentWord.equals("")) {
                if (this.getStringWidth(currentWord) < width) {
                    finalWords.add((lastColorCode == -1) ? (currentWord + " ") : ("\u00a7" + lastColorCode + currentWord + " "));
                    currentWord = "";
                }
                else {
                    for (final String s : this.formatString(currentWord, width)) {
                        finalWords.add(s);
                    }
                }
            }
        }
        else {
            finalWords.add(text);
        }
        return finalWords;
    }

    public List<String> formatString(final String s, final double width) {
        final List<String> finalWords = new ArrayList<String>();
        String currentWord = "";
        char lastColorCode = '\uffff';
        for (int i = 0; i < s.toCharArray().length; ++i) {
            final char c = s.toCharArray()[i];
            if (c == '\u00a7' && i < s.toCharArray().length - 1) {
                lastColorCode = s.toCharArray()[i + 1];
            }
            if (this.getStringWidth(currentWord + c) < width) {
                currentWord += c;
            }
            else {
                finalWords.add(currentWord);
                currentWord = ((lastColorCode == -1) ? String.valueOf(c) : ("\u00a7" + lastColorCode + String.valueOf(c)));
            }
        }
        if (!currentWord.equals("")) {
            finalWords.add(currentWord);
        }
        return finalWords;
    }

    private void drawLine(final double x, final double y, final double x1, final double y1, final float width, final Color color) {
        RenderSystem.disableTexture();
        RenderSystem.lineWidth(width);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        final BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        final float red = color.getRed() / 255.0f;
        final float green = color.getGreen() / 255.0f;
        final float blue = color.getBlue() / 255.0f;
        final float alpha = color.getAlpha() / 255.0f;
        buffer.vertex(x, y, 0.0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(x1, y1, 0.0).color(red, green, blue, alpha).endVertex();
        Tesselator.getInstance().end();
        RenderSystem.enableTexture();
    }

    private String setupColorcodeIdentifier() {
        String s = "0123456789abcdef";
        for (int i = 0; i < this.customColorCodes.length; ++i) {
            if (this.customColorCodes[i] != null) {
                s += (char)i;
            }
        }
        return s + "klmnor";
    }

    private void setupMinecraftColorcodes() {
        for (int index = 0; index < 32; ++index) {
            final int noClue = (index >> 3 & 0x1) * 85;
            int red = (index >> 2 & 0x1) * 170 + noClue;
            int green = (index >> 1 & 0x1) * 170 + noClue;
            int blue = (index >> 0 & 0x1) * 170 + noClue;
            if (index == 6) {
                red += 85;
            }
            if (index >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }
            this.colorCode[index] = ((red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF));
        }
    }
}
