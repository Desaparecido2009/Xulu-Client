package com.elementars.eclient.util;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import java.nio.FloatBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.BufferUtils;

public class VectorUtils
{
    static final Minecraft mc = Minecraft.getInstance();
    public static Matrix4f modelMatrix = new Matrix4f();
    public static Matrix4f projectionMatrix = new Matrix4f();

    private static void VecTransformCoordinate(final Vector4f vec, final Matrix4f matrix) {
        final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrix.store(buffer);
        final float[] m = new float[16];
        buffer.get(m);
        final float x = vec.x();
        final float y = vec.y();
        final float z = vec.z();
        vec.set(x * m[0] + y * m[4] + z * m[8] + m[12], x * m[1] + y * m[5] + z * m[9] + m[13], x * m[2] + y * m[6] + z * m[10] + m[14], x * m[3] + y * m[7] + z * m[11] + m[15]);
    }

    public static Plane toScreen(final double x, final double y, final double z) {
        final Entity view = VectorUtils.mc.getCameraEntity();
        if (view == null) {
            return new Plane(0.0, 0.0, false);
        }
        final Vec3 camPos = VectorUtils.mc.gameRenderer.getMainCamera().getPosition();
        final float vecX = (float)(camPos.x - x);
        final float vecY = (float)(camPos.y - y);
        final float vecZ = (float)(camPos.z - z);
        final Vector4f pos = new Vector4f(vecX, vecY, vecZ, 1.0f);
        VecTransformCoordinate(pos, VectorUtils.modelMatrix);
        VecTransformCoordinate(pos, VectorUtils.projectionMatrix);
        if (pos.w() > 0.0f) {
            pos.setX(pos.x() * -100000.0f);
            pos.setY(pos.y() * -100000.0f);
        }
        else {
            final float invert = 1.0f / pos.w();
            pos.setX(pos.x() * invert);
            pos.setY(pos.y() * invert);
        }
        final float halfWidth = VectorUtils.mc.getWindow().getGuiScaledWidth() / 2.0f;
        final float halfHeight = VectorUtils.mc.getWindow().getGuiScaledHeight() / 2.0f;
        final float screenX = halfWidth + (0.5f * pos.x() * VectorUtils.mc.getWindow().getGuiScaledWidth() + 0.5f);
        final float screenY = halfHeight - (0.5f * pos.y() * VectorUtils.mc.getWindow().getGuiScaledHeight() + 0.5f);
        boolean bVisible = true;
        if (screenX < 0.0f || screenY < 0.0f || screenX > VectorUtils.mc.getWindow().getGuiScaledWidth() || screenY > VectorUtils.mc.getWindow().getGuiScaledHeight()) {
            bVisible = false;
        }
        return new Plane(screenX, screenY, bVisible);
    }

    public static Plane toScreen(final Vec3 vec) {
        return toScreen(vec.x, vec.y, vec.z);
    }

    public static Vec3 from(final double x, final double y, final double z) {
        return new Vec3(x, y, z);
    }

    public static Vec3 multiplyBy(final Vec3 vec1, final Vec3 vec2) {
        return new Vec3(vec1.x * vec2.x, vec1.y * vec2.y, vec1.z * vec2.z);
    }

    public static Vec3 copy(final Vec3 toCopy) {
        return new Vec3(toCopy.x, toCopy.y, toCopy.z);
    }

    public static double getCrosshairDistance(final Vec3 eyes, final Vec3 directionVec, final Vec3 pos) {
        return pos.subtract(eyes).normalize().subtract(directionVec).lengthSqr();
    }
}
