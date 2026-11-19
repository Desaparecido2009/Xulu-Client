package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.module.misc.Avoid;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class MixinBlockState
{
    @Shadow
    public abstract Block getBlock();

    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    private void xulu$avoidCollision(final BlockGetter level, final BlockPos pos, final CallbackInfoReturnable<VoxelShape> info) {
        final VoxelShape shape = this.xulu$avoidShape();
        if (shape != null) {
            info.setReturnValue(shape);
        }
    }

    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    private void xulu$avoidCollision(final BlockGetter level, final BlockPos pos, final CollisionContext context, final CallbackInfoReturnable<VoxelShape> info) {
        final VoxelShape shape = this.xulu$avoidShape();
        if (shape != null) {
            info.setReturnValue(shape);
        }
    }

    private VoxelShape xulu$avoidShape() {
        if (!Avoid.isActive() || Avoid.cactus == null) {
            return null;
        }
        final Block block = this.getBlock();
        if (block instanceof CactusBlock && Avoid.cactus.getValue()) {
            return Shapes.block();
        }
        if (block instanceof BaseFireBlock && Avoid.fire.getValue()) {
            return Shapes.block();
        }
        if (block instanceof WebBlock && Avoid.webs.getValue()) {
            return Shapes.block();
        }
        if (block == Blocks.LAVA && Avoid.lava.getValue()) {
            final Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null) {
                final Block above = mc.level.getBlockState(mc.player.blockPosition().above()).getBlock();
                if (above != Blocks.LAVA) {
                    return Shapes.block();
                }
            }
        }
        return null;
    }
}
