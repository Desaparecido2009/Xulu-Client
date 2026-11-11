package com.elementars.eclient.module.misc;

import com.elementars.eclient.command.Command;
import com.elementars.eclient.event.events.RenderEvent;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.ColorTextUtils;
import com.elementars.eclient.util.EntityUtil;
import com.elementars.eclient.friend.Friends;
import dev.xulu.settings.Value;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Arrays;

public class VisualRange extends Module
{
    private final Value<Boolean> mode;
    private final Value<Boolean> sf;
    private final Value<String> message;
    private final Value<Integer> delayN;
    private final Value<Boolean> ignoreFriends;
    private final Value<Boolean> vr;
    private final Value<Boolean> watermark;
    private final Value<Boolean> color;
    private final Value<String> selectcolor;
    public ArrayList<String> names;
    public ArrayList<String> names2;
    public ArrayList<String> removal;
    private int delay;

    public VisualRange() {
        super("VisualRange", "Alerts people appearing in your visual range", 0, Category.MISC, true);
        this.mode = this.register(new Value<Boolean>("Send Message", this, false));
        this.sf = this.register(new Value<Boolean>("No Friend Send", this, false));
        this.message = this.register(new Value<String>("Message", this, "hello uwu", new ArrayList<String>(Arrays.asList("Change this in the settings"))));
        this.delayN = this.register(new Value<Integer>("Delay", this, 15, 1, 30));
        this.ignoreFriends = this.register(new Value<Boolean>("Ignore Friends", this, false));
        this.vr = this.register(new Value<Boolean>("VisualRange", this, true));
        this.watermark = this.register(new Value<Boolean>("Watermark", this, true));
        this.color = this.register(new Value<Boolean>("Color", this, false));
        this.selectcolor = this.register(new Value<String>("Color Pick", this, "LightGreen", ColorTextUtils.colors));
        this.names = new ArrayList<String>();
        this.names2 = new ArrayList<String>();
        this.removal = new ArrayList<String>();
    }

    @Override
    public void onDisable() {
        this.names.clear();
        this.names2.clear();
        this.removal.clear();
        this.delay = 0;
    }

    @Override
    public void onUpdate() {
        if (this.delay > 0) {
            --this.delay;
        }
    }

    @Override
    public void onWorldRender(final RenderEvent event) {
        if (VisualRange.mc.level == null || VisualRange.mc.player == null) {
            return;
        }
        this.names2.clear();
        for (final Entity entity : VisualRange.mc.level.entitiesForRendering()) {
            if (!(entity instanceof Player) || entity == VisualRange.mc.player || EntityUtil.isFakeLocalPlayer(entity) || !entity.isAlive()) {
                continue;
            }
            this.testName(entity);
        }
        this.testLeave();
    }

    private void testName(final Entity entity) {
        final String name = entity.getName().getString();
        this.names2.add(name);
        if (!this.names.contains(name)) {
            this.sendMessage(name);
            this.names.add(name);
        }
    }

    private void testLeave() {
        for (final String name : this.names) {
            if (!this.names2.contains(name)) {
                this.removal.add(name);
            }
        }
        for (final String name : this.removal) {
            this.names.remove(name);
        }
        this.removal.clear();
    }

    private void sendMessage(final String name) {
        if (this.mode.getValue() && this.delay == 0) {
            if (this.sf.getValue() && Friends.isFriend(name)) {
                return;
            }
            VisualRange.mc.player.chat("/msg " + name + " " + this.message.getValue());
            this.delay = this.delayN.getValue() * 20;
        }
        if (this.vr.getValue()) {
            if (this.ignoreFriends.getValue() && Friends.isFriend(name)) {
                return;
            }
            final String text = this.color.getValue()
                ? ColorTextUtils.getColor(this.selectcolor.getValue()) + name + " entered visual range"
                : name + " entered visual range";
            if (this.watermark.getValue()) {
                Command.sendChatMessage(text);
            }
            else {
                Command.sendRawChatMessage(text);
            }
        }
    }
}
