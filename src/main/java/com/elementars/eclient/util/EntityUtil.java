package com.elementars.eclient.util;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
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

    public static boolean isMobAggressive(final Entity entity) {
        if (entity instanceof ZombifiedPiglin) {
            if (((NeutralMob)entity).isAngry()) {
                return true;
            }
        }
        else {
            if (entity instanceof Wolf) {
                final Wolf wolf = (Wolf)entity;
                return wolf.isAngry() && !Wrapper.getPlayer().equals(wolf.getOwner());
            }
            if (entity instanceof EnderMan) {
                return ((EnderMan)entity).isCreepy();
            }
        }
        return isHostileMob(entity);
    }

    public static boolean isNeutralMob(final Entity entity) {
        return entity instanceof ZombifiedPiglin || entity instanceof Wolf || entity instanceof EnderMan;
    }

    public static boolean isHostileMob(final Entity entity) {
        return entity.getType().getCategory() == MobCategory.MONSTER && !isNeutralMob(entity);
    }

    public static Vec3 getInterpolatedPos(final Entity entity, final float partialTicks) {
        return new Vec3(
            entity.xOld + (entity.getX() - entity.xOld) * partialTicks,
            entity.yOld + (entity.getY() - entity.yOld) * partialTicks,
            entity.zOld + (entity.getZ() - entity.zOld) * partialTicks
        );
    }
}
