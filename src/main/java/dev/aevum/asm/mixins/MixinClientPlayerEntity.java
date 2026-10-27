/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.InteractionHand
 *  net.minecraft.MoverType
 *  net.minecraft.Vec3
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundSwingPacket
 *  net.minecraft.Minecraft
 *  net.minecraft.Mth
 *  net.minecraft.ClientPacketListener
 *  net.minecraft.ClientLevel
 *  net.minecraft.AbstractClientPlayer
 *  net.minecraft.Input
 *  net.minecraft.LocalPlayer
 *  org.spongepowered.asm.mixin.Final
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

import com.mojang.authlib.GameProfile;
import dev.aevum.Aevum;
import dev.aevum.api.events.Event;
import dev.aevum.api.events.impl.MoveEvent;
import dev.aevum.api.events.impl.MovedEvent;
import dev.aevum.api.events.impl.SendMovementPacketsEvent;
import dev.aevum.api.events.impl.TickEvent;
import dev.aevum.api.events.impl.TickMovementEvent;
import dev.aevum.asm.accessors.IClientPlayerEntity;
import dev.aevum.core.impl.CommandManager;
import dev.aevum.core.impl.RotationManager;
import dev.aevum.mod.modules.impl.client.AntiCheat;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import dev.aevum.mod.modules.impl.exploit.PacketControl;
import dev.aevum.mod.modules.impl.misc.Swing;
import dev.aevum.mod.modules.impl.movement.NoSlow;
import dev.aevum.mod.modules.impl.movement.Velocity;
import net.minecraft.InteractionHand;
import net.minecraft.MoverType;
import net.minecraft.Vec3;
import net.minecraft.Packet;
import net.minecraft.ServerboundSwingPacket;
import net.minecraft.Minecraft;
import net.minecraft.Mth;
import net.minecraft.ClientPacketListener;
import net.minecraft.ClientLevel;
import net.minecraft.AbstractClientPlayer;
import net.minecraft.Input;
import net.minecraft.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LocalPlayer.class})
public abstract class MixinClientPlayerEntity
extends AbstractClientPlayer {
    @Shadow
    public Input input;
    @Shadow
    public ClientPacketListener connection;
    @Final
    @Shadow
    protected Minecraft minecraft;
    @Unique
    private float preYaw;
    @Unique
    private float prePitch;
    @Unique
    private boolean rotation = false;
    @Shadow
    private double xLast;
    @Shadow
    private double yLast1;
    @Shadow
    private double zLast;
    @Shadow
    private int positionReminder;
    @Shadow
    private float yRotLast;
    @Shadow
    private float xRotLast;

    public MixinClientPlayerEntity(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method={"pushOutOfBlocks"}, at={@At(value="HEAD")}, cancellable=true)
    private void onPushOutOfBlocksHook(double x, double d, CallbackInfo info) {
        if (Velocity.INSTANCE.isOn() && Velocity.INSTANCE.blockPush.getValue()) {
            info.cancel();
        }
    }

    @Redirect(method={"tickMovement"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"), require=0)
    private boolean tickMovementHook(LocalPlayer player) {
        if (NoSlow.INSTANCE.noSlow()) {
            return false;
        }
        return player.isUsingItem();
    }

    @Inject(at={@At(value="HEAD")}, method={"tickNausea"}, cancellable=true)
    private void updateNausea(CallbackInfo ci) {
        if (ClientSetting.INSTANCE.portalGui()) {
            ci.cancel();
        }
    }

    @Inject(method={"move"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V")}, cancellable=true)
    public void onMoveHook(MoverType movementType, Vec3 movement, CallbackInfo ci) {
        MoveEvent event = MoveEvent.get(movement.x, movement.y, movement.z);
        Aevum.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        } else if (event.modify) {
            ci.cancel();
            super.method_5784(movementType, new Vec3(event.getX(), event.getY(), event.getZ()));
            Aevum.EVENT_BUS.post(MovedEvent.INSTANCE);
        }
    }

    @Inject(method={"move"}, at={@At(value="TAIL")})
    public void onMoveReturnHook(MoverType movementType, Vec3 movement, CallbackInfo ci) {
        Aevum.EVENT_BUS.post(MovedEvent.INSTANCE);
    }

    @Shadow
    public abstract float method_5695(float var1);

    @Shadow
    public abstract float method_5705(float var1);

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void tickHead(CallbackInfo ci) {
        block2: {
            try {
                Aevum.EVENT_BUS.post(TickEvent.get(Event.Stage.Pre));
            }
            catch (Exception e) {
                e.printStackTrace();
                if (!ClientSetting.INSTANCE.debug.getValue()) break block2;
                CommandManager.sendMessage("\u00a74An error has occurred (ClientPlayerEntity.tick() [HEAD]) Message: [" + e.getMessage() + "]");
            }
        }
    }

    @Inject(method={"tick"}, at={@At(value="RETURN")})
    private void tickReturn(CallbackInfo ci) {
        block2: {
            try {
                Aevum.EVENT_BUS.post(TickEvent.get(Event.Stage.Post));
            }
            catch (Exception e) {
                e.printStackTrace();
                if (!ClientSetting.INSTANCE.debug.getValue()) break block2;
                CommandManager.sendMessage("\u00a74An error has occurred (ClientPlayerEntity.tick() [RETURN]) Message: [" + e.getMessage() + "]");
            }
        }
    }

    @Inject(method={"sendMovementPackets"}, at={@At(value="HEAD")})
    private void onSendMovementPacketsHead(CallbackInfo info) {
        this.rotation();
        if (PacketControl.INSTANCE.isOn() && PacketControl.INSTANCE.positionSync.getValue() && this.positionReminder >= PacketControl.INSTANCE.positionDelay.getValueInt() - 1) {
            ((IClientPlayerEntity)((Object)this)).setTicksSinceLastPositionPacketSent(50);
        }
        if (RotationManager.snapBack) {
            ((IClientPlayerEntity)((Object)this)).setTicksSinceLastPositionPacketSent(50);
            ((IClientPlayerEntity)((Object)this)).setLastYaw(999.0f);
            RotationManager.snapBack = false;
            return;
        }
        if (AntiCheat.INSTANCE.fullPackets.getValue()) {
            double d = this.getX() - this.xLast;
            double e = this.getY() - this.yLast1;
            double f = this.getZ() - this.zLast;
            double g = this.getYRot() - this.yRotLast;
            double h = this.getXRot() - this.xRotLast;
            boolean bl3 = g != 0.0 || h != 0.0;
            boolean bl = bl3;
            if (AntiCheat.INSTANCE.force.getValue() || !(Mth.method_41190((double)d, (double)e, (double)f) > Mth.method_33723((double)2.0E-4)) && this.positionReminder >= 19 || bl3) {
                ((IClientPlayerEntity)((Object)this)).setTicksSinceLastPositionPacketSent(50);
                ((IClientPlayerEntity)((Object)this)).setLastYaw(999.0f);
            }
        }
    }

    @Inject(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal=0)})
    private void onTickHasVehicleBeforeSendPackets(CallbackInfo info) {
        this.rotation();
    }

    @Unique
    private void rotation() {
        this.rotation = true;
        this.preYaw = this.getYRot();
        this.prePitch = this.getXRot();
        SendMovementPacketsEvent event = SendMovementPacketsEvent.get(this.getYRot(), this.getXRot());
        Aevum.EVENT_BUS.post(event);
        Aevum.ROTATION.rotationYaw = event.getYaw();
        Aevum.ROTATION.rotationPitch = event.getPitch();
        this.method_36456(event.getYaw());
        this.method_36457(event.getPitch());
    }

    @Inject(method={"sendMovementPackets"}, at={@At(value="TAIL")})
    private void onSendMovementPacketsTail(CallbackInfo info) {
        if (this.rotation) {
            this.method_36456(this.preYaw);
            this.method_36457(this.prePitch);
            this.rotation = false;
        }
    }

    @Inject(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal=1, shift=At.Shift.AFTER)})
    private void onTickHasVehicleAfterSendPackets(CallbackInfo info) {
        if (this.rotation) {
            this.method_36456(this.preYaw);
            this.method_36457(this.prePitch);
            this.rotation = false;
        }
    }

    @Inject(method={"tickMovement"}, at={@At(value="HEAD")})
    private void tickMovement(CallbackInfo ci) {
        Aevum.EVENT_BUS.post(TickMovementEvent.INSTANCE);
    }

    @Inject(method={"swingHand(Lnet/minecraft/util/Hand;)V"}, at={@At(value="HEAD")}, cancellable=true)
    public void swingHandHook(InteractionHand hand, CallbackInfo callbackInfo) {
        Swing swing = Swing.INSTANCE;
        if (swing == null || !swing.isOn()) {
            return;
        }
        if (swing.hand.getValue() == Swing.HandMode.Vanilla) {
            return;
        }
        if (swing.hand.getValue() != Swing.HandMode.Packet) {
            InteractionHand target = swing.hand.getValue() == Swing.HandMode.Offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            super.method_6104(target);
        }
        this.connection.method_52787((Packet)new ServerboundSwingPacket(hand));
        callbackInfo.cancel();
    }
}

