package com.elementars.eclient.module.combat;

import com.elementars.eclient.command.Command;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.ColorTextUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xulu.settings.Value;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BreakAlert extends Module
{
    private final Value<String> mode;
    private final Value<Boolean> ignoreSelf;
    private final Value<Boolean> watermark;
    private final Value<String> color;
    private final Set<BlockPos> breaking;
    private final Set<BlockPos> test;
    private boolean testy;
    private int delay;

    public BreakAlert() {
        super("BreakAlert", "Alerts you when your feet blocks are being broken", 0, Category.COMBAT, true);
        this.mode = this.register(new Value<String>("Mode", this, "Chat", new ArrayList<String>(Arrays.asList("Chat", "Text", "Dot"))));
        this.ignoreSelf = this.register(new Value<Boolean>("Ignore Self", this, true));
        this.watermark = this.register(new Value<Boolean>("Watermark", this, true));
        this.color = this.register(new Value<String>("Color", this, "White", ColorTextUtils.colors));
        this.breaking = new HashSet<BlockPos>();
        this.test = new HashSet<BlockPos>();
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        this.breaking.clear();
        this.test.clear();
        this.testy = false;
        this.delay = 0;
    }

    @Override
    public void onUpdate() {
        if (this.delay > 0) {
            --this.delay;
        }
        this.test.clear();
        if (BreakAlert.mc.level == null || BreakAlert.mc.player == null) {
            return;
        }
        for (final Player player : BreakAlert.mc.level.players()) {
            if (player == null) {
                continue;
            }
            if (this.ignoreSelf.getValue() && player == BreakAlert.mc.player) {
                continue;
            }
            if (!player.swinging) {
                continue;
            }
            final ItemStack held = player.getMainHandItem();
            if (held.getItem() != Items.DIAMOND_PICKAXE && held.getItem() != Items.NETHERITE_PICKAXE) {
                continue;
            }
            final HitResult ray = player.pick(5.0, BreakAlert.mc.getFrameTime(), false);
            if (ray.getType() != HitResult.Type.BLOCK) {
                continue;
            }
            final BlockPos hit = ((BlockHitResult)ray).getBlockPos();
            if (BreakAlert.mc.level.getBlockState(hit).getBlock() == Blocks.BEDROCK) {
                continue;
            }
            this.test.add(hit);
        }
        this.breaking.removeIf(pos -> !this.test.contains(pos));
        this.breaking.addAll(this.test);
        this.testy = false;
        final BlockPos feet = BreakAlert.mc.player.blockPosition();
        if (!this.breaking.isEmpty()) {
            if (this.breaking.contains(feet)
                    || this.breaking.contains(feet.north()) || this.breaking.contains(feet.south())
                    || this.breaking.contains(feet.east()) || this.breaking.contains(feet.west())) {
                this.testy = true;
            }
        }
        if (this.testy && this.mode.getValue().equalsIgnoreCase("Chat")) {
            if (this.delay == 0) {
                final String message = ColorTextUtils.getColor(this.color.getValue()) + "Your feet are being mined!";
                if (this.watermark.getValue()) {
                    Command.sendChatMessage(message);
                }
                else {
                    Command.sendRawChatMessage(message);
                }
            }
            this.delay = 100;
        }
    }

    @SubscribeEvent
    public void onOverlay(final RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL || !this.testy) {
            return;
        }
        final PoseStack poseStack = event.getMatrixStack();
        final int x = BreakAlert.mc.getWindow().getGuiScaledWidth() / 2;
        final int y = BreakAlert.mc.getWindow().getGuiScaledHeight() / 2;
        if (this.mode.getValue().equalsIgnoreCase("Dot")) {
            GuiComponent.fill(poseStack, x - 3, y - 3, x + 4, y + 4, 0xFFFF0000);
        }
        else if (this.mode.getValue().equalsIgnoreCase("Text")) {
            final String text = "\u00a7" + ColorTextUtils.getColor(this.color.getValue()).substring(1) + "Your feet are being mined!";
            BreakAlert.mc.font.drawShadow(poseStack, text, x - BreakAlert.mc.font.width(text) / 2.0f, y - BreakAlert.mc.font.lineHeight / 2.0f, 0xFFFFFFFF);
        }
    }
}