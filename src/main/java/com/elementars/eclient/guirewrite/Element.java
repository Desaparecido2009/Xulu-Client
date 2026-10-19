package com.elementars.eclient.guirewrite;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;

public class Element extends Module
{
    Frame frame;
    protected double x;
    protected double y;
    protected double width;
    protected double height;

    public Element(final String name) {
        super(name, "NONE", 0, Category.HUD, false);
    }

    public void registerFrame() {
        this.frame = HUD.getframeByName(this.getName());
        this.width = this.frame.width;
        this.height = this.frame.height;
    }

    @Override
    public void onUpdate() {
        if (this.frame != null) {
            if (this.frame.width != this.width) {
                this.frame.width = this.width;
            }
            if (this.frame.height != this.height) {
                this.frame.height = this.height;
            }
        }
        super.onUpdate();
    }

    public void onMiddleClick() {
    }

    public Frame getFrame() {
        return this.frame;
    }

    @Override
    public void onEnable() {
        if (this.frame != null) {
            if (this.width > 0.0) {
                this.frame.width = this.width;
            }
            if (this.height > 0.0) {
                this.frame.height = this.height;
            }
            if (!this.frame.pinned) {
                this.frame.pinned = true;
            }
            if (this.frame.x == this.frame.defaultX && this.frame.y == this.frame.defaultY) {
                final int scaledWidth = Element.mc.getWindow().getGuiScaledWidth();
                final int scaledHeight = Element.mc.getWindow().getGuiScaledHeight();
                this.frame.x = Math.max(0.0, (scaledWidth - this.frame.width) / 2.0);
                this.frame.y = Math.max(0.0, (scaledHeight - this.frame.height) / 2.0);
            }
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (this.frame != null && this.frame.pinned) {
            this.frame.pinned = false;
        }
        super.onDisable();
    }

    public void setX(final double x) {
        this.x = x;
    }

    public void setY(final double y) {
        this.y = y;
    }
}
