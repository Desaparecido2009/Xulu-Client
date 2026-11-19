package com.elementars.eclient.module.render;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.ColorUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xulu.settings.Value;
import net.minecraft.util.Mth;

public class Compass extends Module
{
    public final Value<Boolean> axis;
    public final Value<Integer> scale;
    public final Value<Float> position;
    public final Value<Integer> xposition;
    private static final double HALF_PI = 1.5707963267948966;

    public Compass() {
        super("Compass", "Credit to fr1kin", 0, Category.RENDER, true);
        this.axis = this.register(new Value<Boolean>("Axis", this, false));
        this.scale = this.register(new Value<Integer>("Scale", this, 3, 1, 10));
        this.position = this.register(new Value<Float>("Y Position", this, 8.0f, 0.0f, 10.0f));
        this.xposition = this.register(new Value<Integer>("X Position", this, 0, -500, 500));
    }

    @Override
    public void onRender() {
        if (Compass.mc.player == null) {
            return;
        }
        final int width = Compass.mc.getWindow().getGuiScaledWidth();
        final int height = Compass.mc.getWindow().getGuiScaledHeight();
        final double centerX = width / 2.0 + this.xposition.getValue();
        final double centerY = height * (this.position.getValue() / 10.0f);
        final PoseStack poseStack = new PoseStack();
        for (final Direction dir : Direction.values()) {
            final double rad = getPosOnCompass(dir);
            Compass.mc.font.drawShadow(poseStack, ((boolean)this.axis.getValue()) ? dir.getAlternate() : dir.name(), (float)(centerX + this.getX(rad)), (float)(centerY + this.getY(rad)), (dir == Direction.N) ? ColorUtils.Colors.RED : ColorUtils.Colors.WHITE);
        }
    }

    private double getX(final double rad) {
        return Math.sin(rad) * (this.scale.getValue() * 10);
    }

    private double getY(final double rad) {
        final double epicPitch = Mth.clamp(Compass.mc.player.getXRot() + 30.0f, -90.0f, 90.0f);
        final double pitchRadians = Math.toRadians(epicPitch);
        return Math.cos(rad) * Math.sin(pitchRadians) * (this.scale.getValue() * 10);
    }

    private static double getPosOnCompass(final Direction dir) {
        final double yaw = Math.toRadians(Mth.wrapDegrees(Compass.mc.player.getYRot()));
        final int index = dir.ordinal();
        return yaw + index * 1.5707963267948966;
    }

    private enum Direction
    {
        N("-Z"),
        W("-X"),
        S("+Z"),
        E("+X");

        private final String alternate;

        private Direction(final String alternate) {
            this.alternate = alternate;
        }

        public String getAlternate() {
            return this.alternate;
        }
    }
}
