package com.elementars.eclient.module.combat;

import com.elementars.eclient.command.Command;
import com.elementars.eclient.friend.Friends;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.ColorTextUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xulu.settings.Value;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DurabilityAlert extends Module
{
    private final ConcurrentHashMap<String, Integer> players;
    private final Value<String> mode;
    private final Value<Boolean> ignoreself;
    private final Value<Boolean> ignorefriends;
    private final Value<Boolean> watermark;
    private final Value<String> color;

    public DurabilityAlert() {
        super("DurabilityAlert", "Alerts when someone has low durability", 0, Category.COMBAT, true);
        this.players = new ConcurrentHashMap<String, Integer>();
        this.mode = this.register(new Value<String>("Mode", this, "Chat", new String[] { "Chat", "Notification" }));
        this.ignoreself = this.register(new Value<Boolean>("Ignore Self", this, false));
        this.ignorefriends = this.register(new Value<Boolean>("Ignore Friends", this, false));
        this.watermark = this.register(new Value<Boolean>("Watermark", this, true));
        this.color = this.register(new Value<String>("Color", this, "White", ColorTextUtils.colors));
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        this.players.clear();
    }

    @Override
    public void onUpdate() {
        if (DurabilityAlert.mc.level == null || DurabilityAlert.mc.player == null) {
            return;
        }
        for (final Player player : DurabilityAlert.mc.level.players()) {
            if (player == null) {
                continue;
            }
            if (this.ignoreself.getValue() && player == DurabilityAlert.mc.player) {
                continue;
            }
            final String name = player.getName().getString();
            if (this.ignorefriends.getValue() && Friends.isFriend(name)) {
                continue;
            }
            for (final ItemStack stack : player.getArmorSlots()) {
                if (stack == null || stack.isEmpty() || !stack.isDamageableItem()) {
                    continue;
                }
                final double ratio = (double)stack.getDamageValue() / (double)stack.getMaxDamage();
                if (ratio > 0.75 && !this.players.containsKey(name)) {
                    if (this.mode.getValue().equalsIgnoreCase("Chat")) {
                        final String message = ColorTextUtils.getColor(this.color.getValue()) + name + " has low durability!";
                        if (this.watermark.getValue()) {
                            Command.sendChatMessage(message);
                        }
                        else {
                            Command.sendRawChatMessage(message);
                        }
                    }
                    this.players.put(name, 1500);
                    break;
                }
            }
        }
        this.players.replaceAll((name, timeout) -> timeout - 1);
        this.players.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }

    @SubscribeEvent
    public void onOverlay(final RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        if (!this.mode.getValue().equalsIgnoreCase("Notification") || DurabilityAlert.mc.player == null || this.players.isEmpty()) {
            return;
        }
        final int width = DurabilityAlert.mc.getWindow().getGuiScaledWidth();
        final int height = DurabilityAlert.mc.getWindow().getGuiScaledHeight();
        int yCount = (int)(height / 2.0 - height / 2.0 * 0.9) - DurabilityAlert.mc.font.lineHeight / 2;
        final PoseStack poseStack = event.getMatrixStack();
        for (final Map.Entry<String, Integer> entry : this.players.entrySet()) {
            final Integer timeout = entry.getValue();
            if (timeout == null || timeout <= 1000) {
                continue;
            }
            final String plain = entry.getKey() + " has low durability!";
            final String text = "\u00a7" + ColorTextUtils.getColor(this.color.getValue()).substring(1) + plain;
            DurabilityAlert.mc.font.drawShadow(poseStack, text, width / 2.0f - DurabilityAlert.mc.font.width(plain) / 2.0f, (float)yCount, 0xFFFFFFFF);
            yCount += 10;
        }
    }
}