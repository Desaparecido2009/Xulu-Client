package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.module.misc.NoEntityTrace;
import com.elementars.eclient.module.render.NoHurtCam;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(GameRenderer.class)
public class MixinGameRenderer
{
    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void xulu$noHurtCam(final PoseStack poseStack, final float partialTicks, final CallbackInfo info) {
        if (NoHurtCam.shouldDisable()) {
            info.cancel();
        }
    }

    @Redirect(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"))
    private EntityHitResult xulu$noEntityTrace(final Entity entity, final Vec3 from, final Vec3 to, final AABB box, final Predicate<Entity> predicate, final double distance) {
        if (NoEntityTrace.shouldBlock()) {
            return null;
        }
        return ProjectileUtil.getEntityHitResult(entity, from, to, box, predicate, distance);
    }
}
