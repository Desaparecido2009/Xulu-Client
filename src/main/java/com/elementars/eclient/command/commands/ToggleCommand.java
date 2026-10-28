package com.elementars.eclient.command.commands;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.command.Command;
import com.elementars.eclient.module.Module;

public class ToggleCommand extends Command
{
    public ToggleCommand() {
        super("t", "Toggles modules", new String[0]);
    }

    @Override
    public void execute(final String[] args) {
        if (args.length < 2) {
            Command.sendChatMessage("Not enough arguments!");
            return;
        }
        final Module module = Xulu.MODULE_MANAGER.getModuleByName(args[1]);
        if (module == null) {
            Command.sendChatMessage("Module not found.");
            return;
        }
        module.toggle();
        Command.sendChatMessage(module.getDisplayName() + " toggled " + (module.isToggled() ? (Command.SECTIONSIGN() + "aON") : (Command.SECTIONSIGN() + "cOFF")));
    }
}
