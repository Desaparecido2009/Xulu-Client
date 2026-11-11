package com.elementars.eclient.util;

import net.minecraft.world.level.Level;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.Minecraft;
import dev.xulu.settings.FileManager;

public class Wrapper
{
    public static FileManager fileManager;

    public static Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    public static LocalPlayer getPlayer() {
        return getMinecraft().player;
    }

    public static Level getWorld() {
        return getMinecraft().level;
    }

    public static FileManager getFileManager() {
        if (Wrapper.fileManager == null) {
            Wrapper.fileManager = new FileManager();
        }
        return Wrapper.fileManager;
    }
}
