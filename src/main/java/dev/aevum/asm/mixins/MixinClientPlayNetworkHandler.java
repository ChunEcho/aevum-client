/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockableEventLoop
 *  net.minecraft.Entity
 *  net.minecraft.Vec3
 *  net.minecraft.Connection
 *  net.minecraft.PacketListener
 *  net.minecraft.Packet
 *  net.minecraft.PacketUtils
 *  net.minecraft.ClientGamePacketListener
 *  net.minecraft.ClientboundContainerClosePacket
 *  net.minecraft.ClientboundContainerSetContentPacket
 *  net.minecraft.ClientboundLoginPacket
 *  net.minecraft.ClientboundPlayerPositionPacket
 *  net.minecraft.RelativeMovement
 *  net.minecraft.ServerboundAcceptTeleportationPacket
 *  net.minecraft.ServerboundMovePlayerPacket$PosRot
 *  net.minecraft.Minecraft
 *  net.minecraft.ClientPacketListener
 *  net.minecraft.ClientLevel
 *  net.minecraft.LocalPlayer
 *  net.minecraft.ClientboundStartConfigurationPacket
 *  net.minecraft.ClientCommonPacketListenerImpl
 *  net.minecraft.CommonListenerCookie
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.Aevum;
import dev.aevum.api.events.impl.EntityVelocityUpdateEvent;
import dev.aevum.api.events.impl.GameLeftEvent;
import dev.aevum.api.events.impl.InventoryS2CPacketEvent;
import dev.aevum.api.events.impl.S2CCloseScreenEvent;
import dev.aevum.api.events.impl.SendMessageEvent;
import dev.aevum.api.events.impl.ServerChangePositionEvent;
import dev.aevum.mod.modules.impl.exploit.AntiPacket;
import net.minecraft.BlockableEventLoop;
import net.minecraft.Entity;
import net.minecraft.Vec3;
import net.minecraft.Connection;
import net.minecraft.PacketListener;
import net.minecraft.Packet;
import net.minecraft.PacketUtils;
import net.minecraft.ClientGamePacketListener;
import net.minecraft.ClientboundContainerClosePacket;
import net.minecraft.ClientboundContainerSetContentPacket;
import net.minecraft.ClientboundLoginPacket;
import net.minecraft.ClientboundPlayerPositionPacket;
import net.minecraft.RelativeMovement;
import net.minecraft.ServerboundAcceptTeleportationPacket;
import net.minecraft.ServerboundMovePlayerPacket;
import net.minecraft.Minecraft;
import net.minecraft.ClientPacketListener;
import net.minecraft.ClientLevel;
import net.minecraft.LocalPlayer;
import net.minecraft.ClientboundStartConfigurationPacket;
import net.minecraft.ClientCommonPacketListenerImpl;
import net.minecraft.CommonListenerCookie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public abstract class MixinClientPlayNetworkHandler
extends ClientCommonPacketListenerImpl {
    @Shadow
    private ClientLevel level;
    @Unique
    private boolean aevum$worldNotNull;
    @Unique
    private boolean ignore;

    protected MixinClientPlayNetworkHandler(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method={"onEnterReconfiguration"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift=At.Shift.AFTER)})
    private void onEnterReconfiguration(ClientboundStartConfigurationPacket packet, CallbackInfo info) {
        Aevum.EVENT_BUS.post(GameLeftEvent.INSTANCE);
    }

    @Inject(method={"onGameJoin"}, at={@At(value="HEAD")})
    private void onGameJoinHead(ClientboundLoginPacket packet, CallbackInfo info) {
        this.aevum$worldNotNull = this.level != null;
    }

    @Inject(method={"onGameJoin"}, at={@At(value="TAIL")})
    private void onGameJoinTail(ClientboundLoginPacket packet, CallbackInfo info) {
        if (this.aevum$worldNotNull) {
            Aevum.EVENT_BUS.post(GameLeftEvent.INSTANCE);
        }
    }

    @Shadow
    public abstract void method_45729(String var1);

    @Inject(method={"onInventory"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift=At.Shift.AFTER)}, cancellable=true)
    public void onInventoryS2CPacket(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        InventoryS2CPacketEvent event = InventoryS2CPacketEvent.get(packet);
        Aevum.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"sendChatMessage"}, at={@At(value="HEAD")}, cancellable=true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (this.ignore) {
            return;
        }
        if (message.startsWith(Aevum.getPrefix())) {
            Aevum.COMMAND.command(message.split(" "));
            ci.cancel();
        } else {
            SendMessageEvent event = SendMessageEvent.get(message);
            Aevum.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                ci.cancel();
            } else if (!event.message.equals(event.defaultMessage)) {
                this.ignore = true;
                this.method_45729(event.message);
                this.ignore = false;
                ci.cancel();
            }
        }
    }

    @Inject(method={"onCloseScreen"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift=At.Shift.AFTER)}, cancellable=true)
    public void onCloseScreen(ClientboundContainerClosePacket packet, CallbackInfo ci) {
        S2CCloseScreenEvent event = S2CCloseScreenEvent.get();
        Aevum.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Redirect(method={"onEntityVelocityUpdate"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;setVelocityClient(DDD)V"), require=0)
    private void velocityHook(Entity instance, double x, double y, double z) {
        EntityVelocityUpdateEvent event = EntityVelocityUpdateEvent.get(instance, x, y, z, false);
        Aevum.EVENT_BUS.post(event);
        if (!event.isCancelled()) {
            instance.method_5750(event.getX(), event.getY(), event.getZ());
        }
    }

    @Redirect(method={"onExplosion"}, at=@At(value="INVOKE", target="Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;"), require=0)
    private Vec3 velocityHook2(Vec3 instance, double x, double y, double z) {
        EntityVelocityUpdateEvent event = EntityVelocityUpdateEvent.get((Entity)this.minecraft.player, x, y, z, true);
        Aevum.EVENT_BUS.post(event);
        if (!event.isCancelled()) {
            return instance.method_1031(event.getX(), event.getY(), event.getZ());
        }
        return instance;
    }

    @Inject(method={"onPlayerPositionLook"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift=At.Shift.AFTER)}, cancellable=true)
    public void onPlayerPositionLook(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        boolean noRotate = AntiPacket.INSTANCE.isOn() && AntiPacket.INSTANCE.s2CRotate.getValue() && Aevum.SERVER.playerNull.passedS(0.25);
        boolean bl = noRotate;
        if (noRotate) {
            double i;
            double h;
            double g;
            double f;
            double e;
            double d;
            ci.cancel();
            PacketUtils.method_11074((Packet)packet, (PacketListener)((ClientGamePacketListener)ClientPacketListener.class.cast((Object)this)), (BlockableEventLoop)this.minecraft);
            LocalPlayer playerEntity = this.minecraft.player;
            Vec3 vec3d = playerEntity.getDeltaMovement();
            boolean bl2 = packet.getRelativeArguments().contains(RelativeMovement.X);
            boolean bl22 = packet.getRelativeArguments().contains(RelativeMovement.Y);
            boolean bl3 = packet.getRelativeArguments().contains(RelativeMovement.Z);
            if (bl2) {
                d = vec3d.method_10216();
                e = playerEntity.getX() + packet.getX();
                playerEntity.xOld += packet.getX();
                playerEntity.xo += packet.getX();
            } else {
                d = 0.0;
                playerEntity.xOld = e = packet.getX();
                playerEntity.xo = e;
            }
            if (bl22) {
                f = vec3d.method_10214();
                g = playerEntity.getY() + packet.getY();
                playerEntity.yOld += packet.getY();
                playerEntity.yo += packet.getY();
            } else {
                f = 0.0;
                playerEntity.yOld = g = packet.getY();
                playerEntity.yo = g;
            }
            if (bl3) {
                h = vec3d.method_10215();
                i = playerEntity.getZ() + packet.getZ();
                playerEntity.zOld += packet.getZ();
                playerEntity.zo += packet.getZ();
            } else {
                h = 0.0;
                playerEntity.zOld = i = packet.getZ();
                playerEntity.zo = i;
            }
            playerEntity.method_5814(e, g, i);
            playerEntity.method_18800(d, f, h);
            if (AntiPacket.INSTANCE.applyYaw.getValue()) {
                float yaw = packet.getYRot();
                float pitch = packet.getXRot();
                if (packet.getRelativeArguments().contains(RelativeMovement.X_ROT)) {
                    pitch += Aevum.ROTATION.getLastPitch();
                }
                if (packet.getRelativeArguments().contains(RelativeMovement.Y_ROT)) {
                    yaw += Aevum.ROTATION.getLastYaw();
                }
                this.connection.method_10743((Packet)new ServerboundAcceptTeleportationPacket(packet.getId()));
                this.connection.method_10743((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$PosRot(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), yaw, pitch, false));
            } else {
                this.connection.method_10743((Packet)new ServerboundAcceptTeleportationPacket(packet.getId()));
                this.connection.method_10743((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$PosRot(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), Aevum.ROTATION.getLastYaw(), Aevum.ROTATION.getLastPitch(), false));
            }
            Aevum.EVENT_BUS.post(ServerChangePositionEvent.INSTANCE);
        }
    }
}

