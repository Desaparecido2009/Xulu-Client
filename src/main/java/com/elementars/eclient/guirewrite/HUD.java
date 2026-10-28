package com.elementars.eclient.guirewrite;
import com.elementars.eclient.module.core.GuiBackgroundShader;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xulu.clickgui.Panel;
import dev.xulu.clickgui.RenderMethods;
import dev.xulu.settings.ValueManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

import java.util.ArrayList;
import java.util.Collections;

public class HUD extends Screen
{
    public static ArrayList<Frame> frames;
    public static ArrayList<Frame> rframes;
    public Panel hudPanel;
    public ValueManager setmgr;

    public static ArrayList<Frame> getFrames() {
        return HUD.frames;
    }

    public static Frame getframeByName(final String in) {
        for (final Frame f : getFrames()) {
            if (f.title.equalsIgnoreCase(in)) {
                return f;
            }
        }
        return null;
    }

    public static void registerElements() {
        for (final Module m : Xulu.MODULE_MANAGER.getModules()) {
            if (m instanceof Element) {
                ((Element)m).registerFrame();
            }
        }
    }

    public void refreshPanel() {
        this.hudPanel = new Panel("Elements", 10, 10, true) {
            @Override
            public void setupItems() {
                for (final Module m : Xulu.MODULE_MANAGER.getModules()) {
                    if (!m.getCategory().equals(Category.HUD) && !(m instanceof Element)) {
                        continue;
                    }
                    this.addButton(new HudElementButton(m, this));
                }
            }
        };
    }

    public HUD() {
        super(new TextComponent("HUD"));
        this.setmgr = Xulu.VALUE_MANAGER;
        HUD.frames = new ArrayList<Frame>();
        final double pwidth = 80.0;
        final double pheight = 15.0;
        final double px = 10.0;
        final double py = 10.0;
        final double pyplus = pheight + 10.0;
        HUD.frames.add(new Frame("PvPInfo", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("Totems", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("Obsidian", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("Crystals", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("Gapples", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("InvPreview", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("TextRadar", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("Position", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("FeatureList", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("Player", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("Welcome", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("OldName", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("TheGoons", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("Potions", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("StickyNotes", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("Exp", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("HoleHud", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("Info", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("Armor", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("CraftingPreview", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("GodInfo", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("Watermark", 2.0, 2.0, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("Logo", px, py, pwidth, pheight, false, this));
        HUD.frames.add(new Frame("Target", px, py, pwidth, pheight, false, this));
        HUD.rframes = new ArrayList<Frame>();
        for (final Frame f : HUD.frames) {
            HUD.rframes.add(f);
        }
        Collections.reverse(HUD.rframes);
        this.hudPanel = new Panel("Elements", 10, 10, true) {
            @Override
            public void setupItems() {
                for (final Module m : Xulu.MODULE_MANAGER.getModules()) {
                    if (!m.getCategory().equals(Category.HUD) && !(m instanceof Element)) {
                        continue;
                    }
                    this.addButton(new HudElementButton(m, this));
                }
            }
        };
    }

    @Override
    public void render(final PoseStack poseStack, final int mouseX, final int mouseY, final float partialTicks) {
        // Render shader background if enabled
        if (!GuiBackgroundShader.renderBackground(this.width, this.height, partialTicks)) {
            this.renderBackground(poseStack);
        }
        final float midX = this.width / 2.0f;
        final float midY = this.height / 2.0f;
        RenderMethods.drawRect(0.0f, midY, (float)this.width, midY + 1.0f, -1);
        RenderMethods.drawRect(midX, 0.0f, midX + 1.0f, (float)this.height, -1);
        this.hudPanel.drawScreen(mouseX, mouseY, partialTicks);
        for (final Frame f : HUD.frames) {
            f.drawScreen(mouseX, mouseY, partialTicks);
        }
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton) {
        this.hudPanel.mouseClicked((int)mouseX, (int)mouseY, mouseButton);
        for (final Frame f : HUD.frames) {
            if (f.mouseClicked((int)mouseX, (int)mouseY, mouseButton)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int state) {
        this.hudPanel.mouseReleased((int)mouseX, (int)mouseY, state);
        for (final Frame p : HUD.rframes) {
            p.mouseReleased((int)mouseX, (int)mouseY, state);
        }
        return super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double scrollDelta) {
        final int scrollAmount = 5;
        if (scrollDelta > 0) {
            this.hudPanel.setY(Math.max(0, this.hudPanel.getY() + scrollAmount));
        }
        if (scrollDelta < 0) {
            this.hudPanel.setY(Math.max(0, this.hudPanel.getY() - scrollAmount));
        }
        return super.mouseScrolled(mouseX, mouseY, scrollDelta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
