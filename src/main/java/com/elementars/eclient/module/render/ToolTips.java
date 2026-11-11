package com.elementars.eclient.module.render;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.mixin.mixins.AccessorClientTextTooltip;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import dev.xulu.settings.Value;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.Color;
import java.util.List;

public class ToolTips extends Module
{
    private final Value<Boolean> cf;
    private final Value<Boolean> cb;
    private final Value<Boolean> outline;
    private final Value<Integer> r;
    private final Value<Integer> g;
    private final Value<Integer> b;
    private final Value<Integer> Or;
    private final Value<Integer> Og;
    private final Value<Integer> Ob;

    public ToolTips() {
        super("ToolTips", "Custom ToolTips", 0, Category.RENDER, true);
        this.cf = this.register(new Value<Boolean>("Custom Font", this, true));
        this.cb = this.register(new Value<Boolean>("Custom Background", this, true));
        this.outline = this.register(new Value<Boolean>("Outline", this, true));
        this.r = this.register(new Value<Integer>("Red", this, 7, 0, 255));
        this.g = this.register(new Value<Integer>("Green", this, 12, 0, 255));
        this.b = this.register(new Value<Integer>("Blue", this, 17, 0, 255));
        this.Or = this.register(new Value<Integer>("Outline Red", this, 10, 0, 255));
        this.Og = this.register(new Value<Integer>("Outline Green", this, 12, 0, 255));
        this.Ob = this.register(new Value<Integer>("Outline Blue", this, 43, 0, 255));
    }

    @Override
    public void onEnable() {
        ToolTips.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        ToolTips.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onToolText(final RenderTooltipEvent.Pre event) {
        final List<ClientTooltipComponent> components = event.getComponents();
        if (components.isEmpty()) {
            return;
        }
        final Font font = event.getFont();
        int tooltipTextWidth = 0;
        int tooltipHeight = components.size() == 1 ? -2 : 0;
        for (final ClientTooltipComponent component : components) {
            tooltipTextWidth = Math.max(tooltipTextWidth, component.getWidth(font));
            tooltipHeight += component.getHeight();
        }
        int tooltipX = event.getX() + 12;
        int tooltipY = event.getY() - 12;
        if (tooltipX + tooltipTextWidth > event.getScreenWidth()) {
            tooltipX -= 28 + tooltipTextWidth;
        }
        if (tooltipY + tooltipHeight + 6 > event.getScreenHeight()) {
            tooltipY = event.getScreenHeight() - tooltipHeight - 6;
        }
        if (tooltipY < 4) {
            tooltipY = 4;
        }
        final PoseStack poseStack = event.getPoseStack();
        final float oldBlitOffset = ToolTips.mc.getItemRenderer().blitOffset;
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, 400.0);
        ToolTips.mc.getItemRenderer().blitOffset = 400.0f;
        final Matrix4f matrix = poseStack.last().pose();
        RenderSystem.disableDepthTest();
        if (this.cb.getValue()) {
            GuiComponent.fill(poseStack, tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4,
                new Color(this.r.getValue(), this.g.getValue(), this.b.getValue()).getRGB());
            if (this.outline.getValue()) {
                final int outlineColor = new Color(this.Or.getValue(), this.Og.getValue(), this.Ob.getValue()).getRGB();
                GuiComponent.fill(poseStack, tooltipX - 4, tooltipY - 5, tooltipX + tooltipTextWidth + 4, tooltipY - 4, outlineColor);
                GuiComponent.fill(poseStack, tooltipX - 4, tooltipY + tooltipHeight + 4, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 5, outlineColor);
                GuiComponent.fill(poseStack, tooltipX - 4, tooltipY - 4, tooltipX - 3, tooltipY + tooltipHeight + 4, outlineColor);
                GuiComponent.fill(poseStack, tooltipX + tooltipTextWidth + 3, tooltipY - 4, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 4, outlineColor);
            }
        }
        else {
            final int backgroundColor = -267386864;
            final int borderColorStart = 1347420415;
            final int borderColorEnd = 1344798847;
            fillGradient(matrix, tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, backgroundColor, backgroundColor);
            fillGradient(matrix, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
            fillGradient(matrix, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            fillGradient(matrix, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            fillGradient(matrix, tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            fillGradient(matrix, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
            fillGradient(matrix, tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
            fillGradient(matrix, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart);
            fillGradient(matrix, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);
        }
        MultiBufferSource.BufferSource buffer = null;
        int lineY = tooltipY;
        for (int i = 0; i < components.size(); ++i) {
            final ClientTooltipComponent component = components.get(i);
            if (this.cf.getValue() && component instanceof ClientTextTooltip) {
                final FormattedCharSequence sequence = ((AccessorClientTextTooltip)component).xulu$getText();
                drawCustomText(poseStack, toLegacy(sequence), tooltipX, lineY);
            }
            else {
                if (buffer == null) {
                    buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                }
                component.renderText(font, tooltipX, lineY, matrix, buffer);
            }
            lineY += component.getHeight() + (i == 0 ? 2 : 0);
        }
        if (buffer != null) {
            buffer.endBatch();
        }
        lineY = tooltipY;
        for (int i = 0; i < components.size(); ++i) {
            final ClientTooltipComponent component = components.get(i);
            component.renderImage(font, tooltipX, lineY, poseStack, ToolTips.mc.getItemRenderer(), 400);
            lineY += component.getHeight() + (i == 0 ? 2 : 0);
        }
        ToolTips.mc.getItemRenderer().blitOffset = oldBlitOffset;
        RenderSystem.enableDepthTest();
        poseStack.popPose();
        event.setCanceled(true);
    }

    private static void fillGradient(final Matrix4f matrix, final int x1, final int y1, final int x2, final int y2, final int color1, final int color2) {
        final float a1 = (color1 >> 24 & 255) / 255.0f;
        final float r1 = (color1 >> 16 & 255) / 255.0f;
        final float g1 = (color1 >> 8 & 255) / 255.0f;
        final float b1 = (color1 & 255) / 255.0f;
        final float a2 = (color2 >> 24 & 255) / 255.0f;
        final float r2 = (color2 >> 16 & 255) / 255.0f;
        final float g2 = (color2 >> 8 & 255) / 255.0f;
        final float b2 = (color2 & 255) / 255.0f;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        final BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrix, (float)x2, (float)y1, 0.0f).color(r1, g1, b1, a1).endVertex();
        buffer.vertex(matrix, (float)x1, (float)y1, 0.0f).color(r1, g1, b1, a1).endVertex();
        buffer.vertex(matrix, (float)x1, (float)y2, 0.0f).color(r2, g2, b2, a2).endVertex();
        buffer.vertex(matrix, (float)x2, (float)y2, 0.0f).color(r2, g2, b2, a2).endVertex();
        Tesselator.getInstance().end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    private static void drawCustomText(final PoseStack poseStack, final String text, final int x, final int y) {
        if (Xulu.cFontRenderer == null) {
            ToolTips.mc.font.drawShadow(poseStack, text, (float)x, (float)y, -1);
            return;
        }
        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().mulPoseMatrix(poseStack.last().pose());
        RenderSystem.applyModelViewMatrix();
        try {
            Xulu.cFontRenderer.drawStringWithShadow(text, x, y, -1);
        }
        finally {
            RenderSystem.getModelViewStack().popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    private static String toLegacy(final FormattedCharSequence sequence) {
        final StringBuilder builder = new StringBuilder();
        final Style[] last = new Style[1];
        sequence.accept((index, style, codePoint) -> {
            if (!style.equals(last[0])) {
                final TextColor color = style.getColor();
                final ChatFormatting formatting = color == null ? null : byColor(color.getValue());
                if (formatting != null) {
                    builder.append('\u00a7').append(formatting.getChar());
                }
                else {
                    builder.append('\u00a7').append('r');
                }
                if (style.isBold()) {
                    builder.append('\u00a7').append('l');
                }
                if (style.isItalic()) {
                    builder.append('\u00a7').append('o');
                }
                if (style.isUnderlined()) {
                    builder.append('\u00a7').append('n');
                }
                if (style.isStrikethrough()) {
                    builder.append('\u00a7').append('m');
                }
                if (style.isObfuscated()) {
                    builder.append('\u00a7').append('k');
                }
                last[0] = style;
            }
            builder.appendCodePoint(codePoint);
            return true;
        });
        return builder.toString();
    }

    private static ChatFormatting byColor(final int rgb) {
        for (final ChatFormatting formatting : ChatFormatting.values()) {
            if (formatting.getColor() != null && formatting.getColor() == rgb) {
                return formatting;
            }
        }
        return null;
    }
}
