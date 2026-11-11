package com.elementars.eclient.module.render;

import com.elementars.eclient.Xulu;
import dev.xulu.clickgui.ClickGui;
import com.elementars.eclient.module.Category;
import dev.xulu.settings.Value;
import com.elementars.eclient.module.Module;

public class ExeterGui extends Module
{
    private final Value<Boolean> customFont;
    private final Value<Boolean> sound;
    private final Value<Boolean> rainbow;
    private final Value<Integer> rainbowspeed;

    public ExeterGui() {
        super("ExeterGui", "Exeter Gui", 0, Category.CORE, true);
        this.customFont = this.register(new Value<Boolean>("Custom Font", this, false));
        this.sound = this.register(new Value<Boolean>("Sound", this, true));
        this.rainbow = this.register(new Value<Boolean>("Rainbow", this, false));
        this.rainbowspeed = this.register(new Value<Integer>("Rainbow Speed", this, 20, 1, 50));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        ExeterGui.mc.setScreen(ClickGui.getClickGui());
        this.toggle();
    }

    public static boolean getCF() {
        final ExeterGui module = Xulu.MODULE_MANAGER.getModuleT(ExeterGui.class);
        return module != null && module.customFont.getValue();
    }

    public static boolean getSound() {
        final ExeterGui module = Xulu.MODULE_MANAGER.getModuleT(ExeterGui.class);
        return module == null || module.sound.getValue();
    }

    public static boolean getRainbow() {
        final ExeterGui module = Xulu.MODULE_MANAGER.getModuleT(ExeterGui.class);
        return module != null && module.rainbow.getValue();
    }

    public static int getSpeed() {
        final ExeterGui module = Xulu.MODULE_MANAGER.getModuleT(ExeterGui.class);
        return (module == null) ? 20 : module.rainbowspeed.getValue();
    }
}
