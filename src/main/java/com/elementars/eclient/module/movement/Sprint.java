package com.elementars.eclient.module.movement;

import com.elementars.eclient.event.Event;
import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.PlayerMoveEvent;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.MathUtil;
import dev.xulu.settings.Value;

public class Sprint extends Module
{
    private final Value<String> mode;

    public Sprint() {
        super("Sprint", "Sprints", 0, Category.MOVEMENT, true);
        this.mode = this.register(new Value<String>("Mode", this, "Legit", new String[] { "Legit", "Rage" }));
    }

    @Override
    public void onUpdate() {
        if (Sprint.mc.player == null) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Legit") && Sprint.mc.screen == null && Sprint.mc.options.keyUp.isDown() && !Sprint.mc.player.isShiftKeyDown() && !Sprint.mc.player.isUsingItem() && !Sprint.mc.player.horizontalCollision && Sprint.mc.player.getFoodData().getFoodLevel() > 6) {
            Sprint.mc.player.setSprinting(true);
        }
    }

    @EventTarget
    public void onMove(final PlayerMoveEvent event) {
        if (event.getEventState() != Event.State.PRE) {
            return;
        }
        if (Sprint.mc.player == null) {
            return;
        }
        if ((Sprint.mc.options.keyUp.isDown() || Sprint.mc.options.keyDown.isDown() || Sprint.mc.options.keyLeft.isDown() || Sprint.mc.options.keyRight.isDown()) && !Sprint.mc.player.isShiftKeyDown() && !Sprint.mc.player.horizontalCollision && Sprint.mc.player.getFoodData().getFoodLevel() > 6) {
            Sprint.mc.player.setSprinting(true);
            final double[] dir = MathUtil.directionSpeed(0.20000000298023224);
            event.setX(dir[0]);
            event.setZ(dir[1]);
        }
    }
}
