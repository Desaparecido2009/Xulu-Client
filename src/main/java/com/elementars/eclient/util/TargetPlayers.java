package com.elementars.eclient.util;

import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

public class TargetPlayers
{
    public static ConcurrentHashMap<String, Integer> targettedplayers = new ConcurrentHashMap<String, Integer>();

    public static void onAttack(final AttackEntityEvent event) {
        if (event.getTarget() instanceof Player) {
            if (((Player)event.getTarget()).getHealth() == 0.0f) {
                return;
            }
            TargetPlayers.targettedplayers.put(event.getTarget().getName().getString(), 20);
        }
    }

    public static void onUpdate() {
        TargetPlayers.targettedplayers.forEach((name, timeout) -> {
            if (timeout <= 0) {
                TargetPlayers.targettedplayers.remove(name);
            }
            else {
                TargetPlayers.targettedplayers.put(name, timeout - 1);
            }
        });
    }

    public static void addTargetedPlayer(final String name) {
        if (name.equalsIgnoreCase(Minecraft.getInstance().player.getName().getString())) {
            return;
        }
        TargetPlayers.targettedplayers.put(name, 20);
    }
}
