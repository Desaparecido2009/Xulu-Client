package com.elementars.eclient.command.commands;

import com.elementars.eclient.command.Command;
import com.elementars.eclient.util.Wrapper;
import net.minecraft.client.player.LocalPlayer;

public class YawCommand extends Command
{
    public YawCommand() {
        super("setyaw", "Sets the yaw of the player", new String[0]);
    }

    @Override
    public void execute(final String[] args) {
        if (args.length < 2) {
            Command.sendChatMessage("Please specify the yaw!");
            return;
        }
        final LocalPlayer player = Wrapper.getPlayer();
        if (player == null) {
            Command.sendChatMessage("You are not in a world!");
            return;
        }
        final float yaw;
        try {
            yaw = Float.parseFloat(args[1]);
        }
        catch (NumberFormatException e) {
            Command.sendChatMessage("Invalid yaw: " + args[1]);
            return;
        }
        player.setYRot(yaw);
        player.setYHeadRot(yaw);
    }
}
