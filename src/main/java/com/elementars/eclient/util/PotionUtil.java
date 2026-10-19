package com.elementars.eclient.util;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

public class PotionUtil
{
    public static boolean hasEffect(final LivingEntity entity, final net.minecraft.world.effect.MobEffect effect) {
        return entity.hasEffect(effect);
    }

    public static String getPotionName(final MobEffect effect) {
        return effect.getDisplayName().getString();
    }
}

