package com.elementars.eclient.util;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.phys.Vec3;

public class EntityUtil
{
    public static boolean isPassive(final Entity e) {
        return (!(e instanceof Wolf) || !((Wolf)e).isAngry())
            && (e instanceof Animal || e instanceof AgeableMob || e instanceof TamableAnimal || e instanceof AmbientCreature || e instanceof Squid || (e instanceof IronGolem && ((IronGolem)e).getLastHurtByMob() == null));
    }

    public static boolean isLiving(final Entity e) {
        return e instanceof LivingEntity;
    }

    public static Vec3 getInterpolatedPos(final Entity entity, final float partialTicks) {
        return new Vec3(
            entity.xOld + (entity.getX() - entity.xOld) * partialTicks,
            entity.yOld + (entity.getY() - entity.yOld) * partialTicks,
            entity.zOld + (entity.getZ() - entity.zOld) * partialTicks
        );
    }
}
