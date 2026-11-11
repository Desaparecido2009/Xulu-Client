package com.elementars.eclient.module.combat;

import com.elementars.eclient.command.Command;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.ColorTextUtils;
import dev.xulu.settings.Value;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PearlAlert extends Module
{
    private final Value<Boolean> watermark;
    private final Value<String> color;
    private final ConcurrentHashMap<UUID, Integer> uuidMap = new ConcurrentHashMap<UUID, Integer>();

    public PearlAlert() {
        super("PearlAlert", "Alerts pearls thrown", 0, Category.COMBAT, true);
        this.watermark = this.register(new Value<Boolean>("Watermark", this, true));
        this.color = this.register(new Value<String>("Color", this, "White", ColorTextUtils.colors));
    }

    @Override
    public void onUpdate() {
        if (PearlAlert.mc.level == null || PearlAlert.mc.player == null) {
            return;
        }
        for (final Entity entity : PearlAlert.mc.level.entitiesForRendering()) {
            if (!(entity instanceof ThrownEnderpearl)) {
                continue;
            }
            final ThrownEnderpearl pearl = (ThrownEnderpearl)entity;
            if (this.uuidMap.containsKey(pearl.getUUID())) {
                continue;
            }
            Player thrower = (pearl.getOwner() instanceof Player) ? (Player)pearl.getOwner() : null;
            if (thrower == null) {
                double best = Double.MAX_VALUE;
                for (final Player player : PearlAlert.mc.level.players()) {
                    if (player == PearlAlert.mc.player) {
                        continue;
                    }
                    final double d = pearl.distanceTo(player);
                    if (d < best) {
                        best = d;
                        thrower = player;
                    }
                }
            }
            if (thrower == null || pearl.distanceTo(thrower) >= 3.0f) {
                continue;
            }
            this.uuidMap.put(pearl.getUUID(), 200);
            if (thrower == PearlAlert.mc.player) {
                continue;
            }
            final Vec3 motion = pearl.getDeltaMovement();
            final Direction direction = (Math.abs(motion.x) < 0.01 && Math.abs(motion.z) < 0.01)
                    ? pearl.getDirection()
                    : Direction.getNearest(motion.x, 0.0, motion.z);
            final String message = ColorTextUtils.getColor(this.color.getValue()) + thrower.getName().getString() + " threw a pearl towards " + this.getTitle(direction.getName()) + "!";
            if (this.watermark.getValue()) {
                Command.sendChatMessage(message);
            }
            else {
                Command.sendRawChatMessage(message);
            }
        }
        this.uuidMap.replaceAll((uuid, delay) -> delay - 1);
        this.uuidMap.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }

    private String getTitle(final String input) {
        return input;
    }
}