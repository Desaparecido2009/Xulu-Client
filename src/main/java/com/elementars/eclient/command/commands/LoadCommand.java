package com.elementars.eclient.command.commands;

import com.elementars.eclient.command.Command;
import dev.xulu.settings.ConfigManager;

import java.util.Arrays;

public class LoadCommand extends Command
{
    public LoadCommand() {
        super("load", "Loads a named config", new String[] { "load <name>" });
    }

    @Override
    public void execute(final String[] args) {
        final String name = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "main";
        if (!ConfigManager.exists(name)) {
            Command.sendChatMessage("Config " + ConfigManager.normalizeName(name) + " does not exist");
            return;
        }
        if (ConfigManager.load(name)) {
            Command.sendChatMessage("Loaded config " + ConfigManager.normalizeName(name));
        }
        else {
            Command.sendChatMessage("Could not load config " + ConfigManager.normalizeName(name));
        }
    }
}
