package com.elementars.eclient.command.commands;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.command.Command;
import com.elementars.eclient.module.Module;

public class DrawnCommand extends Command
{
    public DrawnCommand() {
        super("drawn", "toggles if a module is drawn on array list", new String[0]);
    }

    @Override
    public void execute(final String[] args) {
        if (args.length < 2) {
            Command.sendChatMessage("Please specify which module you want drawn/undrawn");
            return;
        }
        final Module module = Xulu.MODULE_MANAGER.getModuleByName(args[1]);
        if (module == null) {
            Command.sendChatMessage("Module not found.");
            return;
        }
        module.setDrawn(!module.isDrawn());
        Command.sendChatMessage(module.getDisplayName() + (module.isDrawn() ? " drawn" : " undrawn"));
    }
}
