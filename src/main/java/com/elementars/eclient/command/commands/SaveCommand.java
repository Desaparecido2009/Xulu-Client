package com.elementars.eclient.command.commands;

import com.elementars.eclient.command.Command;
import dev.xulu.settings.ConfigManager;

import java.util.Arrays;

public class SaveCommand extends Command
{
    public SaveCommand() {
        super("save", "Saves the current settings to a config", new String[] { "save <name>" });
    }

    @Override
    public void execute(final String[] args) {
        final String name = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "main";
        if (ConfigManager.save(name)) {
            Command.sendChatMessage("Saved config " + ConfigManager.normalizeName(name));
        }
        else {
            Command.sendChatMessage("Could not save config " + ConfigManager.normalizeName(name));
        }
    }
}
