package com.elementars.eclient.command.commands;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.command.Command;
import com.elementars.eclient.module.Module;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BindCommand extends Command
{
    private static final Map<String, String> ALIASES;

    static {
        ALIASES = new HashMap<String, String>();
        ALIASES.put("lshift", "left.shift");
        ALIASES.put("rshift", "right.shift");
        ALIASES.put("lcontrol", "left.control");
        ALIASES.put("lctrl", "left.control");
        ALIASES.put("rcontrol", "right.control");
        ALIASES.put("rctrl", "right.control");
        ALIASES.put("lmenu", "left.alt");
        ALIASES.put("lalt", "left.alt");
        ALIASES.put("rmenu", "right.alt");
        ALIASES.put("ralt", "right.alt");
        ALIASES.put("lwin", "left.win");
        ALIASES.put("rwin", "right.win");
        ALIASES.put("return", "enter");
        ALIASES.put("escape", "escape");
        ALIASES.put("back", "backspace");
        ALIASES.put("del", "delete");
        ALIASES.put("ins", "insert");
        ALIASES.put("pageup", "page.up");
        ALIASES.put("pagedown", "page.down");
        ALIASES.put("capslock", "caps.lock");
        ALIASES.put("numlock", "num.lock");
        ALIASES.put("scrolllock", "scroll.lock");
        ALIASES.put("grave", "grave.accent");
        ALIASES.put("bracketleft", "left.bracket");
        ALIASES.put("bracketright", "right.bracket");
        ALIASES.put("backslash", "backslash");
        ALIASES.put("semicolon", "semicolon");
        ALIASES.put("apostrophe", "apostrophe");
        ALIASES.put("capital", "caps.lock");
        ALIASES.put("scroll", "scroll.lock");
        ALIASES.put("numpadenter", "keypad.enter");
        ALIASES.put("numpadadd", "keypad.add");
        ALIASES.put("numpadsubtract", "keypad.subtract");
        ALIASES.put("numpadmultiply", "keypad.multiply");
        ALIASES.put("numpaddivide", "keypad.divide");
        ALIASES.put("numpaddecimal", "keypad.decimal");
        ALIASES.put("numpadequal", "keypad.equal");
        for (int i = 0; i <= 9; ++i) {
            ALIASES.put("numpad" + i, "keypad." + i);
        }
    }

    public BindCommand() {
        super("bind", "binds a module to a key", new String[] { "bind <module> <key>" });
    }

    @Override
    public void execute(final String[] args) {
        if (args.length < 2) {
            Command.sendChatMessage("Please specify which module you want bound");
            return;
        }
        if (args.length < 3) {
            Command.sendChatMessage("Please specify the key you would like to bind");
            return;
        }
        final Module module = Xulu.MODULE_MANAGER.getModuleByName(args[1]);
        if (module == null) {
            Command.sendChatMessage("Module not found.");
            return;
        }
        if (args[2].equalsIgnoreCase("none")) {
            module.setKey(0);
            Command.sendChatMessage(module.getDisplayName() + " unbound");
            return;
        }
        final InputConstants.Key key = parseKey(args[2]);
        if (key == InputConstants.UNKNOWN) {
            Command.sendChatMessage("Unknown key: " + args[2]);
            return;
        }
        module.setKey(key.getValue());
        Command.sendChatMessage(module.getDisplayName() + " bound to " + key.getDisplayName().getString());
    }

    private static InputConstants.Key parseKey(final String input) {
        String name = input.toLowerCase(Locale.ROOT);
        final String alias = ALIASES.get(name);
        if (alias != null) {
            name = alias;
        }
        final InputConstants.Key key = InputConstants.getKey("key.keyboard." + name);
        if (key != InputConstants.UNKNOWN) {
            return key;
        }
        try {
            final int code = Integer.parseInt(input);
            if (code < 0) {
                return InputConstants.UNKNOWN;
            }
            return InputConstants.Type.KEYSYM.getOrCreate(code);
        }
        catch (NumberFormatException ignored) {
        }
        return InputConstants.UNKNOWN;
    }
}
