package com.elementars.eclient.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;

public class AngleHelper
{
    public static Angle getAngle(final Entity from, final Entity to) {
        return getAngle(from.position(), to.position());
    }

    public static Angle getAngle(final net.minecraft.world.phys.Vec3 from, final net.minecraft.world.phys.Vec3 to) {
        final double dx = to.x - from.x;
        final double dy = to.y - from.y;
        final double dz = to.z - from.z;
        final double dist = Math.sqrt(dx * dx + dz * dz);
        final float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        final float pitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
        return new Angle(yaw, pitch);
    }
}
