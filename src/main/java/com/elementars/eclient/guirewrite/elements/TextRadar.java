package com.elementars.eclient.guirewrite.elements;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.command.Command;
import com.elementars.eclient.friend.Friends;
import com.elementars.eclient.guirewrite.Element;
import com.elementars.eclient.module.combat.PopCounter;
import com.elementars.eclient.module.core.Global;
import com.elementars.eclient.util.ColorTextUtils;
import com.elementars.eclient.util.ColorUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xulu.settings.Value;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;

import java.text.DecimalFormat;

public class TextRadar extends Element
{
    private final Value<Boolean> pops;
    private final Value<String> friendColor;
    private final DecimalFormat decimalFormat;

    public TextRadar() {
        super("TextRadar");
        this.pops = this.register(new Value<Boolean>("Pop Count", this, true));
        this.friendColor = this.register(new Value<String>("Friend Color", this, "LightGreen", ColorTextUtils.colors));
        this.decimalFormat = new DecimalFormat("#.#");
    }

    @Override
    public void onEnable() {
        this.width = 80.0;
        this.height = 80.0;
        super.onEnable();
    }

    @Override
    public void onRender() {
        if (TextRadar.mc.player == null || TextRadar.mc.level == null) {
            return;
        }
        float yCount = (float)this.y;
        for (final Player entityPlayer : TextRadar.mc.level.players()) {
            if (entityPlayer.getName().getString().equals(TextRadar.mc.player.getName().getString())) {
                continue;
            }
            final String name = Friends.isFriend(entityPlayer.getName().getString())
                ? (Command.SECTIONSIGN() + ColorTextUtils.getColor(this.friendColor.getValue()).substring(1) + entityPlayer.getName().getString())
                : entityPlayer.getName().getString();
            String line = ChatFormatting.GRAY + "- " + name + " " + ChatFormatting.RED + this.decimalFormat.format(entityPlayer.getHealth()) + " " + ChatFormatting.DARK_GREEN + (int)TextRadar.mc.player.distanceTo(entityPlayer);
            if (this.pops.getValue() && PopCounter.INSTANCE != null && PopCounter.INSTANCE.popMap.containsKey(entityPlayer)) {
                line = line + " " + ChatFormatting.DARK_RED + "-" + PopCounter.INSTANCE.popMap.get(entityPlayer);
            }
            if (Xulu.CustomFont) {
                Xulu.cFontRenderer.drawStringWithShadow(line, (float)this.x, yCount, ColorUtils.changeAlpha(ColorUtils.Colors.WHITE, Global.hudAlpha.getValue()));
            }
            else {
                TextRadar.fontRenderer.drawShadow(new PoseStack(), line, (float)this.x, yCount, ColorUtils.changeAlpha(ColorUtils.Colors.WHITE, Global.hudAlpha.getValue()));
            }
            yCount += 10.0f;
        }
    }
}
