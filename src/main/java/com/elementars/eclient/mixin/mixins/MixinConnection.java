package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.event.events.EventReceivePacket;
import com.elementars.eclient.event.events.EventSendPacket;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class MixinConnection
{
    @Unique
    private static boolean xulu$replacingSendPacket;

    @Unique
    private static boolean xulu$replacingReceivePacket;

    @Shadow
    protected abstract void channelRead0(ChannelHandlerContext context, Packet<?> packet);

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void xulu$channelRead0(final ChannelHandlerContext context, final Packet<?> packet, final CallbackInfo callback) {
        if (xulu$replacingReceivePacket) {
            return;
        }
        final EventReceivePacket event = new EventReceivePacket(packet);
        event.call();
        if (event.isCancelled()) {
            callback.cancel();
            return;
        }
        if (event.getPacket() != packet) {
            xulu$replacingReceivePacket = true;
            try {
                this.channelRead0(context, event.getPacket());
            }
            finally {
                xulu$replacingReceivePacket = false;
            }
            callback.cancel();
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void xulu$sendPacket(final Packet<?> packet, final CallbackInfo callback) {
        if (xulu$replacingSendPacket) {
            return;
        }
        final EventSendPacket event = new EventSendPacket(packet);
        event.call();
        if (event.isCancelled()) {
            callback.cancel();
            return;
        }
        if (event.getPacket() != packet) {
            xulu$replacingSendPacket = true;
            try {
                ((Connection)(Object)this).send(event.getPacket());
            }
            finally {
                xulu$replacingSendPacket = false;
            }
            callback.cancel();
        }
    }
}
