package com.elementars.eclient.module.combat;

import com.elementars.eclient.command.Command;
import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventReceivePacket;
import com.elementars.eclient.event.events.EventTotemPop;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.ColorTextUtils;
import com.elementars.eclient.util.TargetPlayers;
import dev.xulu.settings.Value;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.ConcurrentHashMap;

public class PopCounter extends Module
{
    public ConcurrentHashMap<Player, Integer> popMap;
    private final Value<Boolean> onlyTargets;
    private final Value<Boolean> chat;
    private final Value<Boolean> watermark;
    private final Value<String> color;
    private final Value<String> ncolor;
    public static PopCounter INSTANCE;

    public PopCounter() {
        super("PopCounter", "Counts how many times your enemy pops", 0, Category.COMBAT, true);
        this.popMap = new ConcurrentHashMap<Player, Integer>();
        this.onlyTargets = this.register(new Value<Boolean>("Only Targets", this, true));
        this.chat = this.register(new Value<Boolean>("Send Message", this, true));
        this.watermark = this.register(new Value<Boolean>("Watermark", this, true));
        this.color = this.register(new Value<String>("Color", this, "White", ColorTextUtils.colors));
        this.ncolor = this.register(new Value<String>("Number Color", this, "White", ColorTextUtils.colors));
        PopCounter.INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if (PopCounter.mc.level == null) {
            return;
        }
        for (final Player player : PopCounter.mc.level.players()) {
            if (player.getHealth() == 0.0f && this.popMap.containsKey(player)) {
                if (this.chat.getValue()) {
                    this.sendChatMessage(player.getName().getString() + " has died!");
                }
                this.popMap.remove(player);
            }
        }
    }

    @EventTarget
    public void onPop(final EventTotemPop event) {
        if (TargetPlayers.targettedplayers.containsKey(event.getPlayer().getName().getString()) || !this.onlyTargets.getValue()) {
            final int pops = this.popMap.getOrDefault(event.getPlayer(), 0) + 1;
            if (this.chat.getValue()) {
                this.sendChatMessage(event.getPlayer().getName().getString() + " has popped " + ColorTextUtils.getColor(this.ncolor.getValue()) + pops + ColorTextUtils.getColor(this.color.getValue()) + " times!");
            }
            this.popMap.put(event.getPlayer(), pops);
        }
    }

    @EventTarget
    public void onPacket(final EventReceivePacket event) {
        if (PopCounter.mc.level == null) {
            return;
        }
        if (event.getPacket() instanceof ClientboundEntityEventPacket) {
            final ClientboundEntityEventPacket packet = (ClientboundEntityEventPacket)event.getPacket();
            if (packet.getEventId() == 35 && packet.getEntity(PopCounter.mc.level) instanceof Player) {
                final Player player = (Player)packet.getEntity(PopCounter.mc.level);
                new EventTotemPop(player).call();
            }
        }
    }

    public void sendChatMessage(final String message) {
        if (this.watermark.getValue()) {
            Command.sendChatMessage(ColorTextUtils.getColor(this.color.getValue()) + message);
        }
        else {
            Command.sendRawChatMessage(ColorTextUtils.getColor(this.color.getValue()) + message);
        }
    }
}
