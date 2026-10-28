package com.elementars.eclient.module.render;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;

public class NoHurtCam extends Module
{
    public static NoHurtCam INSTANCE;

    public NoHurtCam() {
        super("NoHurtCam", "Disables the hurt cam", 0, Category.RENDER, true);
        NoHurtCam.INSTANCE = this;
    }

    public static boolean shouldDisable() {
        return NoHurtCam.INSTANCE != null && NoHurtCam.INSTANCE.isToggled();
    }
}
