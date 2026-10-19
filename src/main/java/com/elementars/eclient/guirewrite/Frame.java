package com.elementars.eclient.guirewrite;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.module.ModuleManager;
import com.elementars.eclient.util.Wrapper;
import dev.xulu.clickgui.RenderMethods;

import java.awt.Color;

public class Frame
{
    public String title;
    public double x;
    public double y;
    public final double defaultX;
    public final double defaultY;
    private double x2;
    private double y2;
    public double width;
    public double height;
    public boolean dragging;
    public boolean pinned;
    public boolean visible;
    public HUD hud;

    public Frame(final String ititle, final double ix, final double iy, final double iwidth, final double iheight, final boolean ipinned, final HUD hud) {
        this.title = ititle;
        this.x = ix;
        this.y = iy;
        this.defaultX = ix;
        this.defaultY = iy;
        this.width = iwidth;
        this.height = iheight;
        this.pinned = ipinned;
        this.dragging = false;
        this.visible = true;
        this.hud = hud;
        this.setup();
    }

    public void setup() {
    }

    int changeAlpha(int origColor, final int userInputedAlpha) {
        origColor &= 0xFFFFFF;
        return userInputedAlpha << 24 | origColor;
    }

    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        if (!(this.visible = ModuleManager.isModuleEnabled(this.title))) {
            return;
        }
        if (this.dragging) {
            this.x = this.x2 + mouseX;
            this.y = this.y2 + mouseY;
            final int scaledWidth = Wrapper.getMinecraft().getWindow().getGuiScaledWidth();
            final int scaledHeight = Wrapper.getMinecraft().getWindow().getGuiScaledHeight();
            if (this.x < 0.0) {
                this.x = 0.0;
            }
            if (this.y < 0.0) {
                this.y = 0.0;
            }
            if (this.x > scaledWidth - this.width) {
                this.x = scaledWidth - this.width;
            }
            if (this.y > scaledHeight - this.height) {
                this.y = scaledHeight - this.height;
            }
        }
        if (Xulu.MODULE_MANAGER.getModuleByName(this.title) != null) {
            ((Element)Xulu.MODULE_MANAGER.getModuleByName(this.title)).x = this.x;
            ((Element)Xulu.MODULE_MANAGER.getModuleByName(this.title)).y = this.y;
        }
        if (this.dragging) {
            RenderMethods.drawRect((float)this.x, (float)this.y, (float)(this.x + this.width), (float)(this.y + this.height), this.changeAlpha(Color.lightGray.getRGB(), 100));
            RenderMethods.drawRect((float)(this.x + 4.0), (float)(this.y + 2.0), (float)(this.x + 4.3), (float)(this.y + this.height - 2.0), this.changeAlpha(Color.lightGray.getRGB(), 100));
        }
        else {
            RenderMethods.drawRect((float)this.x, (float)this.y, (float)(this.x + this.width), (float)(this.y + this.height), this.changeAlpha(-15592942, 100));
            RenderMethods.drawRect((float)(this.x + 4.0), (float)(this.y + 2.0), (float)(this.x + 4.3), (float)(this.y + this.height - 2.0), this.changeAlpha(-5592406, 100));
        }
        if (Xulu.MODULE_MANAGER.getModuleByName(this.title) != null) {
            Xulu.MODULE_MANAGER.getModuleByName(this.title).onRender();
        }
    }

    public boolean mouseClicked(final int mouseX, final int mouseY, final int mouseButton) {
        if (!this.visible) {
            return false;
        }
        if (mouseButton == 0 && this.isHovered(mouseX, mouseY)) {
            this.x2 = this.x - mouseX;
            this.y2 = this.y - mouseY;
            return this.dragging = true;
        }
        if (mouseButton == 2 && this.isHovered(mouseX, mouseY)) {
            ((Element)Xulu.MODULE_MANAGER.getModuleByName(this.title)).onMiddleClick();
        }
        return false;
    }

    public void mouseReleased(final int mouseX, final int mouseY, final int state) {
        if (!this.visible) {
            return;
        }
        if (state == 0) {
            this.dragging = false;
        }
    }

    public boolean isHovered(final int mouseX, final int mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
    }
}
