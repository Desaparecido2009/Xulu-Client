package com.elementars.eclient.mixin.mixins;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundMovePlayerPacket.class)
public interface AccessorServerboundMovePlayerPacket
{
    @Accessor("xRot")
    float getPacketXRot();

    @Mutable
    @Accessor("xRot")
    void setPacketXRot(final float xRot);
}
