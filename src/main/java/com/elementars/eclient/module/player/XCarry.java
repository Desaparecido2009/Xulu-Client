package com.elementars.eclient.module.player;

import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.CloseInventoryEvent;
import com.elementars.eclient.event.events.EventSendPacket;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;

public class XCarry extends Module
{
    public XCarry() {
        super("XCarry", "Holds things in your crafting menu", 0, Category.PLAYER, true);
    }

    @EventTarget
    public void onPacket(final EventSendPacket event) {
        if (event.getPacket() instanceof ServerboundContainerClosePacket) {
            final ServerboundContainerClosePacket packet = (ServerboundContainerClosePacket)event.getPacket();
            event.setCancelled(packet.getContainerId() == 0);
        }
    }

    @EventTarget
    public void onClose(final CloseInventoryEvent event) {
        event.setCancelled(true);
    }
}
