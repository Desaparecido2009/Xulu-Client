package com.elementars.eclient.util;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import dev.xulu.settings.FileManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.client.Minecraft;

public interface Helper
{
    public static final Minecraft mc = Minecraft.getInstance();
    public static final Level world = Minecraft.getInstance().level;
    public static final LocalPlayer playerSP = Minecraft.getInstance().player;
    public static final Font fontRenderer = Minecraft.getInstance().font;
    public static final FileManager fileManager = Wrapper.getFileManager();
    public static final IEventBus EVENT_BUS = MinecraftForge.EVENT_BUS;
}
