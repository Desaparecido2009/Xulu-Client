package com.elementars.eclient.module.render;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;

import java.awt.Color;

public class EnchantColor extends Module
{
    private final Value<String> mode;
    private final Value<Integer> red;
    private final Value<Integer> green;
    private final Value<Integer> blue;

    public EnchantColor() {
        super("EnchantColor", "Changes the color of the enchantment effect", 0, Category.RENDER, true);
        this.mode = this.register(new Value<String>("Mode", this, "Color", new String[] { "Color", "Rainbow" }));
        this.red = this.register(new Value<Integer>("Red", this, 255, 0, 255));
        this.green = this.register(new Value<Integer>("Green", this, 255, 0, 255));
        this.blue = this.register(new Value<Integer>("Blue", this, 255, 0, 255));
    }

    public static Color getColor(final long offset, final float fade) {
        final EnchantColor module = (Xulu.MODULE_MANAGER == null) ? null : Xulu.MODULE_MANAGER.getModuleT(EnchantColor.class);
        if (module == null) {
            return Color.WHITE;
        }
        if (module.mode.getValue().equalsIgnoreCase("Color")) {
            return new Color(module.red.getValue(), module.green.getValue(), module.blue.getValue());
        }
        final float hue = (System.nanoTime() + offset) / 1.0E10f % 1.0f;
        final Color c = new Color(Color.HSBtoRGB(hue, 1.0f, 1.0f));
        return new Color(c.getRed() / 255.0f * fade, c.getGreen() / 255.0f * fade, c.getBlue() / 255.0f * fade, 1.0f);
    }
}
