package com.elementars.eclient.guirewrite;

import com.elementars.eclient.module.Module;
import dev.xulu.clickgui.Panel;
import dev.xulu.clickgui.item.ModuleButton;

public class HudElementButton extends ModuleButton
{
    public HudElementButton(final Module module, final Panel parent) {
        super(module, parent);
    }

    @Override
    public void toggle() {
        this.module.toggle();
    }
}
