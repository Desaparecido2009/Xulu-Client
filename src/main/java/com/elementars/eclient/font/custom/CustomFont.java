package com.elementars.eclient.font.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

public class CustomFont extends CFont
{
    protected CharData[] boldChars;
    protected CharData[] italicChars;
    protected CharData[] boldItalicChars;
    protected DynamicTexture texBold;
    protected DynamicTexture texItalic;
    protected DynamicTexture texItalicBold;
    private final int[] colorCode;
    private BufferBuilder buffer;
    private DynamicTexture boundTex;

    public CustomFont(final Font font, final boolean antiAlias, final boolean fractionalMetrics) {
        super(font, antiAlias, fractionalMetrics);
        this.boldChars = new CharData[256];
        this.italicChars = new CharData[256];
        this.boldItalicChars = new CharData[256];
        this.colorCode = new int[32];
        this.setupMinecraftColorcodes();
        this.setupBoldItalicIDs();
    }

    public float drawStringWithShadow(final String text, final double x, final double y, final int color) {
        final float shadowWidth = this.drawString(text, x + 1.0, y + 1.0, color, true);
        return Math.max(shadowWidth, this.drawString(text, x, y, color, false));
    }

    public float drawString(final String text, final float x, final float y, final int color) {
        return this.drawString(text, x, y, color, false);
    }

    public float drawCenteredString(final String text, final float x, final float y, final int color) {
        return this.drawString(text, x - this.getStringWidth(text) / 2, y, color);
    }

    public float drawCenteredStringWithShadow(final String text, final float x, final float y, final int color) {
        final float shadowWidth = this.drawString(text, x - this.getStringWidth(text) / 2 + 1.0, y + 1.0, color, true);
        return this.drawString(text, x - this.getStringWidth(text) / 2, y, color);
    }

    public float drawString(final String text, final double xI, final double yI, int color, final boolean shadow) {
        double x = xI;
        double y = yI;
        if (!com.elementars.eclient.module.core.CustomFont.shadow.getValue() && shadow) {
            x -= 0.5;
            y -= 0.5;
        }
        --x;
        if (text == null) {
            return 0.0f;
        }
        if (color == 553648127) {
            color = 16777215;
        }
        if ((color & 0xFC000000) == 0x0) {
            color |= 0xFF000000;
        }
        if (shadow) {
            color = ((color & 0xFCFCFC) >> 2 | (color & 0xFF000000));
        }
        CharData[] currentData = this.charData;
        final float alpha = (color >> 24 & 0xFF) / 255.0f;
        float red = (color >> 16 & 0xFF) / 255.0f;
        float green = (color >> 8 & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        boolean bold = false;
        boolean italic = false;
        boolean strikethrough = false;
        boolean underline = false;
        x *= 2.0;
        y = (y - 3.0) * 2.0;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().scale(0.5f, 0.5f, 1.0f);
        RenderSystem.applyModelViewMatrix();

        this.boundTex = this.tex;
        this.beginBuffer();

        final int size = text.length();
        for (int i = 0; i < size; ++i) {
            final char character = text.charAt(i);
            if (character == '\u00a7' && i < size) {
                int colorIndex = 21;
                try {
                    colorIndex = "0123456789abcdefklmnor".indexOf(text.charAt(i + 1));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                if (colorIndex < 16) {
                    bold = false;
                    italic = false;
                    underline = false;
                    strikethrough = false;
                    currentData = this.charData;
                    if (colorIndex < 0 || colorIndex > 15) {
                        colorIndex = 15;
                    }
                    if (shadow) {
                        colorIndex += 16;
                    }
                    final int colorcode = this.colorCode[colorIndex];
                    red = (colorcode >> 16 & 0xFF) / 255.0f;
                    green = (colorcode >> 8 & 0xFF) / 255.0f;
                    blue = (colorcode & 0xFF) / 255.0f;
                    this.bindTexture(this.tex);
                }
                else if (colorIndex == 17) {
                    bold = true;
                    if (italic) {
                        this.bindTexture(this.texItalicBold);
                        currentData = this.boldItalicChars;
                    }
                    else {
                        this.bindTexture(this.texBold);
                        currentData = this.boldChars;
                    }
                }
                else if (colorIndex == 18) {
                    strikethrough = true;
                }
                else if (colorIndex == 19) {
                    underline = true;
                }
                else if (colorIndex == 20) {
                    italic = true;
                    if (bold) {
                        this.bindTexture(this.texItalicBold);
                        currentData = this.boldItalicChars;
                    }
                    else {
                        this.bindTexture(this.texItalic);
                        currentData = this.italicChars;
                    }
                }
                else if (colorIndex == 21) {
                    bold = false;
                    italic = false;
                    underline = false;
                    strikethrough = false;
                    red = (color >> 16 & 0xFF) / 255.0f;
                    green = (color >> 8 & 0xFF) / 255.0f;
                    blue = (color & 0xFF) / 255.0f;
                    this.bindTexture(this.tex);
                    currentData = this.charData;
                }
                ++i;
            }
            else if (character < currentData.length && character >= '\0') {
                this.drawChar(this.buffer, currentData, character, (float)x, (float)y, red, green, blue, alpha);
                if (strikethrough) {
                    this.flushBuffer();
                    this.drawLine(x, y + currentData[character].height / 2.0, x + currentData[character].width - 8.0, y + currentData[character].height / 2.0, 1.0f, red, green, blue, alpha);
                    this.beginBuffer();
                }
                if (underline) {
                    this.flushBuffer();
                    this.drawLine(x, y + currentData[character].height - 2.0, x + currentData[character].width - 8.0, y + currentData[character].height - 2.0, 1.0f, red, green, blue, alpha);
                    this.beginBuffer();
                }
                x += currentData[character].width - 8 + this.charOffset;
            }
        }
        this.flushBuffer();

        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableCull();
        return (float)x / 2.0f;
    }

    private void beginBuffer() {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        if (this.boundTex != null) {
            RenderSystem.setShaderTexture(0, this.boundTex.getId());
        }
        this.buffer = Tesselator.getInstance().getBuilder();
        this.buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
    }

    private void flushBuffer() {
        if (this.buffer != null && this.buffer.building()) {
            Tesselator.getInstance().end();
        }
    }

    private void bindTexture(final DynamicTexture texture) {
        if (texture != this.boundTex) {
            this.flushBuffer();
            this.boundTex = texture;
            this.beginBuffer();
        }
    }

    private void drawLine(final double x, final double y, final double x1, final double y1, final float width, final float red, final float green, final float blue, final float alpha) {
        RenderSystem.disableTexture();
        RenderSystem.lineWidth(width);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        final BufferBuilder lineBuffer = Tesselator.getInstance().getBuilder();
        lineBuffer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        lineBuffer.vertex(x, y, 0.0).color(red, green, blue, alpha).endVertex();
        lineBuffer.vertex(x1, y1, 0.0).color(red, green, blue, alpha).endVertex();
        Tesselator.getInstance().end();
        RenderSystem.enableTexture();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        if (this.boundTex != null) {
            RenderSystem.setShaderTexture(0, this.boundTex.getId());
        }
    }

    @Override
    public int getStringWidth(final String text) {
        if (text == null) {
            return 0;
        }
        int width = 0;
        CharData[] currentData = this.charData;
        boolean bold = false;
        boolean italic = false;
        for (int size = text.length(), i = 0; i < size; ++i) {
            final char character = text.charAt(i);
            if (character == '\u00a7' && i < size) {
                final int colorIndex = "0123456789abcdefklmnor".indexOf(character);
                if (colorIndex < 16) {
                    bold = false;
                    italic = false;
                }
                else if (colorIndex == 17) {
                    bold = true;
                    if (italic) {
                        currentData = this.boldItalicChars;
                    }
                    else {
                        currentData = this.boldChars;
                    }
                }
                else if (colorIndex == 20) {
                    italic = true;
                    if (bold) {
                        currentData = this.boldItalicChars;
                    }
                    else {
                        currentData = this.italicChars;
                    }
                }
                else if (colorIndex == 21) {
                    bold = false;
                    italic = false;
                    currentData = this.charData;
                }
                ++i;
            }
            else if (character < currentData.length && character >= '\0') {
                width += currentData[character].width - 8 + this.charOffset;
            }
        }
        return width / 2;
    }

    @Override
    public void setFont(final Font font) {
        super.setFont(font);
        this.setupBoldItalicIDs();
    }

    @Override
    public void setAntiAlias(final boolean antiAlias) {
        super.setAntiAlias(antiAlias);
        this.setupBoldItalicIDs();
    }

    @Override
    public void setFractionalMetrics(final boolean fractionalMetrics) {
        super.setFractionalMetrics(fractionalMetrics);
        this.setupBoldItalicIDs();
    }

    private void setupBoldItalicIDs() {
        this.texBold = this.setupTexture(this.font.deriveFont(1), this.antiAlias, this.fractionalMetrics, this.boldChars);
        this.texItalic = this.setupTexture(this.font.deriveFont(2), this.antiAlias, this.fractionalMetrics, this.italicChars);
        this.texItalicBold = this.setupTexture(this.font.deriveFont(3), this.antiAlias, this.fractionalMetrics, this.boldItalicChars);
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
                    currentWord = "\u00a7" + lastColorCode + word + " ";
                }
            }
            if (currentWord.length() > 0) {
                if (this.getStringWidth(currentWord) < width) {
                    finalWords.add("\u00a7" + lastColorCode + currentWord + " ");
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

    public List<String> formatString(final String string, final double width) {
        final List<String> finalWords = new ArrayList<String>();
        String currentWord = "";
        char lastColorCode = '\uffff';
        final char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            final char c = chars[i];
            if (c == '\u00a7' && i < chars.length - 1) {
                lastColorCode = chars[i + 1];
            }
            if (this.getStringWidth(currentWord + c) < width) {
                currentWord += c;
            }
            else {
                finalWords.add(currentWord);
                currentWord = "\u00a7" + lastColorCode + c;
            }
        }
        if (currentWord.length() > 0) {
            finalWords.add(currentWord);
        }
        return finalWords;
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
