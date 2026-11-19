package com.elementars.eclient.module.misc;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;

public class Avoid extends Module
{
    public static Value<Boolean> cactus;
    public static Value<Boolean> fire;
    public static Value<Boolean> lava;
    public static Value<Boolean> webs;
    private static boolean active;

    public Avoid() {
        super("Avoid", "Avoids interactions with certain things", 0, Category.MISC, true);
        Avoid.cactus = this.register(new Value<Boolean>("Cactus", this, true));
        Avoid.fire = this.register(new Value<Boolean>("Fire", this, true));
        Avoid.lava = this.register(new Value<Boolean>("Lava", this, true));
        Avoid.webs = this.register(new Value<Boolean>("Webs", this, true));
    }

    public static boolean isActive() {
        return Avoid.active;
    }

    @Override
    public void onEnable() {
        Avoid.active = true;
    }

    @Override
    public void onDisable() {
        Avoid.active = false;
    }
}
