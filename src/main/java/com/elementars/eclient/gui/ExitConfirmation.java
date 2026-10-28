package com.elementars.eclient.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

public final class ExitConfirmation
{
    private static boolean confirmed;

    private ExitConfirmation() {
    }

    public static boolean shouldConfirm(final Minecraft mc) {
        return !confirmed && mc != null && mc.level != null && mc.player != null;
    }

    public static void show(final Minecraft mc) {
        GLFW.glfwSetWindowShouldClose(mc.getWindow().getWindow(), false);
        if (mc.screen instanceof XuluExitScreen) {
            return;
        }
        final Screen previous = mc.screen;
        mc.setScreen(new XuluExitScreen(previous));
    }

    public static void confirm() {
        confirmed = true;
        Minecraft.getInstance().stop();
    }

    public static void cancel(final Screen previous) {
        Minecraft.getInstance().setScreen(previous);
    }

    private static final class XuluExitScreen extends ConfirmScreen
    {
        private XuluExitScreen(final Screen previous) {
            super(result -> {
                if (result) {
                    ExitConfirmation.confirm();
                }
                else {
                    ExitConfirmation.cancel(previous);
                }
            }, new TextComponent("Xulu"), new TextComponent("¿Estás seguro que quieres forzar la salida? Podría corromper tu configuración. Se recomienda salir desde el botón original de Minecraft."), new TextComponent("Salir de todos modos"), new TextComponent("Cancelar"));
        }
    }
}
