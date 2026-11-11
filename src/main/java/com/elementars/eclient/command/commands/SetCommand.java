package com.elementars.eclient.command.commands;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.command.Command;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;

import java.util.ArrayList;
import java.util.Arrays;

public class SetCommand extends Command
{
    public SetCommand() {
        super("set", "Sets the settings of a module", new String[] { "set <module> <setting> <value>" });
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
        if (args.length < 4) {
            Command.sendChatMessage("Please enter a value you would like to set");
            return;
        }
        final Module module = Xulu.MODULE_MANAGER.getModuleByName(args[1]);
        if (module == null) {
            Command.sendChatMessage("Module not found!");
            return;
        }
        Value setting = null;
        final ArrayList<Value<?>> values = Xulu.VALUE_MANAGER.getValuesByMod(module);
        if (values != null) {
            for (final Value<?> value : values) {
                if (value.getName().equalsIgnoreCase(args[2])) {
                    setting = value;
                    break;
                }
            }
        }
        if (setting == null) {
            Command.sendChatMessage("Setting not found!");
            return;
        }
        final String input = String.join(" ", Arrays.copyOfRange(args, 3, args.length)).trim();
        try {
            if (setting.isToggle()) {
                final Boolean value = parseBoolean(input);
                if (value == null) {
                    Command.sendChatMessage("Value must be true or false!");
                    return;
                }
                setting.setValue(value);
            }
            else if (setting.isMode() || setting.isEnum()) {
                final Object option = findOption(setting, input);
                if (option == null) {
                    Command.sendChatMessage("Option " + input + " not found! Options: " + setting.getOptions());
                    return;
                }
                setting.setValue(option);
            }
            else if (setting.isNumber()) {
                if (!setNumber(setting, input)) {
                    return;
                }
            }
            else {
                Command.sendChatMessage(setting.getName() + " is not a toggle, mode or number setting!");
                return;
            }
        }
        catch (NumberFormatException e) {
            Command.sendChatMessage("Invalid value: " + input);
            return;
        }
        Command.sendChatMessage("Set " + setting.getName() + " to " + input.toUpperCase());
    }

    private static Boolean parseBoolean(final String input) {
        if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("on") || input.equals("1")) {
            return Boolean.TRUE;
        }
        if (input.equalsIgnoreCase("false") || input.equalsIgnoreCase("off") || input.equals("0")) {
            return Boolean.FALSE;
        }
        return null;
    }

    private static Object findOption(final Value<?> setting, final String input) {
        if (setting.getOptions() == null) {
            return null;
        }
        for (final Object option : setting.getOptions()) {
            if (option.toString().equalsIgnoreCase(input)) {
                return option;
            }
        }
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static boolean setNumber(final Value setting, final String input) {
        final Object current = setting.getValue();
        if (!(current instanceof Number)) {
            Command.sendChatMessage("Setting is not a valid number!");
            return false;
        }
        final double parsed = Double.parseDouble(input);
        final Object min = setting.getMin();
        final Object max = setting.getMax();
        if ((min instanceof Number && parsed < ((Number)min).doubleValue()) || (max instanceof Number && parsed > ((Number)max).doubleValue())) {
            Command.sendChatMessage("Value must be between " + min + " and " + max + "!");
            return false;
        }
        if (current instanceof Integer) {
            setting.setValue(Integer.valueOf(input));
        }
        else if (current instanceof Float) {
            setting.setValue(Float.valueOf(input));
        }
        else if (current instanceof Double) {
            setting.setValue(Double.valueOf(parsed));
        }
        else if (current instanceof Long) {
            setting.setValue(Long.valueOf(input));
        }
        else if (current instanceof Short) {
            setting.setValue(Short.valueOf(input));
        }
        else if (current instanceof Byte) {
            setting.setValue(Byte.valueOf(input));
        }
        else {
            Command.sendChatMessage("UNKNOWN NUMBER VALUE");
            return false;
        }
        return true;
    }
}
