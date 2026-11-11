package com.elementars.eclient.module.misc;

import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventReceivePacket;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;

public class Time extends Module
{
    long oldTime;
    private final Value<Long> time;

    public Time() {
        super("Time", "Clientside sets the time", 0, Category.MISC, true);
        this.time = this.register(new Value<Long>("Time", this, 0L, 0L, 24000L));
    }

    @EventTarget
    public void onTime(final EventReceivePacket event) {
        if (event.getPacket() instanceof ClientboundSetTimePacket) {
            this.oldTime = ((ClientboundSetTimePacket)event.getPacket()).getDayTime();
            event.setCancelled(true);
        }
    }

    @Override
    public void onUpdate() {
        if (Time.mc.level == null) {
            return;
        }
        Time.mc.level.setDayTime((long)this.time.getValue());
    }

    @Override
    public void onEnable() {
        this.oldTime = Time.mc.level.getDayTime();
    }

    @Override
    public void onDisable() {
        Time.mc.level.setDayTime(this.oldTime);
    }
}
