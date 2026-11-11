package com.elementars.eclient.guirewrite.elements;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.command.Command;
import com.elementars.eclient.guirewrite.Element;
import com.elementars.eclient.module.combat.PopCounter;
import com.elementars.eclient.util.ColorUtils;
import com.elementars.eclient.util.ColourHolder;
import com.elementars.eclient.util.Pair;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xulu.settings.Value;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Target extends Element
{
    private final Value<Boolean> cf;
    private Player target;
    private final BlockPos NORTH;
    private final BlockPos EAST;
    private final BlockPos SOUTH;
    private final BlockPos WEST;

    public Target() {
        super("Target");
        this.cf = this.register(new Value<Boolean>("Custom Font", this, false));
        this.NORTH = new BlockPos(0, 0, -1);
        this.EAST = new BlockPos(1, 0, 0);
        this.SOUTH = new BlockPos(0, 0, 1);
        this.WEST = new BlockPos(-1, 0, 0);
    }

    @Override
    public void onEnable() {
        this.width = 200.0;
        this.height = 100.0;
        super.onEnable();
    }

    @Override
    public void onUpdate() {
        if (Target.mc.player == null || Target.mc.level == null) {
            return;
        }
        final List<Player> players = new ArrayList<Player>(Target.mc.level.players());
        players.remove(Target.mc.player);
        players.sort(Comparator.comparingDouble(player -> Target.mc.player.distanceTo(player)));
        this.target = players.isEmpty() ? null : players.get(0);
        super.onUpdate();
    }

    public String getPing(final float ping) {
        if (ping > 200.0f) {
            return "c";
        }
        if (ping > 100.0f) {
            return "e";
        }
        return "a";
    }

    private String getDistance(final double distance) {
        if (distance < 15.0) {
            return "c";
        }
        return "a";
    }

    private boolean isBrockOrObby(final BlockPos pos) {
        return Target.mc.level.getBlockState(pos).getBlock() == Blocks.BEDROCK || Target.mc.level.getBlockState(pos).getBlock() == Blocks.OBSIDIAN;
    }

    private ItemStack holeItem(final BlockPos pos) {
        if (!this.isBrockOrObby(pos)) {
            return ItemStack.EMPTY;
        }
        final BlockState state = Target.mc.level.getBlockState(pos);
        return new ItemStack(state.getBlock().asItem());
    }

    private List<ItemStack> getNorth(final Player player) {
        final BlockPos playerPos = player.blockPosition();
        return new ArrayList<ItemStack>(Arrays.asList(this.holeItem(playerPos.offset(this.NORTH)), this.holeItem(playerPos.offset(this.EAST)), this.holeItem(playerPos.offset(this.SOUTH)), this.holeItem(playerPos.offset(this.WEST))));
    }

    private void itemrender(final List<ItemStack> items, final int x, final int y) {
        final List<Pair<Integer, Integer>> coordinates = new ArrayList<Pair<Integer, Integer>>(Arrays.asList(new Pair<Integer, Integer>(x + 16, y), new Pair<Integer, Integer>(x + 32, y + 16), new Pair<Integer, Integer>(x + 16, y + 32), new Pair<Integer, Integer>(x, y + 16)));
        for (int item = 0; item < 4; ++item) {
            Target.mc.getItemRenderer().renderAndDecorateItem(items.get(item), coordinates.get(item).getKey(), coordinates.get(item).getValue());
        }
    }

    @Override
    public void onRender() {
        if (Target.mc.player == null || Target.mc.level == null || this.target == null) {
            return;
        }
        final int x = (int)this.x;
        final int y = (int)this.y;
        GuiComponent.fill(new PoseStack(), x, y, x + (int)this.width, y + (int)this.height, ColorUtils.changeAlpha(Color.BLACK.getRGB(), 50));
        try {
            InventoryScreen.renderEntityInInventory(x + 30, y + 90, 45, 0.0f, 0.0f, this.target);
        }
        catch (Exception ignored) {
        }
        final String section = String.valueOf(Command.SECTIONSIGN());
        final String name = this.target.getName().getString();
        final int ping = (Target.mc.getConnection() != null && Target.mc.getConnection().getPlayerInfo(this.target.getUUID()) != null) ? Target.mc.getConnection().getPlayerInfo(this.target.getUUID()).getLatency() : -1;
        final String pingText = (ping < 0) ? (ChatFormatting.RED + "-1") : (section + this.getPing((float)ping) + ping);
        final int pops = (PopCounter.INSTANCE != null && PopCounter.INSTANCE.popMap.containsKey(this.target)) ? PopCounter.INSTANCE.popMap.get(this.target) : 0;
        final String popsText = (pops > 0) ? (ChatFormatting.GREEN + "" + pops + " Pops") : (ChatFormatting.RED + "0 Pops");
        final double distance = Target.mc.player.distanceTo(this.target);
        final String distanceText = section + this.getDistance(distance) + (int)distance + " blocks away";
        if (this.cf.getValue()) {
            Xulu.cFontRenderer.drawStringWithShadow(name, this.x + 62.0, this.y + 3.0, -1);
            Xulu.cFontRenderer.drawStringWithShadow(pingText + "ms", this.x + 62.0, this.y + 18.0, -1);
            Xulu.cFontRenderer.drawStringWithShadow(popsText, this.x + 62.0, this.y + 33.0, -1);
            Xulu.cFontRenderer.drawStringWithShadow(distanceText, this.x + 62.0, this.y + 48.0, -1);
        }
        else {
            Target.fontRenderer.drawShadow(new PoseStack(), name, (float)(x + 62), (float)(y + 3), -1);
            Target.fontRenderer.drawShadow(new PoseStack(), pingText + "ms", (float)(x + 62), (float)(y + 18), -1);
            Target.fontRenderer.drawShadow(new PoseStack(), popsText, (float)(x + 62), (float)(y + 33), -1);
            Target.fontRenderer.drawShadow(new PoseStack(), distanceText, (float)(x + 62), (float)(y + 48), -1);
        }
        final float health = (float)Mth.clamp(Mth.ceil(this.target.getHealth()), 0, 20);
        final float percentBar = (20.0f - health) / 20.0f;
        final float red = 1.0f - percentBar;
        GuiComponent.fill(new PoseStack(), x, y + (int)this.height - 3, x + (int)(red * this.width), y + (int)this.height, ColorUtils.changeAlpha(ColourHolder.toHex((int)(percentBar * 255.0f), (int)(red * 255.0f), 0), 200));
        this.itemrender(this.getNorth(this.target), x + (int)this.width - 52, y + 4);
        int iteration = 0;
        for (final ItemStack itemStack : this.target.getInventory().armor) {
            ++iteration;
            if (itemStack.isEmpty()) {
                continue;
            }
            final int armorX = x - 90 + (9 - iteration) * 20 + 2 - 12 + 60;
            final int armorY = y + (int)this.height - 24;
            Target.mc.getItemRenderer().renderAndDecorateItem(itemStack, armorX, armorY);
            Target.mc.getItemRenderer().renderGuiItemDecorations(Target.mc.font, itemStack, armorX, armorY);
            final String count = (itemStack.getCount() > 1) ? String.valueOf(itemStack.getCount()) : "";
            Target.fontRenderer.drawShadow(new PoseStack(), count, (float)(armorX + 19 - 2 - Target.fontRenderer.width(count)), (float)(armorY + 9), 16777215);
            if (itemStack.isDamageableItem() && itemStack.getMaxDamage() > 0) {
                final float green = (itemStack.getMaxDamage() - (float)itemStack.getDamageValue()) / itemStack.getMaxDamage();
                final float redDamage = 1.0f - green;
                final int dmg = 100 - (int)(redDamage * 100.0f);
                final int dmgColor = ColourHolder.toHex((int)(redDamage * 255.0f), (int)(green * 255.0f), 0);
                if (this.cf.getValue()) {
                    Xulu.cFontRenderer.drawStringWithShadow(String.valueOf(dmg), armorX + 8 - Xulu.cFontRenderer.getStringWidth(String.valueOf(dmg)) / 2, armorY - 11, dmgColor);
                }
                else {
                    Target.fontRenderer.drawShadow(new PoseStack(), String.valueOf(dmg), (float)(armorX + 9 - Target.fontRenderer.width(String.valueOf(dmg)) / 2), (float)(armorY - 11), dmgColor);
                }
            }
        }
    }
}
