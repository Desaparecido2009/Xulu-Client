package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.module.render.Skeleton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerModel.class, priority = 9999)
public class MixinModelPlayer
{
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("RETURN"))
    private void xulu$setupAnim(final LivingEntity entityIn, final float limbSwing, final float limbSwingAmount, final float ageInTicks, final float netHeadYaw, final float headPitch, final CallbackInfo callbackInfo) {
        if (Minecraft.getInstance().level != null && Minecraft.getInstance().player != null && entityIn instanceof Player) {
            Skeleton.addEntity((Player)entityIn, (PlayerModel)(Object)this);
        }
    }
}
