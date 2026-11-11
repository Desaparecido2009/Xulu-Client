package com.elementars.eclient.command;

import com.elementars.eclient.module.core.Global;
import com.elementars.eclient.util.ColorTextUtils;
import com.elementars.eclient.util.Wrapper;
import net.minecraft.network.chat.TextComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Command
{
    protected String name;
    protected String description;
    protected String[] syntax;
    private static String prefix;

    public Command(final String name, final String description, final String[] syntax) {
        this.name = name;
        this.description = description;
        this.syntax = syntax;
        Command.prefix = ".";
    }

    public static String[] getBrackets(final String type) {
        if (type.equalsIgnoreCase("[]")) {
            return new String[] { "[", "]" };
        }
        if (type.equalsIgnoreCase("<>")) {
            return new String[] { "<", ">" };
        }
        if (type.equalsIgnoreCase("()")) {
            return new String[] { "(", ")" };
        }
        if (type.equalsIgnoreCase("{}")) {
            return new String[] { "{", "}" };
        }
        if (type.equalsIgnoreCase("-==-")) {
            return new String[] { "-=", "=-" };
        }
        return new String[] { "[", "]" };
    }

    public static void sendChatMessage(final String message) {
        sendRawChatMessage(ColorTextUtils.getColor(Global.command2.getValue()) + getBrackets(Global.command3.getValue())[0] + ColorTextUtils.getColor(Global.command1.getValue()) + "Xulu" + ColorTextUtils.getColor(Global.command2.getValue()) + getBrackets(Global.command3.getValue())[1] + " &r" + message);
    }

    public static void sendStringChatMessage(final String[] messages) {
        sendChatMessage("");
        for (final String s : messages) {
            sendRawChatMessage(s);
        }
    }

    public static void sendRawChatMessage(final String message) {
        Wrapper.getMinecraft().gui.getChat().addMessage(new TextComponent(translateAmpersandCodes(message)));
    }

    private static String translateAmpersandCodes(final String text) {
        final Pattern p = Pattern.compile("&[0123456789abcdefrlosmk]");
        final Matcher m = p.matcher(text);
        final StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "\u00a7" + m.group().substring(1));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public void execute(final String[] args) {
    }

    public void syntaxCheck(final String[] args) {
        if (args.length > 1 && args[1].equalsIgnoreCase("help")) {
            this.showSyntax(args[0]);
            return;
        }
        this.execute(args);
    }

    public static void setPrefix(final String in) {
        Command.prefix = in;
    }

    public static String getPrefix() {
        return Command.prefix;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String[] getSyntax() {
        return this.syntax;
    }

    public void showSyntax(final String command) {
        sendChatMessage("Options for " + command);
        if (this.syntax.length == 0) {
            sendChatMessage("No options for this command.");
            return;
        }
        for (final String arg : this.syntax) {
            sendChatMessage(" - " + arg);
        }
    }

    public static char SECTIONSIGN() {
        return '\u00a7';
    }
}
