/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Minecraft
 *  net.minecraft.ConnectScreen
 *  net.minecraft.Screen
 *  net.minecraft.ServerAddress
 *  net.minecraft.ServerData
 *  net.minecraft.TransferState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.Aevum;
import dev.aevum.api.events.impl.ServerConnectBeginEvent;
import net.minecraft.Minecraft;
import net.minecraft.ConnectScreen;
import net.minecraft.Screen;
import net.minecraft.ServerAddress;
import net.minecraft.ServerData;
import net.minecraft.TransferState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ConnectScreen.class})
public abstract class MixinConnectScreen {
    @Inject(method={"connect(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;ZLnet/minecraft/client/network/CookieStorage;)V"}, at={@At(value="HEAD")})
    private static void tryConnectEvent(Screen screen, Minecraft client, ServerAddress address, ServerData info, boolean quickPlay, TransferState cookieStorage, CallbackInfo ci) {
        Aevum.EVENT_BUS.post(ServerConnectBeginEvent.get(address, info));
    }

    @Inject(method={"connect(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;Lnet/minecraft/client/network/CookieStorage;)V"}, at={@At(value="HEAD")})
    private void tryConnectEvent(Minecraft client, ServerAddress address, ServerData info, TransferState cookieStorage, CallbackInfo ci) {
        Aevum.EVENT_BUS.post(ServerConnectBeginEvent.get(address, info));
    }
}

