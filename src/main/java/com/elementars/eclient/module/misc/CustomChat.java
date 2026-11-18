package com.elementars.eclient.module.misc;

import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventSendPacket;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;
import net.minecraft.network.protocol.game.ServerboundChatPacket;

public class CustomChat extends Module
{
    private final Value<Boolean> commands;
    private final Value<Boolean> mode;
    private final String suffix1 = " \u23d0 \u166d \u144c \u14aa \u144c";
    private final String suffix2 = " | X U L U";

    public CustomChat() {
        super("CustomChat", "Appends XULU to the end of your chat messages", 0, Category.MISC, true);
        this.commands = this.register(new Value<Boolean>("Commands", this, false));
        this.mode = this.register(new Value<Boolean>("2b2t Mode", this, false));
    }

    @EventTarget
    public void onPacket(final EventSendPacket event) {
        if (!(event.getPacket() instanceof ServerboundChatPacket)) {
            return;
        }
        String s = ((ServerboundChatPacket)event.getPacket()).getMessage();
        if (s.startsWith("/") && !this.commands.getValue()) {
            return;
        }
        s += (this.mode.getValue() ? this.suffix2 : this.suffix1);
        if (s.length() >= 256) {
            s = s.substring(0, 256);
        }
        event.setPacket(new ServerboundChatPacket(s));
    }
}
