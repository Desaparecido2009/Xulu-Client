package com.elementars.eclient.module.combat;

import com.elementars.eclient.command.Command;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.ColorTextUtils;
import dev.xulu.settings.Value;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class StrengthDetect extends Module
{
    private static final List<Player> strPlayers = new ArrayList<Player>();
    private final Value<Boolean> watermark;
    private final Value<String> color;

    public StrengthDetect() {
        super("StrengthDetect", "Detects when someone has strength", 0, Category.COMBAT, true);
        this.watermark = this.register(new Value<Boolean>("Watermark", this, true));
        this.color = this.register(new Value<String>("Color", this, "White", ColorTextUtils.colors));
    }

    @Override
    public void onEnable() {
        strPlayers.clear();
    }

    @Override
    public void onDisable() {
        strPlayers.clear();
    }

    @Override
    public void onUpdate() {
        if (StrengthDetect.mc.level == null || StrengthDetect.mc.player == null) {
            return;
        }
        for (final Player player : StrengthDetect.mc.level.players()) {
            if (player == null || player == StrengthDetect.mc.player) {
                continue;
            }
            if (!player.isAlive()) {
                strPlayers.remove(player);
                continue;
            }
            boolean hasStrength = false;
            for (final MobEffectInstance effect : player.getActiveEffects()) {
                if (effect.getEffect() == MobEffects.DAMAGE_BOOST) {
                    hasStrength = true;
                    break;
                }
            }
            final String name = player.getName().getString();
            if (hasStrength && !strPlayers.contains(player)) {
                strPlayers.add(player);
                this.sendMessage(ColorTextUtils.getColor(this.color.getValue()) + name + " has drank a strength pot!");
            }
            else if (!hasStrength && strPlayers.remove(player)) {
                this.sendMessage(ColorTextUtils.getColor(this.color.getValue()) + name + " no longer has strength!");
            }
        }
        strPlayers.removeIf(player -> !player.isAlive() || player.level != StrengthDetect.mc.level);
    }

    private void sendMessage(final String message) {
        if (this.watermark.getValue()) {
            Command.sendChatMessage(message);
        }
        else {
            Command.sendRawChatMessage(message);
        }
    }
}
