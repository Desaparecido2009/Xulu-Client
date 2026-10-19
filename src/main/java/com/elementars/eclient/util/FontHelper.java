package com.elementars.eclient.util;

import net.minecraft.client.Minecraft;

public class FontHelper
{
    public static int getStringWidth(final String text) {
        return Minecraft.getInstance().font.width(text);
    }

    public static int getFontHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }
}
