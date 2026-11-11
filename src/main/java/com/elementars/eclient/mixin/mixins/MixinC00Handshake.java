package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.Xulu;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientIntentionPacket.class)
public class MixinC00Handshake
{
    @Shadow
    private int protocolVersion;
    @Shadow
    private String hostName;
    @Shadow
    private int port;
    @Shadow
    private ConnectionProtocol intention;

    @Inject(method = "write", at = @At("HEAD"), cancellable = true)
    public void writePacketData(final FriendlyByteBuf buf, final CallbackInfo info) {
        if (Xulu.MODULE_MANAGER.getModuleByName("FakeVanilla").isToggled()) {
            info.cancel();
            buf.writeVarInt(this.protocolVersion);
            buf.writeUtf(this.hostName);
            buf.writeShort(this.port);
            buf.writeVarInt(this.intention.getId());
        }
    }
}
