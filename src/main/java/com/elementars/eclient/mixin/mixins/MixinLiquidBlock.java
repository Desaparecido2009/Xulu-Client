package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.module.misc.LiquidInteract;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LiquidBlock.class)
public class MixinLiquidBlock
{
    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void xulu$liquidInteract(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context, final CallbackInfoReturnable<VoxelShape> info) {
        if (LiquidInteract.INSTANCE != null && LiquidInteract.INSTANCE.isToggled()) {
            info.setReturnValue(Shapes.block());
        }
    }
}
