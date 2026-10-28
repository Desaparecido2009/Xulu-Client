package com.elementars.eclient.command.commands;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.command.Command;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;

import java.util.Arrays;

public class SetStringCommand extends Command
{
    public SetStringCommand() {
        super("setstring", "Sets a string easier", new String[] { "setstring <module> <setting> <value>" });
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void execute(final String[] args) {
        if (args.length < 2) {
            Command.sendChatMessage("Please specify a module");
            return;
        }
        if (args.length < 3) {
            Command.sendChatMessage("Please specify which setting you would like to change");
            return;
        }
        final Module module = Xulu.MODULE_MANAGER.getModuleByName(args[1]);
        if (module == null) {
            Command.sendChatMessage("Module not found!");
            return;
        }
        final Value setting = Xulu.VALUE_MANAGER.getValueByMod(module, args[2]);
        if (setting == null) {
            Command.sendChatMessage("Setting not found!");
            return;
        }
        if (!setting.isMode()) {
            Command.sendChatMessage(setting.getName() + " is not a text setting!");
            return;
        }
        if (args.length < 4) {
            Command.sendChatMessage("Please specify the text you would like to set");
            return;
        }
        final String input = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        final String option = setting.getCorrectString(input);
        if (option == null) {
            Command.sendChatMessage("Option " + input + " not found! Options: " + setting.getOptions());
            return;
        }
        setting.setValue(option);
        Command.sendChatMessage("Set " + setting.getName() + " to " + option.toUpperCase());
    }
}
