package com.elementars.eclient.util;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventReceivePacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;

public class LagUtil
{
    public static LagUtil INSTANCE;
    private long timeLastTimeUpdate;

    public LagUtil() {
        this.timeLastTimeUpdate = -1L;
        Xulu.EVENT_MANAGER.register(this);
    }

    public long getLastTimeDiff() {
        if (this.timeLastTimeUpdate != -1L) {
            return System.currentTimeMillis() - this.timeLastTimeUpdate;
        }
        return 0L;
    }

    @EventTarget
    public void onPacketPreceived(final EventReceivePacket event) {
        if (event.getPacket() instanceof ClientboundSetTimePacket) {
            this.timeLastTimeUpdate = System.currentTimeMillis();
            LagUtil.INSTANCE.timeLastTimeUpdate = this.timeLastTimeUpdate;
        }
    }
}
