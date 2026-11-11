package com.elementars.eclient.command.commands;

import com.elementars.eclient.command.Command;
import com.elementars.eclient.debug.FakePlayer;
import com.elementars.eclient.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.Difficulty;

import java.util.List;

public class FakePlayerCommand extends Command
{
    public FakePlayerCommand() {
        super("fakeplayer", "spawns client-side fake players for testing modules", new String[] { "add [name]", "del [name]", "list", "tp" });
    }

    @Override
    public void execute(final String[] args) {
        if (args.length < 2) {
            Command.sendChatMessage("Try .fakeplayer add, .fakeplayer del, .fakeplayer list or .fakeplayer tp");
            return;
        }
        final Minecraft minecraft = Wrapper.getMinecraft();
        if (minecraft.player == null || minecraft.level == null) {
            Command.sendChatMessage("Join a world first!");
            return;
        }
        if (args[1].equalsIgnoreCase("add")) {
            final String name = (args.length > 2) ? args[2] : ("Dummy" + (FakePlayer.getActive().size() + 1));
            if (FakePlayer.getByName(name) != null) {
                Command.sendChatMessage(name + " already exists!");
                return;
            }
            if (FakePlayer.add(name) == null) {
                Command.sendChatMessage("Could not spawn " + name);
                return;
            }
            Command.sendChatMessage("Spawned " + name + " (&cclient-side only, the server cannot see it&r)");
            if (minecraft.level.getDifficulty() == Difficulty.PEACEFUL) {
                Command.sendChatMessage("&7Note: difficulty is Peaceful - damage-based modules (e.g. AutoCrystal) compute 0 damage on Peaceful, same as the original client. Set Easy or higher.");
            }
        }
        else if (args[1].equalsIgnoreCase("del")) {
            if (args.length < 3 || args[2].equalsIgnoreCase("all")) {
                final int count = FakePlayer.removeAll();
                Command.sendChatMessage(count == 0 ? "No fake players to remove" : ("Removed " + count + " fake player(s)"));
                return;
            }
            final FakePlayer fake = FakePlayer.getByName(args[2]);
            if (fake == null) {
                Command.sendChatMessage(args[2] + " isn't a fake player");
                return;
            }
            FakePlayer.remove(fake);
            Command.sendChatMessage("Removed " + args[2]);
        }
        else if (args[1].equalsIgnoreCase("list")) {
            final List<FakePlayer> fakePlayers = FakePlayer.getActive();
            if (fakePlayers.isEmpty()) {
                Command.sendChatMessage("No fake players spawned");
                return;
            }
            for (final FakePlayer fake : fakePlayers) {
                Command.sendChatMessage(fake.getName().getString() + " &7(" + String.format("%.1f", minecraft.player.distanceTo(fake)) + "m away)");
            }
        }
        else if (args[1].equalsIgnoreCase("tp")) {
            final List<FakePlayer> fakePlayers = FakePlayer.getActive();
            if (fakePlayers.isEmpty()) {
                Command.sendChatMessage("No fake players spawned");
                return;
            }
            for (final FakePlayer fake : fakePlayers) {
                FakePlayer.teleport(fake, minecraft.player);
            }
            Command.sendChatMessage("Moved " + fakePlayers.size() + " fake player(s) in front of you");
        }
        else {
            Command.sendChatMessage("Unknown attribute '" + args[1] + "'");
        }
    }
}
