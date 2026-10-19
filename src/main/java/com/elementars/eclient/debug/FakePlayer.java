package com.elementars.eclient.debug;

import com.elementars.eclient.util.Wrapper;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FakePlayer extends RemotePlayer
{
    private static final List<FakePlayer> active = new ArrayList<FakePlayer>();
    private static int nextEntityId = Integer.MAX_VALUE;

    public FakePlayer(final ClientLevel level, final GameProfile profile) {
        super(level, profile);
    }

    public static FakePlayer add(final String name) {
        final Minecraft minecraft = Wrapper.getMinecraft();
        final Player player = minecraft.player;
        final ClientLevel level = minecraft.level;
        if (player == null || level == null) {
            return null;
        }
        final Vec3 offset = horizontalOffset(player, 2.0);
        final double x = player.getX() + offset.x;
        final double z = player.getZ() + offset.z;
        final FakePlayer fake = new FakePlayer(level, new GameProfile(UUID.randomUUID(), name));
        final int id = nextEntityId--;
        fake.setId(id);
        fake.setPacketCoordinates(x, player.getY(), z);
        fake.absMoveTo(x, player.getY(), z, Mth.wrapDegrees(player.getYRot() + 180.0F), 0.0F);
        fake.setOldPosAndRot();
        level.addPlayer(id, fake);
        active.add(fake);
        return fake;
    }

    public static boolean remove(final FakePlayer fake) {
        if (fake == null) {
            return false;
        }
        active.remove(fake);
        if (fake.level instanceof ClientLevel) {
            ((ClientLevel)fake.level).removeEntity(fake.getId(), Entity.RemovalReason.DISCARDED);
        }
        return true;
    }

    public static int removeAll() {
        final List<FakePlayer> copy = new ArrayList<FakePlayer>(active);
        for (final FakePlayer fake : copy) {
            remove(fake);
        }
        return copy.size();
    }

    public static void teleport(final FakePlayer fake, final Player player) {
        final Vec3 offset = horizontalOffset(player, 2.0);
        final double x = player.getX() + offset.x;
        final double z = player.getZ() + offset.z;
        fake.absMoveTo(x, player.getY(), z, Mth.wrapDegrees(player.getYRot() + 180.0F), 0.0F);
        fake.setOldPosAndRot();
    }

    public static List<FakePlayer> getActive() {
        prune();
        return active;
    }

    public static FakePlayer getByName(final String name) {
        prune();
        for (final FakePlayer fake : active) {
            if (fake.getName().getString().equalsIgnoreCase(name)) {
                return fake;
            }
        }
        return null;
    }

    private static void prune() {
        final ClientLevel level = Wrapper.getMinecraft().level;
        active.removeIf(fake -> fake.level != level);
    }

    private static Vec3 horizontalOffset(final Player player, final double distance) {
        final float yaw = player.getYRot() * ((float)Math.PI / 180.0F);
        return new Vec3(-Mth.sin(yaw) * distance, 0.0, Mth.cos(yaw) * distance);
    }
}
