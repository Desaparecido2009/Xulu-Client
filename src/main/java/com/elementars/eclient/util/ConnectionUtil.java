package com.elementars.eclient.util;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventPlayerConnect;
import com.elementars.eclient.event.events.EventReceivePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;

import java.util.UUID;

public class ConnectionUtil
{
    public static ConnectionUtil INSTANCE;
    private long lastPingTime;
    private int ping;

    public ConnectionUtil() {
        Xulu.EVENT_MANAGER.register(this);
    }

    public long getLastPingTime() {
        return this.lastPingTime;
    }

    public int getPing() {
        return this.ping;
    }

    public void setPing(final int ping) {
        this.lastPingTime = System.currentTimeMillis();
        this.ping = ping;
    }

    @EventTarget
    public void onPacket(final EventReceivePacket event) {
        if (!(event.getPacket() instanceof ClientboundPlayerInfoPacket)) {
            return;
        }
        final ClientboundPlayerInfoPacket packet = (ClientboundPlayerInfoPacket)event.getPacket();
        final ClientboundPlayerInfoPacket.Action action = packet.getAction();
        if (action != ClientboundPlayerInfoPacket.Action.ADD_PLAYER
            && action != ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER) {
            return;
        }
        for (final ClientboundPlayerInfoPacket.PlayerUpdate update : packet.getEntries()) {
            if (update == null || update.getProfile() == null) {
                continue;
            }
            final UUID uuid = update.getProfile().getId();
            if (uuid == null) {
                continue;
            }
            final String name = update.getProfile().getName();
            if (action == ClientboundPlayerInfoPacket.Action.ADD_PLAYER) {
                new EventPlayerConnect.Join(uuid, name).call();
            }
            else {
                new EventPlayerConnect.Leave(uuid, name).call();
            }
        }
    }
}
