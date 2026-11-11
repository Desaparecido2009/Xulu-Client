package com.elementars.eclient.module.combat;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.TargetPlayers;
import dev.xulu.settings.Value;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class AutoEz extends Module
{
    private final Value<String> message;
    private final Value<Boolean> mode;
    private final Value<Boolean> autocityboss;
    private final Value<String> acmessage;
    private final Value<Boolean> acmode;
    private final Value<Boolean> autoezhelmet;
    private final Value<String> ahmessage;
    private final Value<Boolean> ahmode;
    private final ConcurrentHashMap<String, Integer> totemplayers;
    private final ConcurrentHashMap<String, Integer> helmetplayers;
    private int acdelay;
    private int ahdelay;

    public AutoEz() {
        super("AutoEZ", "Sends toxic messages for you (use NAME like in Welcome)", 0, Category.COMBAT, true);
        final ArrayList<String> dummy = new ArrayList<String>(Arrays.asList("Change this in the settings"));
        this.message = this.register(new Value<String>("Message", this, "NAME has been put in the montage", dummy));
        this.mode = this.register(new Value<Boolean>("Names", this, true));
        this.autocityboss = this.register(new Value<Boolean>("AutoCityboss", this, false));
        this.acmessage = this.register(new Value<String>("AC message", this, "NAME ez pop", dummy));
        this.acmode = this.register(new Value<Boolean>("AC Names", this, true));
        this.autoezhelmet = this.register(new Value<Boolean>("AutoEZHelmet", this, false));
        this.ahmessage = this.register(new Value<String>("AH message", this, "NAME ez helmet", dummy));
        this.ahmode = this.register(new Value<Boolean>("AH Names", this, true));
        this.totemplayers = new ConcurrentHashMap<String, Integer>();
        this.helmetplayers = new ConcurrentHashMap<String, Integer>();
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onAttack(final AttackEntityEvent event) {
        if (!this.isToggled()) {
            return;
        }
        if (event.getTarget() instanceof Player) {
            final Player target = (Player)event.getTarget();
            if (target.getHealth() <= 0.0f || target.isDeadOrDying()) {
                this.sendChatMessage(this.message.getValue(), this.mode.getValue() ? target.getName().getString() : null);
            }
        }
    }

    @SubscribeEvent
    public void onDeath(final LivingDeathEvent event) {
        if (AutoEz.mc.player == null) {
            return;
        }
        if (event.getEntityLiving() instanceof Player) {
            final Player target = (Player)event.getEntityLiving();
            final String name = target.getName().getString();
            if (TargetPlayers.targettedplayers.containsKey(name)) {
                this.sendChatMessage(this.message.getValue(), this.mode.getValue() ? name : null);
                TargetPlayers.targettedplayers.remove(name);
            }
        }
    }

    @Override
    public void onUpdate() {
        if (AutoEz.mc.player == null || AutoEz.mc.level == null) {
            return;
        }
        for (final Entity entity : AutoEz.mc.level.entitiesForRendering()) {
            if (!(entity instanceof Player)) {
                continue;
            }
            final Player player = (Player)entity;
            if (player.getHealth() > 0.0f) {
                continue;
            }
            final String name = player.getName().getString();
            if (!TargetPlayers.targettedplayers.containsKey(name)) {
                continue;
            }
            this.sendChatMessage(this.message.getValue(), this.mode.getValue() ? name : null);
            TargetPlayers.targettedplayers.remove(name);
        }
        if (this.autocityboss.getValue() && this.acdelay == 0) {
            for (final Entity entity : AutoEz.mc.level.entitiesForRendering()) {
                if (!(entity instanceof Player)) {
                    continue;
                }
                final Player player = (Player)entity;
                final String name = player.getName().getString();
                if (player.getOffhandItem().getItem() == Items.TOTEM_OF_UNDYING) {
                    this.totemplayers.put(name, 20);
                    continue;
                }
                if (TargetPlayers.targettedplayers.containsKey(name) && this.totemplayers.containsKey(name)) {
                    this.sendChatMessage(this.acmessage.getValue(), this.acmode.getValue() ? name : null);
                    TargetPlayers.targettedplayers.remove(name);
                    this.totemplayers.remove(name);
                    this.acdelay = 1500;
                    break;
                }
            }
        }
        if (this.autoezhelmet.getValue() && this.ahdelay == 0) {
            for (final Entity entity : AutoEz.mc.level.entitiesForRendering()) {
                if (!(entity instanceof Player)) {
                    continue;
                }
                final Player player = (Player)entity;
                final String name = player.getName().getString();
                boolean helmet = false;
                for (final ItemStack stack : player.getArmorSlots()) {
                    if (stack.getItem() == Items.DIAMOND_HELMET || stack.getItem() == Items.NETHERITE_HELMET) {
                        helmet = true;
                        break;
                    }
                }
                if (helmet) {
                    this.helmetplayers.put(name, 20);
                    continue;
                }
                if (TargetPlayers.targettedplayers.containsKey(name) && this.helmetplayers.containsKey(name)) {
                    this.sendChatMessage(this.ahmessage.getValue(), this.ahmode.getValue() ? name : null);
                    TargetPlayers.targettedplayers.remove(name);
                    this.helmetplayers.remove(name);
                    this.ahdelay = 1500;
                    break;
                }
            }
        }
        this.totemplayers.replaceAll((name, timeout) -> timeout - 1);
        this.totemplayers.entrySet().removeIf(entry -> entry.getValue() <= 0);
        this.helmetplayers.replaceAll((name, timeout) -> timeout - 1);
        this.helmetplayers.entrySet().removeIf(entry -> entry.getValue() <= 0);
        if (this.acdelay > 0) {
            --this.acdelay;
        }
        if (this.ahdelay > 0) {
            --this.ahdelay;
        }
    }

    private void sendChatMessage(final String message, final String name) {
        if (AutoEz.mc.player == null) {
            return;
        }
        final String text = (name == null) ? message : message.replace("NAME", name);
        AutoEz.mc.player.chat(text);
    }
}
