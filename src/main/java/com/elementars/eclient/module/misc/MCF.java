package com.elementars.eclient.module.misc;

import com.elementars.eclient.command.Command;
import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventMiddleClick;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.friend.Friends;
import dev.xulu.settings.Value;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public class MCF extends Module
{
    private final Value<Boolean> message;

    public MCF() {
        super("MCF", "Middle Click Friends", 0, Category.MISC, true);
        this.message = this.register(new Value<Boolean>("Send Message", this, true));
    }

    @EventTarget
    public void onMiddleClick(final EventMiddleClick event) {
        if (!(MCF.mc.hitResult instanceof EntityHitResult)) {
            return;
        }
        final Entity entity = ((EntityHitResult)MCF.mc.hitResult).getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        final String name = entity.getName().getString();
        if (Friends.isFriend(name)) {
            Friends.delFriend(name);
            if (this.message.getValue()) {
                Command.sendChatMessage("&b" + name + "&r has been unfriended.");
            }
        }
        else {
            Friends.addFriend(name);
            if (this.message.getValue()) {
                Command.sendChatMessage("&b" + name + "&r has been friended.");
            }
        }
    }
}
