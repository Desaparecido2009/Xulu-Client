package com.elementars.eclient.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class MathUtil
{
    public static Vec3 interpolateEntity(final Entity entity, final float time) {
        return new Vec3(entity.xo + (entity.getX() - entity.xo) * time, entity.yo + (entity.getY() - entity.yo) * time, entity.zo + (entity.getZ() - entity.zo) * time);
    }

    public static double radToDeg(final double rad) {
        return rad * 57.295780181884766;
    }

    public static double degToRad(final double deg) {
        return deg * 0.01745329238474369;
    }

    public static Vec3 direction(final float yaw) {
        return new Vec3(Math.cos(degToRad(yaw + 90.0f)), 0.0, Math.sin(degToRad(yaw + 90.0f)));
    }

    public static float[] calcAngle(final Vec3 from, final Vec3 to) {
        final double difX = to.x - from.x;
        final double difY = (to.y - from.y) * -1.0;
        final double difZ = to.z - from.z;
        final double dist = Mth.sqrt((float)(difX * difX + difZ * difZ));
        return new float[] { (float)Mth.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0), (float)Mth.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist))) };
    }

    public static double[] directionSpeed(final double speed) {
        final Minecraft mc = Minecraft.getInstance();
        float forward = mc.player.input.forwardImpulse;
        float side = mc.player.input.leftImpulse;
        float yaw = mc.player.yRotO + (mc.player.getYRot() - mc.player.yRotO) * mc.getFrameTime();
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += ((forward > 0.0f) ? -45 : 45);
            }
            else if (side < 0.0f) {
                yaw += ((forward > 0.0f) ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            }
            else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        final double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        final double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        final double posX = forward * speed * cos + side * speed * sin;
        final double posZ = forward * speed * sin - side * speed * cos;
        return new double[] { posX, posZ };
    }

    public static Vec3 mult(final Vec3 factor, final Vec3 multiplier) {
        return new Vec3(factor.x * multiplier.x, factor.y * multiplier.y, factor.z * multiplier.z);
    }

    public static Vec3 mult(final Vec3 factor, final float multiplier) {
        return new Vec3(factor.x * multiplier, factor.y * multiplier, factor.z * multiplier);
    }

    public static Vec3 div(final Vec3 factor, final Vec3 divisor) {
        return new Vec3(factor.x / divisor.x, factor.y / divisor.y, factor.z / divisor.z);
    }

    public static Vec3 div(final Vec3 factor, final float divisor) {
        return new Vec3(factor.x / divisor, factor.y / divisor, factor.z / divisor);
    }

    public static double round(final double value, final int places) {
        if (places < 0) {
            return value;
        }
        return new BigDecimal(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }

    public static int clamp(final int num, final int min, final int max) {
        return (num < min) ? min : Math.min(num, max);
    }

    public static float clamp(final float num, final float min, final float max) {
        return (num < min) ? min : Math.min(num, max);
    }

    public static double clamp(final double num, final double min, final double max) {
        return (num < min) ? min : Math.min(num, max);
    }

    public static int round(final double value) {
        return (int)Math.round(value);
    }

    public static int ensureRange(final int value, final int min, final int max) {
        final int toReturn = Math.min(Math.max(value, min), max);
        return toReturn;
    }

    public static float wrap(float val) {
        val %= 360.0f;
        if (val >= 180.0f) {
            val -= 360.0f;
        }
        if (val < -180.0f) {
            val += 360.0f;
        }
        return val;
    }

    public static double map(double value, final double a, final double b, final double c, final double d) {
        value = (value - a) / (b - a);
        return c + value * (d - c);
    }

    public static double linear(final double from, final double to, final double incline) {
        return (from < to - incline) ? (from + incline) : ((from > to + incline) ? (from - incline) : to);
    }

    public static double parabolic(final double from, final double to, final double incline) {
        return from + (to - from) / incline;
    }

    public static double getDistance(final Vec3 pos, final double x, final double y, final double z) {
        final double deltaX = pos.x - x;
        final double deltaY = pos.y - y;
        final double deltaZ = pos.z - z;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }
}
