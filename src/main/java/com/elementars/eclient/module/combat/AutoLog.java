package com.elementars.eclient.module.combat;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AutoLog extends Module
{
    private final Value<Integer> health;
    private boolean shouldLog;
    private long lastLog;

    public AutoLog() {
        super("AutoLog", "Automatically Logs", 0, Category.COMBAT, true);
        this.health = this.register(new Value<Integer>("Health", this, 6, 0, 36));
        this.shouldLog = false;
        this.lastLog = System.currentTimeMillis();
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        this.shouldLog = false;
    }

    @SubscribeEvent
    public void onEntity(final EntityJoinWorldEvent event) {
        if (!event.getWorld().isClientSide() || AutoLog.mc.player == null || AutoLog.mc.level == null) {
            return;
        }
        if (!(event.getEntity() instanceof EndCrystal)) {
            return;
        }
        final float damage = this.calculateDamage((EndCrystal)event.getEntity(), AutoLog.mc.player);
        final float hp = AutoLog.mc.player.getHealth() + AutoLog.mc.player.getAbsorptionAmount();
        if (hp - damage < this.health.getValue()) {
            this.log();
        }
    }

    @Override
    public void onUpdate() {
        if (!this.shouldLog) {
            return;
        }
        this.shouldLog = false;
        if (AutoLog.mc.getConnection() != null) {
            AutoLog.mc.getConnection().handleDisconnect(new ClientboundDisconnectPacket(new TextComponent("AutoLogged")));
        }
    }

    private void log() {
        final long now = System.currentTimeMillis();
        if (now - this.lastLog < 2000L) {
            return;
        }
        final Module autoReconnect = com.elementars.eclient.Xulu.MODULE_MANAGER.getModuleByName("AutoReconnect");
        if (autoReconnect != null) {
            autoReconnect.disable();
        }
        this.lastLog = now;
        this.shouldLog = true;
    }

    private float calculateDamage(final EndCrystal crystal, final LivingEntity target) {
        final Vec3 pos = crystal.position();
        final double dist = Math.sqrt(target.distanceToSqr(pos)) / 12.0;
        if (dist > 1.0) {
            return 0.0f;
        }
        final double impact = (1.0 - dist) * Explosion.getSeenPercent(pos, target);
        float damage = (float)((int)((impact * impact + impact) / 2.0 * 7.0 * 12.0 + 1.0));
        switch (AutoLog.mc.level.getDifficulty()) {
            case PEACEFUL:
                return 0.0f;
            case EASY:
                damage = Math.min(damage / 2.0f + 1.0f, damage);
                break;
            case HARD:
                damage = damage * 3.0f / 2.0f;
                break;
            default:
                break;
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, (float)target.getArmorValue(), (float)target.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        final int protection = EnchantmentHelper.getDamageProtection(target.getArmorSlots(), DamageSource.explosion((Explosion)null));
        if (protection > 0) {
            damage = CombatRules.getDamageAfterMagicAbsorb(damage, (float)protection);
        }
        final MobEffectInstance resistance = target.getEffect(MobEffects.DAMAGE_RESISTANCE);
        if (resistance != null) {
            damage = damage * (25 - (resistance.getAmplifier() + 1) * 5) / 25.0f;
        }
        return Math.max(damage, 0.0f);
    }
}