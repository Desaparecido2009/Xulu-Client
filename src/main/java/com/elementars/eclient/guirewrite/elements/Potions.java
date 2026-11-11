package com.elementars.eclient.guirewrite.elements;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.guirewrite.Element;
import com.elementars.eclient.module.core.Global;
import com.elementars.eclient.util.ColorUtils;
import com.elementars.eclient.util.PotionUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xulu.settings.Value;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Potions extends Element
{
    private final DecimalFormat df;
    private final Value<Boolean> pc;
    private final Value<Integer> red;
    private final Value<Integer> green;
    private final Value<Integer> blue;
    private final Value<Boolean> onlysw;
    private final Value<String> align;
    private final Value<String> order;

    public Potions() {
        super("Potions");
        this.df = new DecimalFormat("00");
        this.pc = this.register(new Value<Boolean>("Use Potion Color", this, true));
        this.red = this.register(new Value<Integer>("Red", this, 0, 0, 255));
        this.green = this.register(new Value<Integer>("Green", this, 0, 0, 255));
        this.blue = this.register(new Value<Integer>("Blue", this, 0, 0, 255));
        this.onlysw = this.register(new Value<Boolean>("Only Str & Weak", this, false));
        this.align = this.register(new Value<String>("Align", this, "Auto", new ArrayList<String>(Arrays.asList("Auto", "Left", "Right"))));
        this.order = this.register(new Value<String>("Order", this, "Auto", new ArrayList<String>(Arrays.asList("Auto", "Up", "Down"))));
    }

    @Override
    public void onEnable() {
        this.width = 50.0;
        this.height = 50.0;
        super.onEnable();
    }

    @Override
    public void onRender() {
        if (Potions.mc.player == null) {
            return;
        }
        final List<String> texts = new ArrayList<String>();
        final List<Integer> colors = new ArrayList<Integer>();
        for (final MobEffectInstance potionEffect : Potions.mc.player.getActiveEffects()) {
            if (this.onlysw.getValue() && potionEffect.getEffect() != MobEffects.DAMAGE_BOOST && potionEffect.getEffect() != MobEffects.WEAKNESS) {
                continue;
            }
            final String time = this.getTime(potionEffect.getDuration() / 20);
            final String text = PotionUtil.getPotionName(potionEffect.getEffect()) + " " + ((potionEffect.getAmplifier() + 1 != 1) ? (potionEffect.getAmplifier() + 1 + " ") : "") + ChatFormatting.GRAY + ((time.length() == 1) ? "0" : "") + time;
            int color = new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue()).getRGB();
            if (this.pc.getValue()) {
                color = potionEffect.getEffect().getColor();
            }
            texts.add(text);
            colors.add(color);
        }
        int maxWidth = 0;
        for (final String text : texts) {
            maxWidth = Math.max(maxWidth, this.getStringWidth(text));
        }
        this.width = Math.max(50.0, maxWidth + 2.0);
        this.height = Math.max(50.0, texts.size() * 10.0 + 1.0);
        final String alignValue = this.align.getValue();
        final boolean rightAligned = alignValue.equalsIgnoreCase("Right")
                || (alignValue.equalsIgnoreCase("Auto") && this.isRightHalf());
        final String orderValue = this.order.getValue();
        final boolean growUp = orderValue.equalsIgnoreCase("Down")
                || (orderValue.equalsIgnoreCase("Auto") && this.isBottomHalf());
        float yCount = growUp ? (float)(this.y + this.height - 10.0) : (float)this.y;
        for (int i = 0; i < texts.size(); ++i) {
            final String text = texts.get(i);
            final double textX = rightAligned ? this.x + this.width - 1.0 - this.getStringWidth(text) : this.x + 1.0;
            final int color = ColorUtils.changeAlpha(colors.get(i), Global.hudAlpha.getValue());
            if (Xulu.CustomFont) {
                Xulu.cFontRenderer.drawStringWithShadow(text, (float)textX, yCount + 1.0f, color);
            }
            else {
                Potions.fontRenderer.drawShadow(new PoseStack(), text, (float)textX, yCount + 1.0f, color);
            }
            yCount += growUp ? -10.0f : 10.0f;
        }
    }

    private int getStringWidth(final String text) {
        return Xulu.CustomFont ? Xulu.cFontRenderer.getStringWidth(text) : Potions.fontRenderer.width(text);
    }

    public String getTime(int duration) {
        int min;
        for (min = 0; duration > 59; duration -= 60, ++min) {}
        return min + ":" + this.df.format(duration);
    }
}
