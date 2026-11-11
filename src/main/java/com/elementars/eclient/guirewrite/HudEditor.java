package com.elementars.eclient.guirewrite;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;

public class HudEditor extends Module
{
    public HudEditor() {
        super("HudEditor", "Editor for HUD elements", 0, Category.CORE, false);
    }

    @Override
    public void setup() {
        this.setKey(0);
    }

    @Override
    public void onEnable() {
        HudEditor.mc.setScreen(Xulu.hud);
        this.toggle();
    }
}
