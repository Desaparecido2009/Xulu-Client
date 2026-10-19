package com.elementars.eclient.module.misc;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FovSlider extends Module
{
    private final Value<Integer> FOV;
    private final Value<String> mode;
    private float fov;

    public FovSlider() {
        super("FovSlider", "Better FOV slider", 0, Category.MISC, true);
        this.FOV = this.register(new Value<Integer>("FOV", this, 110, 90, 200));
        this.mode = this.register(new Value<String>("Mode", this, "Fov Changer", new String[] { "Fov Changer", "Hand Changer" }));
    }

    @SubscribeEvent
    public void fovOn(final EntityViewRenderEvent.FieldOfView e) {
        if (this.mode.getValue().equalsIgnoreCase("Hand Changer")) {
            e.setFOV((float)this.FOV.getValue());
        }
    }

    @Override
    public void onEnable() {
        FovSlider.EVENT_BUS.register((Object)this);
        this.fov = (float)FovSlider.mc.options.fov;
    }

    @Override
    public void onDisable() {
        FovSlider.EVENT_BUS.unregister((Object)this);
        FovSlider.mc.options.fov = this.fov;
    }

    @Override
    public void onUpdate() {
        if (!this.isToggled() || FovSlider.mc.level == null) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Fov Changer")) {
            FovSlider.mc.options.fov = this.FOV.getValue();
        }
    }
}
