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
        this.align = this.register(new Value<String>("Align", this, "Left", new ArrayList<String>(Arrays.asList("Left", "Right"))));
        this.order = this.register(new Value<String>("Order", this, "Up", new ArrayList<String>(Arrays.asList("Up", "Down"))));
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
        float yCount = (float)this.y;
        if (this.order.getValue().equalsIgnoreCase("Down")) {
            yCount += (float)(this.height - 10.0);
        }
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
            if (Xulu.CustomFont) {
                Xulu.cFontRenderer.drawStringWithShadow(text, (this.align.getValue().equalsIgnoreCase("Right") ? (this.x - Xulu.cFontRenderer.getStringWidth(text) + this.getFrame().width) : this.x) + 1.0, yCount + 1.0f, ColorUtils.changeAlpha(color, Global.hudAlpha.getValue()));
            }
            else {
                Potions.fontRenderer.drawShadow(new PoseStack(), text, (float)((this.align.getValue().equalsIgnoreCase("Right") ? (this.x - Potions.fontRenderer.width(text) + this.getFrame().width) : this.x) + 1.0), yCount + 1.0f, ColorUtils.changeAlpha(color, Global.hudAlpha.getValue()));
            }
            yCount += (this.order.getValue().equalsIgnoreCase("Up") ? 10 : -10);
        }
    }

    public String getTime(int duration) {
        int min;
        for (min = 0; duration > 59; duration -= 60, ++min) {}
        return min + ":" + this.df.format(duration);
    }
}
