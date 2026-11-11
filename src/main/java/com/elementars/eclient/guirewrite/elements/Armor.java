package com.elementars.eclient.guirewrite.elements;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.guirewrite.Element;
import com.elementars.eclient.util.ColorUtils;
import com.elementars.eclient.util.ColourHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xulu.settings.Value;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.item.ItemStack;

public class Armor extends Element
{
    private final Value<String> aligned;
    private final Value<Boolean> progress;
    private final Value<Integer> w;
    private final Value<Integer> h;
    private final Value<Boolean> cf;
    private final Value<Boolean> damage;
    private final Value<Boolean> fixed;

    public Armor() {
        super("Armor");
        this.aligned = this.register(new Value<String>("Aligned", this, "X-axis", new String[] { "X-axis", "Y-axis" })).onChanged(value -> this.updateSize());
        this.progress = this.register(new Value<Boolean>("Durab. Bar", this, false));
        this.w = this.register(new Value<Integer>("Width", this, 0, 0, 100)).onChanged(value -> this.updateSize());
        this.h = this.register(new Value<Integer>("Height", this, 0, 0, 100)).onChanged(value -> this.updateSize());
        this.cf = this.register(new Value<Boolean>("Custom Font", this, false));
        this.damage = this.register(new Value<Boolean>("Damage", this, false));
        this.fixed = this.register(new Value<Boolean>("Fixed", this, false));
    }

    private void updateSize() {
        final boolean xAxis = this.aligned.getValue().equalsIgnoreCase("X-axis");
        final int baseWidth = xAxis ? 79 : 16;
        final int baseHeight = xAxis ? 16 : 72;
        final int customWidth = (this.w != null && this.w.getValue() > 0) ? this.w.getValue() : baseWidth;
        final int customHeight = (this.h != null && this.h.getValue() > 0) ? this.h.getValue() : baseHeight;
        this.width = customWidth;
        this.height = customHeight;
        if (this.getFrame() != null) {
            this.getFrame().width = customWidth;
            this.getFrame().height = customHeight;
        }
    }

    @Override
    public void onEnable() {
        this.updateSize();
        super.onEnable();
    }

    @Override
    public void onRender() {
        if (Armor.mc.player == null) {
            return;
        }
        final boolean xAxis = this.aligned.getValue().equalsIgnoreCase("X-axis");
        final int scaledWidth = Armor.mc.getWindow().getGuiScaledWidth();
        final int scaledHeight = Armor.mc.getWindow().getGuiScaledHeight();
        final int centerX = scaledWidth / 2;
        final int fixedY = scaledHeight - 55 - (Armor.mc.player.isInWater() ? 10 : 0);
        int iteration = 0;
        for (final ItemStack itemStack : Armor.mc.player.getInventory().armor) {
            ++iteration;
            int x = 0;
            int y = 0;
            if (xAxis) {
                x = (int)this.x - 90 + (9 - iteration) * 20 + 2 - 12;
                y = (int)this.y;
                if (this.fixed.getValue()) {
                    x = centerX - 90 + (9 - iteration) * 20 + 2;
                    y = fixedY;
                }
            }
            else {
                x = (int)this.x;
                y = (int)this.y - (iteration - 1) * 18 + 54;
                if (this.fixed.getValue()) {
                    x = centerX;
                    y = fixedY - (iteration - 1) * 18 + 54;
                }
            }
            if (this.progress.getValue()) {
                if (xAxis) {
                    this.rect(x - 1, y + 16 - 51, x + 19, y + 16, ColorUtils.changeAlpha(ColorUtils.Colors.BLACK, 60));
                }
                else {
                    this.rect(x, y, x + 69, y + 18, ColorUtils.changeAlpha(ColorUtils.Colors.BLACK, 60));
                }
                if (itemStack.isEmpty()) {
                    continue;
                }
                if (itemStack.isDamageableItem() && itemStack.getMaxDamage() > 0) {
                    final float percentBar = (itemStack.getMaxDamage() - (float)itemStack.getDamageValue()) / itemStack.getMaxDamage();
                    final int barColor = ColorUtils.changeAlpha(ColourHolder.toHex((int)((1.0f - percentBar) * 255.0f), (int)(percentBar * 255.0f), 0), 150);
                    if (xAxis) {
                        this.rect(x - 1, y + 16 - (int)(percentBar * 51.0f), x + 19, y + 16, barColor);
                    }
                    else {
                        this.rect(x, y, x + (int)(percentBar * 69.0f), y + 18, barColor);
                    }
                }
            }
            else if (itemStack.isEmpty()) {
                continue;
            }
            Armor.mc.getItemRenderer().renderAndDecorateItem(itemStack, x, y);
            Armor.mc.getItemRenderer().renderGuiItemDecorations(Armor.mc.font, itemStack, x, y);
            final String count = (itemStack.getCount() > 1) ? String.valueOf(itemStack.getCount()) : "";
            if (this.cf.getValue()) {
                Xulu.cFontRenderer.drawStringWithShadow(count, x + 19 - 2 - Xulu.cFontRenderer.getStringWidth(count), y + 9, 16777215);
            }
            else {
                Armor.fontRenderer.drawShadow(new PoseStack(), count, (float)(x + 19 - 2 - Armor.fontRenderer.width(count)), (float)(y + 9), 16777215);
            }
            if (this.damage.getValue() && itemStack.isDamageableItem() && itemStack.getMaxDamage() > 0) {
                final float green = (itemStack.getMaxDamage() - (float)itemStack.getDamageValue()) / itemStack.getMaxDamage();
                final float red = 1.0f - green;
                final int dmg = 100 - (int)(red * 100.0f);
                final int dmgColor = ColourHolder.toHex((int)(red * 255.0f), (int)(green * 255.0f), 0);
                if (xAxis) {
                    if (this.cf.getValue()) {
                        Xulu.cFontRenderer.drawStringWithShadow(String.valueOf(dmg), x + 8 - Xulu.cFontRenderer.getStringWidth(String.valueOf(dmg)) / 2, y - 11, dmgColor);
                    }
                    else {
                        Armor.fontRenderer.drawShadow(new PoseStack(), String.valueOf(dmg), (float)(x + 9 - Armor.fontRenderer.width(String.valueOf(dmg)) / 2), (float)(y - 11), dmgColor);
                    }
                }
                else if (this.cf.getValue()) {
                    Xulu.cFontRenderer.drawStringWithShadow(String.valueOf(dmg), x + 18, y + 5, dmgColor);
                }
                else {
                    Armor.fontRenderer.drawShadow(new PoseStack(), String.valueOf(dmg), (float)(x + 18), (float)(y + 5), dmgColor);
                }
            }
        }
    }

    private void rect(final int x1, final int y1, final int x2, final int y2, final int color) {
        GuiComponent.fill(new PoseStack(), Math.min(x1, x2), Math.min(y1, y2), Math.max(x1, x2), Math.max(y1, y2), color);
    }
}
