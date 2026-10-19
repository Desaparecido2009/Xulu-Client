package com.elementars.eclient.module.movement;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.MathUtil;
import dev.xulu.settings.Value;
import net.minecraft.world.phys.Vec3;

public class FastSwim extends Module
{
    private Value<Boolean> up;
    private Value<Boolean> forward;
    private Value<Boolean> sprint;
    private Value<Boolean> only2b;
    private Value<Boolean> down;
    int divider;

    public FastSwim() {
        super("FastSwim", "Allows The Player To Swim Faster Horizontally and Vertically", 0, Category.MOVEMENT, true);
        this.divider = 5;
        this.up = this.register(new Value<Boolean>("FastSwimUp", this, true));
        this.down = this.register(new Value<Boolean>("FastSwimDown", this, true));
        this.forward = this.register(new Value<Boolean>("FastSwimForward", this, true));
        this.sprint = this.register(new Value<Boolean>("AutoSprintInLiquid", this, true));
        this.only2b = this.register(new Value<Boolean>("Only2b", this, true));
    }

    @Override
    public void onUpdate() {
        if (FastSwim.mc.player == null) {
            return;
        }
        final boolean on2b = !FastSwim.mc.isLocalServer() && FastSwim.mc.getCurrentServer() != null && FastSwim.mc.getCurrentServer().ip.equalsIgnoreCase("2b2t.org");
        if (this.only2b.getValue() && !on2b) {
            return;
        }
        final boolean inWater = FastSwim.mc.player.isInWater();
        final boolean inLava = FastSwim.mc.player.isInLava();
        if (this.sprint.getValue() && (inLava || inWater)) {
            FastSwim.mc.player.setSprinting(true);
        }
        if ((inWater || inLava) && FastSwim.mc.options.keyJump.isDown() && this.up.getValue()) {
            final Vec3 motion = FastSwim.mc.player.getDeltaMovement();
            FastSwim.mc.player.setDeltaMovement(motion.x, 0.725 / this.divider, motion.z);
        }
        if (inWater || inLava) {
            if ((this.forward.getValue() && FastSwim.mc.options.keyUp.isDown()) || FastSwim.mc.options.keyLeft.isDown() || FastSwim.mc.options.keyRight.isDown() || FastSwim.mc.options.keyDown.isDown()) {
                final double[] dir = MathUtil.directionSpeed(0.34000000298023224 / this.divider);
                final Vec3 motion = FastSwim.mc.player.getDeltaMovement();
                FastSwim.mc.player.setDeltaMovement(motion.x + dir[0], motion.y, motion.z + dir[1]);
            }
        }
        if (inWater && this.down.getValue() && FastSwim.mc.options.keyShift.isDown()) {
            final int divider2 = this.divider * -1;
            final Vec3 motion = FastSwim.mc.player.getDeltaMovement();
            FastSwim.mc.player.setDeltaMovement(motion.x, 2.2 / divider2, motion.z);
        }
        if (inLava && this.down.getValue() && FastSwim.mc.options.keyShift.isDown()) {
            final int divider2 = this.divider * -1;
            final Vec3 motion = FastSwim.mc.player.getDeltaMovement();
            FastSwim.mc.player.setDeltaMovement(motion.x, 0.91 / divider2, motion.z);
        }
    }
}
