package com.elementars.eclient.guirewrite.elements;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.command.Command;
import com.elementars.eclient.guirewrite.Element;
import com.elementars.eclient.module.core.Global;
import com.elementars.eclient.util.ColorUtils;
import com.elementars.eclient.util.LagCompensator;
import com.elementars.eclient.util.PotionUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xulu.newgui.util.ColorUtil;
import dev.xulu.settings.Value;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Info extends Element
{
    private final Value<Boolean> rainbow;
    private final Value<Boolean> t24;
    private final Value<String> mode;
    private final Value<String> order;
    private final Value<String> color2;
    private final Value<Boolean> FPS;
    private final Value<Boolean> PING;
    private final Value<Boolean> TPS;
    private final Value<Boolean> SPEED;
    private final Value<Boolean> TIME;
    private final Value<Boolean> DURABILITY;
    private final Value<Boolean> SERVER_IP;
    private final Value<Boolean> POTIONS;
    private final Value<Boolean> ALPHABETICAL;
    private final DecimalFormat df;
    private final DecimalFormat df2;

    public Info() {
        super("Info");
        this.rainbow = this.register(new Value<Boolean>("Rainbow", this, false));
        this.t24 = this.register(new Value<Boolean>("24hr time", this, false));
        this.mode = this.register(new Value<String>("Aligned", this, "Right", new String[] { "Right", "Left" }));
        this.order = this.register(new Value<String>("Ordering", this, "Down", new String[] { "Up", "Down" }));
        this.color2 = this.register(new Value<String>("2nd Color", this, "LightGray", com.elementars.eclient.util.ColorTextUtils.colors));
        this.FPS = this.register(new Value<Boolean>("Fps", this, true));
        this.PING = this.register(new Value<Boolean>("Ping", this, true));
        this.TPS = this.register(new Value<Boolean>("Tps", this, true));
        this.SPEED = this.register(new Value<Boolean>("Speed", this, true));
        this.TIME = this.register(new Value<Boolean>("Time", this, true));
        this.DURABILITY = this.register(new Value<Boolean>("Durability", this, true));
        this.SERVER_IP = this.register(new Value<Boolean>("Server IP", this, true));
        this.POTIONS = this.register(new Value<Boolean>("Potions", this, true));
        this.ALPHABETICAL = this.register(new Value<Boolean>("ABC Potions", this, false));
        this.df = new DecimalFormat("#.#");
        this.df2 = new DecimalFormat("#.###");
    }

    public static double coordsDiff(final char s) {
        switch (s) {
            case 'x': {
                return Info.mc.player.getX() - Info.mc.player.xOld;
            }
            case 'z': {
                return Info.mc.player.getZ() - Info.mc.player.zOld;
            }
            default: {
                return 0.0;
            }
        }
    }

    public boolean isToolArmor(final Item i) {
        return i instanceof ArmorItem || i instanceof SwordItem || i instanceof DiggerItem || i instanceof HoeItem;
    }

    @Override
    public void onRender() {
        if (Info.mc.player == null || Info.mc.level == null) {
            return;
        }
        final String section = String.valueOf(Command.SECTIONSIGN());
        final String secondColor = section + com.elementars.eclient.util.ColorTextUtils.getColor(this.color2.getValue()).substring(1);
        int color = ColorUtil.getClickGUIColor().getRGB();
        if (this.rainbow.getValue()) {
            color = Xulu.rgb;
        }
        final ItemStack itemStack = Info.mc.player.getMainHandItem();
        final List<String> infolist = new ArrayList<String>();
        final Map<String, MobEffectInstance> potionMap = new HashMap<String, MobEffectInstance>();
        List<String> potionList = new ArrayList<String>();
        if (this.FPS.getValue()) {
            infolist.add("FPS: " + secondColor + this.getFps());
        }
        if (this.PING.getValue()) {
            final int ping = (Info.mc.getConnection() != null && Info.mc.getConnection().getPlayerInfo(Info.mc.player.getUUID()) != null) ? Info.mc.getConnection().getPlayerInfo(Info.mc.player.getUUID()).getLatency() : -1;
            infolist.add("Ping: " + secondColor + ping + "ms");
        }
        if (this.TPS.getValue()) {
            final float tickRate = LagCompensator.INSTANCE == null ? 20.0f : LagCompensator.INSTANCE.getTickRate();
            infolist.add("TPS: " + secondColor + this.df2.format(tickRate));
        }
        if (this.SPEED.getValue()) {
            infolist.add("Speed: " + secondColor + this.df.format(Math.sqrt(Math.pow(coordsDiff('x'), 2.0) + Math.pow(coordsDiff('z'), 2.0)) / 0.05 * 3.6) + " hm/h");
        }
        if (this.TIME.getValue()) {
            infolist.add("Time: " + secondColor + (this.t24.getValue() ? new SimpleDateFormat("k:mm").format(new Date()) : new SimpleDateFormat("h:mm a").format(new Date())));
        }
        if (this.DURABILITY.getValue() && this.isToolArmor(itemStack.getItem()) && itemStack.isDamageableItem()) {
            infolist.add("Durability: " + secondColor + (itemStack.getMaxDamage() - itemStack.getDamageValue()));
        }
        if (this.SERVER_IP.getValue()) {
            infolist.add("Server IP: " + secondColor + ((Info.mc.getCurrentServer() == null) ? "None" : Info.mc.getCurrentServer().ip));
        }
        if (this.POTIONS.getValue()) {
            for (final MobEffectInstance potionEffect : Info.mc.player.getActiveEffects()) {
                final String text = PotionUtil.getPotionName(potionEffect.getEffect()) + " " + ((potionEffect.getAmplifier() + 1 != 1) ? (potionEffect.getAmplifier() + 1 + " ") : "") + secondColor + this.getTime(potionEffect.getDuration() / 20);
                potionMap.put(text, potionEffect);
            }
            potionList = new ArrayList<String>(potionMap.keySet());
            potionList.sort(Comparator.comparingInt(this::getStringWidth));
            if (this.ALPHABETICAL.getValue()) {
                Collections.sort(potionList);
                if (this.order.getValue().equalsIgnoreCase("Down")) {
                    Collections.reverse(potionList);
                }
            }
        }
        infolist.sort(Comparator.comparingInt(this::getStringWidth));
        Collections.reverse(infolist);
        int maxWidth = 0;
        for (final String s : infolist) {
            maxWidth = Math.max(maxWidth, this.getStringWidth(s));
        }
        for (final String s : potionList) {
            maxWidth = Math.max(maxWidth, this.getStringWidth(s));
        }
        this.width = Math.max(50.0, maxWidth + 2.0);
        this.height = Math.max(50.0, (infolist.size() + potionList.size()) * 10.0 + 1.0);
        final boolean growUp = this.order.getValue().equalsIgnoreCase("Down");
        final boolean rightAligned = this.mode.getValue().equalsIgnoreCase("Right");
        float yCount = growUp ? (float)(this.y + this.height - 10.0) : (float)this.y;
        for (final String s : potionList) {
            final MobEffectInstance effect = potionMap.get(s);
            final int potionColor = ColorUtils.changeAlpha(effect.getEffect().getColor(), Global.hudAlpha.getValue());
            this.drawText(s, rightAligned, yCount + 1.0f, potionColor);
            yCount += growUp ? -10.0f : 10.0f;
        }
        for (final String s : infolist) {
            this.drawText(s, rightAligned, yCount + 1.0f, ColorUtils.changeAlpha(color, Global.hudAlpha.getValue()));
            yCount += growUp ? -10.0f : 10.0f;
        }
    }

    private void drawText(final String text, final boolean rightAligned, final float y, final int color) {
        final double textX = rightAligned ? this.x + this.width - 1.0 - this.getStringWidth(text) : this.x + 1.0;
        if (Xulu.CustomFont) {
            Xulu.cFontRenderer.drawStringWithShadow(text, (float)textX, y, color);
        }
        else {
            Info.fontRenderer.drawShadow(new PoseStack(), text, (float)textX, y, color);
        }
    }

    private int getStringWidth(final String text) {
        return Xulu.CustomFont ? Xulu.cFontRenderer.getStringWidth(text) : Info.fontRenderer.width(text);
    }

    private int getFps() {
        final long[] log = Info.mc.getFrameTimer().getLog();
        long sum = 0L;
        int count = 0;
        for (final long frame : log) {
            if (frame > 0L) {
                sum += frame;
                ++count;
            }
        }
        return (count > 0 && sum > 0L) ? (int)(1000000000L * count / sum) : 0;
    }

    public String getTime(int duration) {
        int min;
        for (min = 0; duration > 59; duration -= 60, ++min) {}
        return min + ":" + ((this.df.format(duration).length() == 1) ? ("0" + this.df.format(duration)) : this.df.format(duration));
    }
}
