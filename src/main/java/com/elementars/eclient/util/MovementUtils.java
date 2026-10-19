package com.elementars.eclient.util;

import net.minecraft.client.player.LocalPlayer;

public class MovementUtils
{
    public static double getSpeed(final LocalPlayer player) {
        return Math.sqrt(player.getDeltaMovement().x * player.getDeltaMovement().x + player.getDeltaMovement().z * player.getDeltaMovement().z);
    }

    public static void setSpeed(final LocalPlayer player, final double speed) {
        player.setDeltaMovement(-Math.sin(Math.toRadians(player.getYRot())) * speed, player.getDeltaMovement().y, Math.cos(Math.toRadians(player.getYRot())) * speed);
    }
}
