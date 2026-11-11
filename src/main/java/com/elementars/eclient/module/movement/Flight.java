package com.elementars.eclient.module.movement;

import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.MotionEvent;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.MathUtil;
import dev.xulu.settings.Value;

public class Flight extends Module
{
    private Double flyHeight;
    private Value<Float> speed;
    private Value<Float> vSpeed;
    private Value<Boolean> glide;
    private Value<Float> glideSpeed;

    public Flight() {
        super("Flight", "Get off the ground!", 0, Category.MOVEMENT, true);
        this.speed = this.register(new Value<Float>("Speed", this, 10.0f, 0.0f, 20.0f));
        this.vSpeed = this.register(new Value<Float>("V Speed", this, 3.0f, 0.0f, 20.0f));
        this.glide = this.register(new Value<Boolean>("Glide", this, true));
        this.glideSpeed = this.register(new Value<Float>("GlideSpeed", this, 0.25f, 0.0f, 5.0f));
    }

    @EventTarget
    public void onWalkingUpdate(final MotionEvent event) {
        if (Flight.mc.player == null) {
            return;
        }
        final double[] dir = MathUtil.directionSpeed(this.speed.getValue());
        if (Flight.mc.player.isFallFlying()) {
            if (this.flyHeight == null) {
                this.flyHeight = Flight.mc.player.getY();
            }
            if (this.glide.getValue()) {
                this.flyHeight = this.flyHeight - this.glideSpeed.getValue();
            }
            double posX = 0.0;
            double posZ = 0.0;
            if (Flight.mc.player.input.leftImpulse != 0.0f || Flight.mc.player.input.forwardImpulse != 0.0f) {
                posX = dir[0];
                posZ = dir[1];
            }
            if (Flight.mc.options.keyJump.isDown()) {
                this.flyHeight = Flight.mc.player.getY() + this.vSpeed.getValue();
            }
            if (Flight.mc.options.keyShift.isDown()) {
                this.flyHeight = Flight.mc.player.getY() - this.vSpeed.getValue();
            }
            Flight.mc.player.setPos(Flight.mc.player.getX() + posX, this.flyHeight, Flight.mc.player.getZ() + posZ);
            Flight.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
        }
        this.flyHeight = null;
    }
}
