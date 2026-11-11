package com.elementars.eclient.command.commands;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.command.Command;
import com.elementars.eclient.font.CFontManager;

import java.util.Arrays;

public class CustomFontCommand extends Command
{
    public CustomFontCommand() {
        super("customfont", "Tweaks the custom font", new String[] { "default", "defaultxdolf", "defaultinfo", "currentfont", "currentfontxdolf", "fonts", "setfont <font> <size>", "setfontxdolf <font> <size>" });
    }

    @Override
    public void execute(final String[] args) {
        if (args.length < 2) {
            Command.sendChatMessage("Do " + Command.getPrefix() + "customfont help for options");
            return;
        }
        if (args[1].equalsIgnoreCase("default")) {
            Xulu.setcFontRendererDefault();
            Command.sendChatMessage("Set the default custom font.");
        }
        else if (args[1].equalsIgnoreCase("defaultxdolf")) {
            Xulu.setXdolfFontRendererDefault();
            Command.sendChatMessage("Set the default Xdolf font.");
        }
        else if (args[1].equalsIgnoreCase("defaultinfo")) {
            Command.sendChatMessage("Font: Verdana, Size: 18");
        }
        else if (args[1].equalsIgnoreCase("currentfont")) {
            if (CFontManager.customFont == null) {
                Xulu.setcFontRendererDefault();
            }
            if (CFontManager.customFont == null) {
                Command.sendChatMessage("Custom font is unavailable.");
                return;
            }
            Command.sendChatMessage("Font: " + CFontManager.customFont.getFont().getFontName() + ", Size: " + CFontManager.customFont.getFont().getSize());
        }
        else if (args[1].equalsIgnoreCase("currentfontxdolf")) {
            if (CFontManager.xFontRenderer == null) {
                Xulu.setXdolfFontRendererDefault();
            }
            if (CFontManager.xFontRenderer == null) {
                Command.sendChatMessage("Xdolf font is unavailable.");
                return;
            }
            Command.sendChatMessage("Font: " + CFontManager.xFontRenderer.getFontName() + ", Size: " + CFontManager.xFontRenderer.getSize());
        }
        else if (args[1].equalsIgnoreCase("fonts")) {
            final String[] fonts = Xulu.getFonts();
            if (fonts.length == 0) {
                Command.sendChatMessage("No fonts found.");
                return;
            }
            Command.sendChatMessage("Fonts:");
            Command.sendChatMessage(String.join(", ", fonts));
        }
        else if (args[1].equalsIgnoreCase("setfont")) {
            this.setFont(args, false);
        }
        else if (args[1].equalsIgnoreCase("setfontxdolf")) {
            this.setFont(args, true);
        }
        else {
            Command.sendChatMessage("Unknown option " + args[1] + ". Do " + Command.getPrefix() + "customfont help for options");
        }
    }

    private void setFont(final String[] args, final boolean xdolf) {
        if (args.length < 3) {
            Command.sendChatMessage("Specify your font!");
            return;
        }
        if (args.length < 4) {
            Command.sendChatMessage("Specify your font size!");
            return;
        }
        final int size;
        try {
            size = Integer.parseInt(args[args.length - 1]);
        }
        catch (NumberFormatException e) {
            Command.sendChatMessage("Invalid font size: " + args[args.length - 1]);
            return;
        }
        if (size <= 0 || size > 200) {
            Command.sendChatMessage("Font size must be between 1 and 200!");
            return;
        }
        final String font = String.join(" ", Arrays.copyOfRange(args, 2, args.length - 1));
        if (xdolf) {
            Xulu.setXdolfFontRenderer(font, size);
        }
        else {
            Xulu.setCFontRenderer(font, size);
        }
    }
}
