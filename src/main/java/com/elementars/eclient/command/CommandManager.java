package com.elementars.eclient.command;

import com.elementars.eclient.command.commands.AboutCommand;
import com.elementars.eclient.command.commands.AntiVoidCommand;
import com.elementars.eclient.command.commands.BindCommand;
import com.elementars.eclient.command.commands.CreditsCommand;
import com.elementars.eclient.command.commands.CustomFontCommand;
import com.elementars.eclient.command.commands.DrawnCommand;
import com.elementars.eclient.command.commands.EnemyCommand;
import com.elementars.eclient.command.commands.FakePlayerCommand;
import com.elementars.eclient.command.commands.FriendCommand;
import com.elementars.eclient.command.commands.HelpCommand;
import com.elementars.eclient.command.commands.LoadCommand;
import com.elementars.eclient.command.commands.PrefixCommand;
import com.elementars.eclient.command.commands.SaveCommand;
import com.elementars.eclient.command.commands.SetCommand;
import com.elementars.eclient.command.commands.SetStringCommand;
import com.elementars.eclient.command.commands.ToggleCommand;
import com.elementars.eclient.command.commands.YawCommand;
import com.elementars.eclient.util.Wrapper;
import java.util.ArrayList;

public class CommandManager
{
    public static ArrayList<Command> commands;
    public static ArrayList<String> rcommands;

    public void init() {
        CommandManager.commands.add(new AboutCommand());
        CommandManager.commands.add(new CreditsCommand());
        CommandManager.commands.add(new HelpCommand());
        CommandManager.commands.add(new BindCommand());
        CommandManager.commands.add(new ToggleCommand());
        CommandManager.commands.add(new SetCommand());
        CommandManager.commands.add(new SetStringCommand());
        CommandManager.commands.add(new DrawnCommand());
        CommandManager.commands.add(new PrefixCommand());
        CommandManager.commands.add(new FriendCommand());
        CommandManager.commands.add(new EnemyCommand());
        CommandManager.commands.add(new CustomFontCommand());
        CommandManager.commands.add(new YawCommand());
        CommandManager.commands.add(new AntiVoidCommand());
        CommandManager.commands.add(new FakePlayerCommand());
        CommandManager.commands.add(new SaveCommand());
        CommandManager.commands.add(new LoadCommand());
    }

    public static void runCommand(final String message) {
        final String trimmed = message.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        final String[] args = trimmed.split("\\s+");
        for (int i = 0; i < args.length; ++i) {
            if (args[i] != null) {
                args[i] = args[i].replaceAll("<>", " ");
            }
        }
        Command target = null;
        for (final Command command : CommandManager.commands) {
            if (command.getName().equalsIgnoreCase(args[0])) {
                target = command;
                break;
            }
        }
        if (target == null) {
            Command.sendChatMessage("Command not found. Try " + Command.getPrefix() + "help for a list of commands");
            return;
        }
        try {
            Wrapper.getMinecraft().gui.getChat().addRecentChat(Command.getPrefix() + trimmed);
            target.syntaxCheck(args);
        }
        catch (Exception e) {
            e.printStackTrace();
            Command.sendChatMessage("Error occured when running command!");
        }
    }

    public static ArrayList<Command> getCommands() {
        return CommandManager.commands;
    }

    static {
        CommandManager.commands = new ArrayList<Command>();
        CommandManager.rcommands = new ArrayList<String>();
    }
}
