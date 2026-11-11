package com.elementars.eclient.module.render;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.Color;

public class AntiFog extends Module
{
    private final Value<Boolean> rainbow;
    private final Value<Float> r;
    private final Value<Float> g;
    private final Value<Float> b;
    private final Value<Float> r1;
    private final Value<Float> g1;
    private final Value<Float> b1;
    private final Value<Float> r2;
    private final Value<Float> g2;
    private final Value<Float> b2;
    private final Value<Boolean> clear;
    private final Value<Boolean> color;

    public AntiFog() {
        super("AntiFog", "Prevents fog", 0, Category.RENDER, true);
        this.rainbow = this.register(new Value<Boolean>("Rainbow", this, false));
        this.r = this.register(new Value<Float>("Red", this, 1.0f, 0.0f, 1.0f));
        this.g = this.register(new Value<Float>("Green", this, 1.0f, 0.0f, 1.0f));
        this.b = this.register(new Value<Float>("Blue", this, 1.0f, 0.0f, 1.0f));
        this.r1 = this.register(new Value<Float>("Nether Red", this, 1.0f, 0.0f, 1.0f));
        this.g1 = this.register(new Value<Float>("Nether Green", this, 1.0f, 0.0f, 1.0f));
        this.b1 = this.register(new Value<Float>("Nether Blue", this, 1.0f, 0.0f, 1.0f));
        this.r2 = this.register(new Value<Float>("End Red", this, 1.0f, 0.0f, 1.0f));
        this.g2 = this.register(new Value<Float>("End Green", this, 1.0f, 0.0f, 1.0f));
        this.b2 = this.register(new Value<Float>("End Blue", this, 1.0f, 0.0f, 1.0f));
        this.clear = this.register(new Value<Boolean>("Remove fog", this, true));
        this.color = this.register(new Value<Boolean>("Color fog", this, true));
    }

    @Override
    public void onEnable() {
        AntiFog.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        AntiFog.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onRenderFog(final EntityViewRenderEvent.RenderFogEvent event) {
        if (this.clear.getValue()) {
            event.setNearPlaneDistance(Float.MAX_VALUE);
            event.setFarPlaneDistance(Float.MAX_VALUE);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onFogColor(final EntityViewRenderEvent.FogColors event) {
        if (this.color.getValue()) {
            if (this.rainbow.getValue()) {
                final Color color = new Color(Xulu.rgb);
                event.setRed(color.getRed() / 255.0f);
                event.setGreen(color.getGreen() / 255.0f);
                event.setBlue(color.getBlue() / 255.0f);
            }
            else if (AntiFog.mc.player != null && AntiFog.mc.level != null) {
                if (AntiFog.mc.level.dimension() == Level.NETHER) {
                    event.setRed(this.r1.getValue());
                    event.setGreen(this.g1.getValue());
                    event.setBlue(this.b1.getValue());
                }
                else if (AntiFog.mc.level.dimension() == Level.END) {
                    event.setRed(this.r2.getValue());
                    event.setGreen(this.g2.getValue());
                    event.setBlue(this.b2.getValue());
                }
                else {
                    event.setRed(this.r.getValue());
                    event.setGreen(this.g.getValue());
                    event.setBlue(this.b.getValue());
                }
            }
        }
    }
}
