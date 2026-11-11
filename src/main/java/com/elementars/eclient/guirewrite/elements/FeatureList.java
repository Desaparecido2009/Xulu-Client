package com.elementars.eclient.guirewrite.elements;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.command.Command;
import com.elementars.eclient.guirewrite.Element;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.module.core.Global;
import com.elementars.eclient.util.ColorUtils;
import com.elementars.eclient.util.XuluTessellator;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xulu.newgui.util.ColorUtil;
import dev.xulu.settings.Value;
import net.minecraft.ChatFormatting;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureList extends Element
{
    public static Value<Boolean> animation = new Value<Boolean>("Animation", null, false);
    public static Value<String> type = new Value<String>("Type", null, "Both", new String[] { "Both", "Enable", "Disable" });

    private final Value<Boolean> corner;
    private final Value<Boolean> alphab;
    private final Value<Boolean> box;
    private final Value<String> boxMode;
    private final Value<String> prefix;
    private final Value<String> suffix;
    private final Value<String> mode;
    private final Value<String> order;
    private final Value<Boolean> autoAlign;
    private final Value<String> rlist;
    private final Value<String> categoryProfile;
    private final Value<Integer> rainbowspeed;
    private final Value<Integer> rspeed;
    private final Value<Integer> rsaturation;
    private final Value<Integer> rlightness;
    private final Value<String> bracketType;
    private final Value<Boolean> showHudInfo;
    private final Rainbow rainbow;
    private final Map<Module, AnimationEntry> animationMap;
    private final List<Module> removal;

    public FeatureList() {
        super("FeatureList");
        this.rainbow = new Rainbow();
        this.animationMap = new HashMap<Module, AnimationEntry>();
        this.removal = new ArrayList<Module>();
        this.corner = this.register(new Value<Boolean>("List In Corner", this, false));
        FeatureList.animation = this.register(new Value<Boolean>("Animation", this, false)).onChanged(aBoolean -> this.clearAnimations());
        FeatureList.type = this.register(new Value<String>("Type", this, "Both", new String[] { "Both", "Enable", "Disable" }));
        this.alphab = this.register(new Value<Boolean>("Alphabetical", this, false));
        this.box = this.register(new Value<Boolean>("Boxes", this, false));
        this.boxMode = this.register(new Value<String>("Box Mode", this, "Tag", new String[] { "Black", "Tag", "Outline" }));
        this.prefix = this.register(new Value<String>("Prefix", this, "None", new String[] { "None", ">", ")", "]", "}", ">(space)", "->", "-", "=", "<", "(", "[", "{" }));
        this.suffix = this.register(new Value<String>("Suffix", this, "None", new String[] { "None", ">", ")", "]", "}", "(space)<", "<-", "-", "=", "<", "(", "[", "{" }));
        this.mode = this.register(new Value<String>("Aligned", this, "Left", new ArrayList<String>(Arrays.asList("Left", "Right"))));
        this.order = this.register(new Value<String>("Ordering", this, "Up", new ArrayList<String>(Arrays.asList("Up", "Down"))));
        this.autoAlign = this.register(new Value<Boolean>("Auto Align", this, true));
        this.rlist = this.register(new Value<String>("Color Mode", this, "ClickGui", new String[] { "ClickGui", "Rainbow", "Category" }));
        this.categoryProfile = this.register(new Value<String>("Category Mode", this, "Xulu", new String[] { "Xulu", "Impact", "DotGod" }));
        this.rainbowspeed = this.register(new Value<Integer>("Rainbow Speed", this, 5, 1, 100));
        this.rspeed = this.register(new Value<Integer>("Rainbow Size", this, 2, 0, 20));
        this.rsaturation = this.register(new Value<Integer>("Rainbow Sat.", this, 255, 0, 255));
        this.rlightness = this.register(new Value<Integer>("Rainbow Light.", this, 255, 0, 255));
        this.bracketType = this.register(new Value<String>("Bracket Type", this, "[]", new String[] { "[]", "<>", "()", "{}", "-==-", "None" }));
        this.showHudInfo = this.register(new Value<Boolean>("Hud Info", this, true));
    }

    @Override
    public void onEnable() {
        this.width = 80.0;
        this.height = 80.0;
        this.animationMap.clear();
        this.removal.clear();
        for (final Module m : Xulu.MODULE_MANAGER.getModules()) {
            if (!m.isToggled() && m.inAnimation.getValue() != Module.Animation.NONE) {
                m.inAnimation.setEnumValue("NONE");
            }
        }
        super.onEnable();
    }

    private void clearAnimations() {
        if (this.animationMap == null) {
            return;
        }
        for (final Module m : Xulu.MODULE_MANAGER.getModules()) {
            if (m.inAnimation.getValue() != Module.Animation.NONE) {
                m.inAnimation.setEnumValue("NONE");
            }
        }
        this.animationMap.clear();
        this.removal.clear();
    }

    @Override
    public void onRender() {
        if (this.getFrame() == null) {
            return;
        }
        this.rainbow.updateRainbow();
        final boolean inCorner = this.corner.getValue();
        final double listX = inCorner ? (FeatureList.mc.getWindow().getGuiScaledWidth() - 3.0 - this.getFrame().width) : (this.x + 1.0);
        final double startY = inCorner ? 3.0 : (this.y + 1.0);
        final boolean rightAligned = this.isRightAligned(listX);
        final boolean growUpward = this.isGrowUpward(startY);
        final List<Module> mods = new ArrayList<Module>();
        for (final Module m : Xulu.MODULE_MANAGER.getModules()) {
            if (m.isToggledAnim() && m.isDrawn()) {
                mods.add(m);
            }
        }
        mods.sort(Comparator.comparingInt((Module m) -> -this.getStringWidth(this.getDisplay(m))));
        if (this.alphab.getValue()) {
            mods.sort(Comparator.comparing(Module::getName));
        }
        double yCount = startY;
        if (growUpward) {
            yCount += this.getFrame().height - 11.0;
        }
        float hue = this.rainbow.hue;
        for (final Module m : mods) {
            final int color = this.getColor(m, hue);
            final String display = this.getDisplay(m);
            final double width = this.getStringWidth(display);
            final double textX = this.getStaticX(listX, width, rightAligned);
            final AnimationEntry entry = this.animationMap.get(m);
            if (entry != null) {
                entry.color = color;
                if (entry.outgoing && m.isToggled()) {
                    entry.outgoing = false;
                    entry.targetX = textX;
                }
                else if (!entry.outgoing && !m.isToggled()) {
                    entry.outgoing = true;
                    entry.targetX = this.getOffscreenX(textX, width, rightAligned);
                }
            }
            else if (m.inAnimation.getValue() != Module.Animation.NONE) {
                if (m.inAnimation.getValue() == Module.Animation.ENABLE) {
                    this.animationMap.put(m, new AnimationEntry(this.getOffscreenX(textX, width, rightAligned), yCount, textX, false, color));
                }
                else {
                    this.animationMap.put(m, new AnimationEntry(textX, yCount, this.getOffscreenX(textX, width, rightAligned), true, color));
                }
            }
            else {
                if (this.box.getValue()) {
                    this.drawEntryBox(textX, yCount, width, color);
                }
                this.drawText(display, (float)textX, (float)yCount, ColorUtils.changeAlpha(color, Global.hudAlpha.getValue()));
            }
            yCount += growUpward ? -10.0 : 10.0;
            hue += this.rspeed.getValue() / 100.0f;
        }
        for (final Module m : this.animationMap.keySet()) {
            final AnimationEntry entry2 = this.animationMap.get(m);
            final String display2 = this.getDisplay(m);
            final double width2 = this.getStringWidth(display2);
            if (this.box.getValue()) {
                this.drawEntryBox(entry2.x, entry2.y, width2, entry2.color);
            }
            this.drawText(display2, (float)entry2.x, (float)entry2.y, ColorUtils.changeAlpha(entry2.color, Global.hudAlpha.getValue()));
            if (entry2.x == entry2.targetX) {
                m.inAnimation.setEnumValue("NONE");
                this.removal.add(m);
            }
            else if (entry2.x < entry2.targetX) {
                entry2.x += 1.0;
            }
            else {
                entry2.x -= 1.0;
            }
        }
        for (final Module m : this.removal) {
            this.animationMap.remove(m);
        }
        this.removal.clear();
    }

    private String getDisplay(final Module m) {
        return this.getPrefix() + m.getName() + this.getHudInfo(m) + this.getSuffix();
    }

    private String getPrefix() {
        final String value = this.prefix.getValue();
        if (value.equalsIgnoreCase("None")) {
            return "";
        }
        if (value.equalsIgnoreCase(">(space)")) {
            return "> ";
        }
        return value;
    }

    private String getSuffix() {
        final String value = this.suffix.getValue();
        if (value.equalsIgnoreCase("None")) {
            return "";
        }
        if (value.equalsIgnoreCase("(space)<")) {
            return " <";
        }
        return value;
    }

    private String getHudInfo(final Module m) {
        final String info = m.getHudInfo();
        if (info == null || !this.showHudInfo.getValue()) {
            return "";
        }
        if (this.bracketType.getValue().equalsIgnoreCase("None")) {
            return Command.SECTIONSIGN() + "7 " + Command.SECTIONSIGN() + "f" + info + ChatFormatting.RESET;
        }
        final String[] brackets = Command.getBrackets(this.bracketType.getValue());
        return Command.SECTIONSIGN() + "7 " + brackets[0] + Command.SECTIONSIGN() + "f" + info + Command.SECTIONSIGN() + "7" + brackets[1] + ChatFormatting.RESET;
    }

    private boolean isRightAligned(final double listX) {
        if (this.autoAlign.getValue()) {
            return listX + this.getFrame().width / 2.0 > FeatureList.mc.getWindow().getGuiScaledWidth() / 2.0;
        }
        return this.mode.getValue().equalsIgnoreCase("Right");
    }

    private boolean isGrowUpward(final double startY) {
        if (this.autoAlign.getValue()) {
            return startY + this.getFrame().height / 2.0 > FeatureList.mc.getWindow().getGuiScaledHeight() / 2.0;
        }
        return this.order.getValue().equalsIgnoreCase("Down");
    }

    private double getStaticX(final double listX, final double width, final boolean rightAligned) {
        if (rightAligned) {
            return listX + this.getFrame().width - 2.0 - width;
        }
        return listX + 1.0;
    }

    private double getOffscreenX(final double textX, final double width, final boolean rightAligned) {
        if (rightAligned) {
            return textX + width;
        }
        return textX - width;
    }

    private int getColor(final Module m, final float hue) {
        final String colorMode = this.rlist.getValue();
        if (colorMode.equalsIgnoreCase("ClickGui")) {
            return ColorUtil.getClickGUIColor().getRGB();
        }
        if (colorMode.equalsIgnoreCase("Category")) {
            return this.getCategoryColor(m);
        }
        return Color.HSBtoRGB(hue, this.rsaturation.getValue() / 255.0f, this.rlightness.getValue() / 255.0f);
    }

    private void drawEntryBox(final double textX, final double y, final double width, final int color) {
        final double left = textX - 2.0;
        final double top = y - 1.0;
        final double right = textX + width + 2.0;
        final double bottom = y + this.getFontHeight();
        final String boxModeValue = this.boxMode.getValue();
        if (boxModeValue.equalsIgnoreCase("Outline")) {
            XuluTessellator.drawRectDouble(left, top, right, bottom, 1427181841);
            XuluTessellator.drawRectOutline(left, top, right, bottom, 1.0, color);
            return;
        }
        XuluTessellator.drawRectDouble(left, top, right, bottom, 1427181841);
        if (boxModeValue.equalsIgnoreCase("Tag")) {
            XuluTessellator.drawRectDouble(left, top, left + 2.0, bottom, color);
        }
    }

    private void drawText(final String text, final float x, final float y, final int color) {
        if (Xulu.CustomFont) {
            Xulu.cFontRenderer.drawStringWithShadow(text, x, y, color);
        }
        else {
            FeatureList.fontRenderer.drawShadow(new PoseStack(), text, x, y, color);
        }
    }

    private int getStringWidth(final String text) {
        return Xulu.CustomFont ? Xulu.cFontRenderer.getStringWidth(text) : FeatureList.fontRenderer.width(text);
    }

    private float getFontHeight() {
        return Xulu.CustomFont ? Xulu.cFontRenderer.getHeight() : FeatureList.fontRenderer.lineHeight;
    }

    private int getCategoryColor(final Module m) {
        final String profile = this.categoryProfile.getValue();
        final Category category = m.getCategory();
        if (profile.equalsIgnoreCase("Impact")) {
            switch (category) {
                case CORE: return new Color(0, 218, 242).getRGB();
                case COMBAT: return new Color(229, 30, 16).getRGB();
                case MOVEMENT: return new Color(8, 116, 227).getRGB();
                case PLAYER: return new Color(43, 203, 55).getRGB();
                case RENDER: return new Color(227, 162, 50).getRGB();
                case MISC: return new Color(97, 30, 212).getRGB();
                case DUMMY: return new Color(241, 243, 90).getRGB();
                case HUD: return new Color(255, 0, 123).getRGB();
                default: return -1;
            }
        }
        if (profile.equalsIgnoreCase("DotGod")) {
            switch (category) {
                case CORE: return new Color(0, 218, 242).getRGB();
                case COMBAT: return new Color(39, 181, 171).getRGB();
                case MOVEMENT: return new Color(26, 84, 219).getRGB();
                case PLAYER: return new Color(219, 184, 190).getRGB();
                case RENDER: return new Color(169, 204, 83).getRGB();
                case MISC: return new Color(215, 214, 216).getRGB();
                case DUMMY: return new Color(222, 57, 209).getRGB();
                case HUD: return new Color(255, 0, 123).getRGB();
                default: return -1;
            }
        }
        switch (category) {
            case CORE: return new Color(0, 218, 242).getRGB();
            case COMBAT: return new Color(222, 57, 11).getRGB();
            case MOVEMENT: return new Color(189, 28, 173).getRGB();
            case PLAYER: return new Color(83, 219, 41).getRGB();
            case RENDER: return new Color(255, 242, 62).getRGB();
            case MISC: return new Color(255, 143, 15).getRGB();
            case DUMMY: return new Color(222, 57, 209).getRGB();
            case HUD: return new Color(255, 0, 123).getRGB();
            default: return -1;
        }
    }

    private static class AnimationEntry
    {
        private double x;
        private final double y;
        private double targetX;
        private boolean outgoing;
        private int color;

        private AnimationEntry(final double x, final double y, final double targetX, final boolean outgoing, final int color) {
            this.x = x;
            this.y = y;
            this.targetX = targetX;
            this.outgoing = outgoing;
            this.color = color;
        }
    }

    public class Rainbow
    {
        public int rgb;
        public int a;
        public int r;
        public int g;
        public int b;
        float hue;

        public Rainbow() {
            this.hue = 0.01f;
        }

        public void updateRainbow() {
            this.rgb = Color.HSBtoRGB(this.hue, FeatureList.this.rsaturation.getValue() / 255.0f, FeatureList.this.rlightness.getValue() / 255.0f);
            this.a = (this.rgb >>> 24 & 0xFF);
            this.r = (this.rgb >>> 16 & 0xFF);
            this.g = (this.rgb >>> 8 & 0xFF);
            this.b = (this.rgb & 0xFF);
            this.hue += FeatureList.this.rainbowspeed.getValue() / 10000.0f;
            if (this.hue > 1.0f) {
                --this.hue;
            }
        }
    }
}
