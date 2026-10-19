package com.elementars.eclient.guirewrite.elements;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.guirewrite.Element;
import com.elementars.eclient.module.core.Global;
import com.elementars.eclient.util.ColorUtils;
import com.elementars.eclient.util.ListHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xulu.newgui.util.ColorUtil;
import dev.xulu.settings.Value;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;

public class PvPInfo extends Element
{
    private final Value<Boolean> rainbow;
    public static boolean attack;
    public static boolean place;
    public static boolean surround;
    private Vec3[] offset;

    public PvPInfo() {
        super("PvPInfo");
        this.rainbow = this.register(new Value<Boolean>("Rainbow", this, false));
        this.offset = new Vec3[] { new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), new Vec3(-1.0, 0.0, 0.0), new Vec3(0.0, 0.0, -1.0), new Vec3(0.0, -1.0, 0.0) };
    }

    public void checkSurround() {
        if (PvPInfo.mc.player == null || PvPInfo.mc.level == null) {
            return;
        }
        boolean isSafe = true;
        for (final Vec3 pos : this.offset) {
            final BlockPos bp = new BlockPos(PvPInfo.mc.player.getX(), PvPInfo.mc.player.getY(), PvPInfo.mc.player.getZ()).offset(pos.x, pos.y, pos.z);
            if (PvPInfo.mc.level.getBlockState(bp).getBlock() != Blocks.OBSIDIAN && PvPInfo.mc.level.getBlockState(bp).getBlock() != Blocks.BEDROCK) {
                isSafe = false;
            }
        }
        PvPInfo.surround = isSafe;
    }

    @Override
    public void onRender() {
        this.checkSurround();
        float yCount = (float)this.y;
        int color = ColorUtil.getClickGUIColor().getRGB();
        int totems = PvPInfo.mc.player.getInventory().items.stream().filter(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING).mapToInt(ItemStack::getCount).sum();
        if (PvPInfo.mc.player.getOffhandItem().getItem() == Items.TOTEM_OF_UNDYING) {
            ++totems;
        }
        final String[] pvpinfo = { "ATT: " + this.getFromBoolean(PvPInfo.attack), "PLC: " + this.getFromBoolean(PvPInfo.place), "FOB: " + this.getFromBoolean(PvPInfo.surround), "PING: " + this.getPing((PvPInfo.mc.getConnection() != null && PvPInfo.mc.player != null && PvPInfo.mc.getConnection().getPlayerInfo(PvPInfo.mc.player.getUUID()) != null) ? PvPInfo.mc.getConnection().getPlayerInfo(PvPInfo.mc.player.getUUID()).getLatency() : -1), "TOTEMS: " + this.getTotems(totems), "AT: " + this.getAutoTrap(), "SU: " + this.getSurround(), "CA: " + this.getCaura() };
        this.width = PvPInfo.fontRenderer.width(ListHelper.longest(pvpinfo)) + 2;
        this.height = (PvPInfo.fontRenderer.lineHeight + 1) * pvpinfo.length + 1;
        if (this.rainbow.getValue()) {
            color = new Color(Xulu.Rainbow.r, Xulu.Rainbow.g, Xulu.Rainbow.b).getRGB();
        }
        if (Xulu.CustomFont) {
            for (final String s : pvpinfo) {
                Xulu.cFontRenderer.drawStringWithShadow(s, this.x + 1.0, yCount + 1.0f, ColorUtils.changeAlpha(color, Global.hudAlpha.getValue()));
                yCount += 10.0f;
            }
        }
        else {
            for (final String s : pvpinfo) {
                PvPInfo.fontRenderer.drawShadow(new PoseStack(), s, (float)this.x + 1.0f, yCount + 1.0f, ColorUtils.changeAlpha(color, Global.hudAlpha.getValue()));
                yCount += 10.0f;
            }
        }
    }

    private String getTotems(final int totems) {
        if (totems > 0) {
            return "" + ChatFormatting.GREEN + totems;
        }
        return "" + ChatFormatting.RED + totems;
    }

    private String getPing(final long ping) {
        if (ping > 100L) {
            return "" + ChatFormatting.RED + ping;
        }
        return "" + ChatFormatting.GREEN + ping;
    }

    private String getAutoTrap() {
        if (Xulu.MODULE_MANAGER.getModuleByName("AutoTrap") == null) {
            return "NULL";
        }
        if (Xulu.MODULE_MANAGER.getModuleByName("AutoTrap").isToggled()) {
            return ChatFormatting.GREEN + "ON";
        }
        return ChatFormatting.RED + "OFF";
    }

    private String getSurround() {
        if (Xulu.MODULE_MANAGER.getModuleByName("Surround") == null) {
            return "NULL";
        }
        if (Xulu.MODULE_MANAGER.getModuleByName("Surround").isToggled()) {
            return ChatFormatting.GREEN + "ON";
        }
        return ChatFormatting.RED + "OFF";
    }

    private String getCaura() {
        boolean caura = false;
        if (Xulu.MODULE_MANAGER.getModuleByName("AutoCrystal") != null) {
            caura = Xulu.MODULE_MANAGER.getModuleByName("AutoCrystal").isToggled();
            if (caura) {
                return ChatFormatting.GREEN + "ON";
            }
        }
        if (Xulu.MODULE_MANAGER.getModuleByName("AutoCrystalO") != null) {
            caura = Xulu.MODULE_MANAGER.getModuleByName("AutoCrystalO").isToggled();
            if (caura) {
                return ChatFormatting.GREEN + "ON";
            }
        }
        if (Xulu.MODULE_MANAGER.getModuleByName("AutoCrystalX") != null) {
            caura = Xulu.MODULE_MANAGER.getModuleByName("AutoCrystalX").isToggled();
            if (caura) {
                return ChatFormatting.GREEN + "ON";
            }
        }
        return ChatFormatting.RED + "OFF";
    }

    public String getFromBoolean(final boolean b) {
        if (b) {
            return ChatFormatting.GREEN + "TRUE";
        }
        return ChatFormatting.RED + "FALSE";
    }

    static {
        PvPInfo.attack = false;
        PvPInfo.place = false;
        PvPInfo.surround = false;
    }
}
