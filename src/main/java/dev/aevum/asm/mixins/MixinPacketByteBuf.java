/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.NbtAccounter
 *  net.minecraft.FriendlyByteBuf
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package dev.aevum.asm.mixins;

import dev.aevum.mod.modules.impl.exploit.AntiPacket;
import net.minecraft.NbtAccounter;
import net.minecraft.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={FriendlyByteBuf.class})
public abstract class MixinPacketByteBuf {
    @ModifyArg(method={"readNbt(Lio/netty/buffer/ByteBuf;)Lnet/minecraft/nbt/NbtCompound;"}, at=@At(value="INVOKE", target="Lnet/minecraft/network/PacketByteBuf;readNbt(Lio/netty/buffer/ByteBuf;Lnet/minecraft/nbt/NbtSizeTracker;)Lnet/minecraft/nbt/NbtElement;"))
    private static NbtAccounter xlPackets(NbtAccounter sizeTracker) {
        return AntiPacket.INSTANCE.isOn() && AntiPacket.INSTANCE.decode.getValue() ? NbtAccounter.method_53898() : sizeTracker;
    }
}

