/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.timeout.TimeoutException
 *  net.minecraft.Connection
 *  net.minecraft.PacketListener
 *  net.minecraft.SkipPacketException
 *  net.minecraft.Packet
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.Aevum;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.core.impl.CommandManager;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.TimeoutException;
import net.minecraft.Connection;
import net.minecraft.PacketListener;
import net.minecraft.SkipPacketException;
import net.minecraft.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Connection.class})
public class MixinClientConnection {
    @Inject(at={@At(value="HEAD")}, method={"handlePacket"}, cancellable=true)
    private static <T extends PacketListener> void onHandlePacket(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        PacketEvent.Receive event = new PacketEvent.Receive(packet);
        Aevum.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"send(Lnet/minecraft/network/packet/Packet;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void onSendPacketPre(Packet<?> packet, CallbackInfo info) {
        PacketEvent.Send event = new PacketEvent.Send(packet);
        Aevum.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method={"send(Lnet/minecraft/network/packet/Packet;)V"}, at={@At(value="RETURN")})
    private void onSendPacketPost(Packet<?> packet, CallbackInfo info) {
        PacketEvent.Sent event = new PacketEvent.Sent(packet);
        Aevum.EVENT_BUS.post(event);
    }

    @Inject(method={"exceptionCaught"}, at={@At(value="HEAD")}, cancellable=true)
    private void exceptionCaught(ChannelHandlerContext context, Throwable throwable, CallbackInfo ci) {
        if (!(throwable instanceof TimeoutException) && !(throwable instanceof SkipPacketException) && ClientSetting.INSTANCE.caughtException.getValue()) {
            if (ClientSetting.INSTANCE.log.getValue()) {
                CommandManager.sendMessage("\u00a74Caught exception \u00a77" + throwable.getMessage());
            }
            ci.cancel();
        }
    }
}

