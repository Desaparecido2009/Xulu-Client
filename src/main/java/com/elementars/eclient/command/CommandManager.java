package com.elementars.eclient.command;

import com.elementars.eclient.command.commands.AntiVoidCommand;
import com.elementars.eclient.command.commands.EnemyCommand;
import com.elementars.eclient.command.commands.FakePlayerCommand;
import com.elementars.eclient.command.commands.FriendCommand;
import com.elementars.eclient.util.Wrapper;
import java.util.ArrayList;

public class CommandManager
{
    public static ArrayList<Command> commands;
    public static ArrayList<String> rcommands;

    public void init() {
        CommandManager.commands.add(new AntiVoidCommand());
        CommandManager.commands.add(new FakePlayerCommand());
        CommandManager.commands.add(new FriendCommand());
        CommandManager.commands.add(new EnemyCommand());
    }

    public static void runCommand(final String message) {
        final String[] args = message.split(" ");
        for (int i = 0; i < args.length; ++i) {
            if (args[i] != null) {
                args[i] = args[i].replaceAll("<>", " ");
            }
        }
        try {
            Wrapper.getMinecraft().gui.getChat().addRecentChat(Command.getPrefix() + message);
            for (final Command command : CommandManager.commands) {
                if (command.getName().equalsIgnoreCase(args[0])) {
                    command.syntaxCheck(args);
                    return;
                }
            }
        }
        catch (Exception e) {
            Command.sendChatMessage("Error occured when running command!");
        }
        Command.sendChatMessage("Command not found. Try .help for a list of commands");
    }

    public static ArrayList<Command> getCommands() {
        return CommandManager.commands;
    }

    static {
        CommandManager.commands = new ArrayList<Command>();
        CommandManager.rcommands = new ArrayList<String>();
    }
}
