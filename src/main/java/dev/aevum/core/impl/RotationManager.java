/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.Vec3
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundMovePlayerPacket
 *  net.minecraft.ServerboundMovePlayerPacket$PosRot
 *  net.minecraft.ServerboundMovePlayerPacket$Rot
 *  net.minecraft.Mth
 */
package dev.aevum.core.impl;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.DoAttackEvent;
import dev.aevum.api.events.impl.FireworkShooterRotationEvent;
import dev.aevum.api.events.impl.InteractBlockEvent;
import dev.aevum.api.events.impl.InteractItemEvent;
import dev.aevum.api.events.impl.JumpEvent;
import dev.aevum.api.events.impl.KeyboardInputEvent;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.RotationEvent;
import dev.aevum.api.events.impl.SendMovementPacketsEvent;
import dev.aevum.api.events.impl.TickEvent;
import dev.aevum.api.events.impl.TickMovementEvent;
import dev.aevum.api.events.impl.TravelEvent;
import dev.aevum.api.events.impl.UpdateRotateEvent;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.path.BaritoneUtil;
import dev.aevum.asm.accessors.IClientPlayerEntity;
import dev.aevum.mod.modules.impl.client.AntiCheat;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import dev.aevum.mod.modules.impl.movement.HoleSnap;
import dev.aevum.mod.modules.impl.player.Freecam;
import dev.aevum.mod.modules.settings.enums.SnapBack;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.Vec3;
import net.minecraft.Packet;
import net.minecraft.ServerboundMovePlayerPacket;
import net.minecraft.Mth;

public class RotationManager
implements Wrapper {
    public static final Timer ROTATE_TIMER = new Timer();
    public static Vec3 directionVec = null;
    public static boolean snapBack = false;
    private static float renderPitch;
    private static float renderYawOffset;
    private static float prevRenderPitch;
    private static float prevRenderYawOffset;
    private static float prevRotationYawHead;
    private static float rotationYawHead;
    public float nextYaw;
    public float nextPitch;
    public float rotationYaw;
    public float rotationPitch;
    public float lastYaw;
    public float lastPitch;
    public Vec3 crossHairUpdatePos;
    private int ticksExisted;
    public static float fixYaw;
    public static float fixPitch;
    private float prevYaw;
    private float prevPitch;

    public RotationManager() {
        Aevum.EVENT_BUS.subscribe(this);
    }

    @EventListener
    public void onInteract(InteractItemEvent event) {
        if (AntiCheat.INSTANCE.interactRotation.getValue() && RotationManager.mc.player != null) {
            if (event.isPre()) {
                this.snapAt(RotationManager.mc.player.getYRot(), RotationManager.mc.player.getXRot());
            } else {
                this.snapBack();
            }
        }
    }

    @EventListener
    public void onInteract(InteractBlockEvent event) {
        if (AntiCheat.INSTANCE.interactRotation.getValue() && RotationManager.mc.player != null) {
            if (event.isPre()) {
                this.snapAt(RotationManager.mc.player.getYRot(), RotationManager.mc.player.getXRot());
            } else {
                this.snapBack();
            }
        }
    }

    @EventListener
    public void doAttack(DoAttackEvent event) {
        if (AntiCheat.INSTANCE.interactRotation.getValue() && RotationManager.mc.player != null) {
            if (event.isPre()) {
                this.snapAt(RotationManager.mc.player.getYRot(), RotationManager.mc.player.getXRot());
            } else {
                this.snapBack();
            }
        }
    }

    public void snapBack() {
        if (AntiCheat.INSTANCE.snapBackEnum.is(SnapBack.Force)) {
            mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$PosRot(RotationManager.mc.player.getX(), RotationManager.mc.player.getY(), RotationManager.mc.player.getZ(), this.rotationYaw, this.rotationPitch, RotationManager.mc.player.onGround()));
        } else if (AntiCheat.INSTANCE.snapBackEnum.is(SnapBack.Tick)) {
            snapBack = true;
        }
    }

    public void lookAt(Vec3 directionVec) {
        this.rotationTo(directionVec);
        this.snapAt(directionVec);
    }

    public void lookAt(BlockPos pos, Direction side) {
        Vec3 hitVec = pos.method_46558().method_1019(new Vec3((double)side.getNormal().method_10263() * 0.5, (double)side.getNormal().method_10264() * 0.5, (double)side.getNormal().getZ() * 0.5));
        this.lookAt(hitVec);
    }

    public void snapAt(float yaw, float pitch) {
        this.setRenderRotation(yaw, pitch, true);
        if (AntiCheat.INSTANCE.grimRotation.getValue()) {
            mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$PosRot(RotationManager.mc.player.getX(), RotationManager.mc.player.getY(), RotationManager.mc.player.getZ(), yaw, pitch, RotationManager.mc.player.onGround()));
        } else {
            mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Rot(yaw, pitch, RotationManager.mc.player.onGround()));
        }
    }

    public void snapAt(Vec3 directionVec) {
        float[] angle = RotationManager.getRotation(directionVec);
        this.snapAt(angle[0], angle[1]);
    }

    public void rotationTo(Vec3 vec3d) {
        ROTATE_TIMER.reset();
        directionVec = vec3d;
    }

    public boolean inFov(Vec3 directionVec, float fov) {
        float[] angle = RotationManager.getRotation(this.crossHairUpdatePos != null ? this.crossHairUpdatePos : new Vec3(RotationManager.mc.player.getX(), RotationManager.mc.player.getY() + (double)RotationManager.mc.player.method_18381(RotationManager.mc.player.getPose()), RotationManager.mc.player.getZ()), directionVec);
        return this.inFov(angle[0], angle[1], fov);
    }

    public boolean inFov(float yaw, float pitch, float fov) {
        float pitchDifferent;
        float f;
        float yawDifferent = Mth.method_15356((float)yaw, (float)this.rotationYaw);
        return yawDifferent * yawDifferent + f * (pitchDifferent = Math.abs(pitch - this.rotationPitch)) <= fov * fov;
    }

    @EventListener
    public void onTickMovement(TickMovementEvent event) {
        if (RotationManager.mc.player == null) {
            return;
        }
        this.crossHairUpdatePos = new Vec3(RotationManager.mc.player.getX(), RotationManager.mc.player.getY() + (double)RotationManager.mc.player.method_18381(RotationManager.mc.player.getPose()), RotationManager.mc.player.getZ());
    }

    @EventListener
    public void update(SendMovementPacketsEvent event) {
        if (AntiCheat.INSTANCE.movementSync() && !BaritoneUtil.isActive()) {
            event.setYaw(this.nextYaw);
            event.setPitch(this.nextPitch);
        } else {
            UpdateRotateEvent updateRotateEvent = UpdateRotateEvent.get(event.getYaw(), event.getPitch());
            Aevum.EVENT_BUS.post(updateRotateEvent);
            event.setYaw(updateRotateEvent.getYaw());
            event.setPitch(updateRotateEvent.getPitch());
        }
    }

    @EventListener(priority=999)
    public void update(TickMovementEvent event) {
        if (RotationManager.mc.player == null) {
            return;
        }
        if (AntiCheat.INSTANCE.movementSync() && !BaritoneUtil.isActive()) {
            UpdateRotateEvent updateRotateEvent = UpdateRotateEvent.get(RotationManager.mc.player.getYRot(), RotationManager.mc.player.getXRot());
            Aevum.EVENT_BUS.post(updateRotateEvent);
            this.nextYaw = updateRotateEvent.getYaw();
            this.nextPitch = updateRotateEvent.getPitch();
            fixYaw = this.nextYaw;
            fixPitch = this.nextPitch;
        }
    }

    @EventListener(priority=-200)
    public void onLastRotation(UpdateRotateEvent event) {
        RotationEvent rotationEvent = RotationEvent.get();
        Aevum.EVENT_BUS.post(rotationEvent);
        if (rotationEvent.getRotation()) {
            float[] newAngle = this.injectStep(new float[]{rotationEvent.getYaw(), rotationEvent.getPitch()}, rotationEvent.getSpeed());
            event.setYaw(newAngle[0]);
            event.setPitch(newAngle[1]);
        } else if (rotationEvent.getTarget() != null) {
            float[] newAngle = this.injectStep(rotationEvent.getTarget(), rotationEvent.getSpeed());
            event.setYaw(newAngle[0]);
            event.setPitch(newAngle[1]);
        } else if (!event.isModified() && AntiCheat.INSTANCE.look.getValue() && directionVec != null && !ROTATE_TIMER.passed((long)(AntiCheat.INSTANCE.rotateTime.getValue() * 1000.0))) {
            float[] newAngle = this.injectStep(directionVec, AntiCheat.INSTANCE.steps.getValueFloat());
            event.setYaw(newAngle[0]);
            event.setPitch(newAngle[1]);
        }
    }

    @EventListener
    public void travel(TravelEvent e) {
        if (!AntiCheat.INSTANCE.movementSync()) {
            return;
        }
        if (BaritoneUtil.isActive()) {
            return;
        }
        if (RotationManager.mc.player.isHandsBusy()) {
            return;
        }
        if (e.isPre()) {
            this.prevYaw = RotationManager.mc.player.getYRot();
            this.prevPitch = RotationManager.mc.player.getXRot();
            RotationManager.mc.player.method_36456(fixYaw);
            RotationManager.mc.player.method_36457(fixPitch);
        } else {
            RotationManager.mc.player.method_36456(this.prevYaw);
            RotationManager.mc.player.method_36457(this.prevPitch);
        }
    }

    @EventListener
    public void onJump(JumpEvent e) {
        if (!AntiCheat.INSTANCE.movementSync()) {
            return;
        }
        if (BaritoneUtil.isActive()) {
            return;
        }
        if (RotationManager.mc.player.isHandsBusy()) {
            return;
        }
        if (e.isPre()) {
            this.prevYaw = RotationManager.mc.player.getYRot();
            this.prevPitch = RotationManager.mc.player.getXRot();
            RotationManager.mc.player.method_36456(fixYaw);
            RotationManager.mc.player.method_36457(fixPitch);
        } else {
            RotationManager.mc.player.method_36456(this.prevYaw);
            RotationManager.mc.player.method_36457(this.prevPitch);
        }
    }

    @EventListener
    public void onFirework(FireworkShooterRotationEvent event) {
        if (!AntiCheat.INSTANCE.movementSync()) {
            return;
        }
        if (BaritoneUtil.isActive()) {
            return;
        }
        if (event.shooter == RotationManager.mc.player) {
            event.yaw = fixYaw;
            event.pitch = fixPitch;
            event.cancel();
        }
    }

    @EventListener(priority=-999)
    public void onKeyInput(KeyboardInputEvent e) {
        if (!AntiCheat.INSTANCE.movementSync()) {
            return;
        }
        if (BaritoneUtil.isActive()) {
            return;
        }
        if (HoleSnap.INSTANCE.isOn()) {
            return;
        }
        if (RotationManager.mc.player.isHandsBusy() || Freecam.INSTANCE.isOn()) {
            return;
        }
        float mF = RotationManager.mc.player.input.forwardImpulse;
        float mS = RotationManager.mc.player.input.leftImpulse;
        float delta = (RotationManager.mc.player.getYRot() - fixYaw) * ((float)Math.PI / 180);
        float cos = Mth.method_15362((float)delta);
        float sin = Mth.method_15374((float)delta);
        RotationManager.mc.player.input.leftImpulse = Math.round(mS * cos - mF * sin);
        RotationManager.mc.player.input.forwardImpulse = Math.round(mF * cos + mS * sin);
    }

    public float[] injectStep(Vec3 vec, float steps) {
        float currentYaw = AntiCheat.INSTANCE.serverSide.getValue() ? this.getLastYaw() : this.rotationYaw;
        float currentPitch = AntiCheat.INSTANCE.serverSide.getValue() ? this.getLastPitch() : this.rotationPitch;
        float yawDelta = Mth.method_15393((float)((float)Mth.method_15338((double)(Math.toDegrees(Math.atan2(vec.z - RotationManager.mc.player.getZ(), vec.x - RotationManager.mc.player.getX())) - 90.0)) - currentYaw));
        float pitchDelta = (float)(-Math.toDegrees(Math.atan2(vec.y - (RotationManager.mc.player.position().y + (double)RotationManager.mc.player.method_18381(RotationManager.mc.player.getPose())), Math.sqrt(Math.pow(vec.x - RotationManager.mc.player.getX(), 2.0) + Math.pow(vec.z - RotationManager.mc.player.getZ(), 2.0))))) - currentPitch;
        if (AntiCheat.INSTANCE.random.getValue()) {
            float angleToRad = (float)Math.toRadians(27 * (RotationManager.mc.player.tickCount % 30));
            yawDelta = (float)((double)yawDelta + Math.sin(angleToRad) * 3.0) + MathUtil.random(-1.0f, 1.0f);
            pitchDelta += MathUtil.random(-0.6f, 0.6f);
        }
        if (yawDelta > 180.0f) {
            yawDelta -= 180.0f;
        }
        float yawStepVal = 180.0f * steps;
        float clampedYawDelta = Mth.method_15363((float)Mth.method_15379((float)yawDelta), (float)(-yawStepVal), (float)yawStepVal);
        float clampedPitchDelta = Mth.method_15363((float)pitchDelta, (float)-45.0f, (float)45.0f);
        float newYaw = currentYaw + (yawDelta > 0.0f ? clampedYawDelta : -clampedYawDelta);
        float newPitch = Mth.method_15363((float)(currentPitch + clampedPitchDelta), (float)-90.0f, (float)90.0f);
        return new float[]{newYaw, newPitch};
    }

    public float[] injectStep(float[] angle, float steps) {
        float currentYaw = AntiCheat.INSTANCE.serverSide.getValue() ? this.getLastYaw() : this.rotationYaw;
        float currentPitch = AntiCheat.INSTANCE.serverSide.getValue() ? this.getLastPitch() : this.rotationPitch;
        float yawDelta = Mth.method_15393((float)(angle[0] - currentYaw));
        float pitchDelta = angle[1] - currentPitch;
        if (AntiCheat.INSTANCE.random.getValue()) {
            float angleToRad = (float)Math.toRadians(27 * (RotationManager.mc.player.tickCount % 30));
            yawDelta = (float)((double)yawDelta + Math.sin(angleToRad) * 3.0) + MathUtil.random(-1.0f, 1.0f);
            pitchDelta += MathUtil.random(-0.6f, 0.6f);
        }
        if (yawDelta > 180.0f) {
            yawDelta -= 180.0f;
        }
        float yawStepVal = 180.0f * steps;
        float pitchStepVal = 90.0f * steps;
        float clampedYawDelta = Mth.method_15363((float)Mth.method_15379((float)yawDelta), (float)(-yawStepVal), (float)yawStepVal);
        float clampedPitchDelta = Mth.method_15363((float)pitchDelta, (float)(-pitchStepVal), (float)pitchStepVal);
        float newYaw = currentYaw + (yawDelta > 0.0f ? clampedYawDelta : -clampedYawDelta);
        float newPitch = Mth.method_15363((float)(currentPitch + clampedPitchDelta), (float)-90.0f, (float)90.0f);
        return new float[]{newYaw, newPitch};
    }

    @EventListener(priority=-999)
    public void onPacketSend(PacketEvent.Sent event) {
        ServerboundMovePlayerPacket packet;
        if (RotationManager.mc.player == null) {
            return;
        }
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof ServerboundMovePlayerPacket && (packet = (ServerboundMovePlayerPacket)packet2).hasRotation()) {
            this.setLastYaw(packet.method_12271(this.getLastYaw()));
            this.setLastPitch(packet.method_12270(this.getLastPitch()));
            this.setRenderRotation(this.getLastYaw(), this.getLastPitch(), ClientSetting.INSTANCE.sync.getValue());
        }
    }

    @EventListener
    public void onUpdateWalkingPost(TickEvent event) {
        if (event.isPost()) {
            this.setRenderRotation(this.getLastYaw(), this.getLastPitch(), false);
        }
    }

    public void setRenderRotation(float yaw, float pitch, boolean force) {
        if (RotationManager.mc.player == null) {
            return;
        }
        if (RotationManager.mc.player.tickCount == this.ticksExisted && !force) {
            return;
        }
        this.ticksExisted = RotationManager.mc.player.tickCount;
        prevRenderPitch = renderPitch;
        prevRenderYawOffset = renderYawOffset;
        renderYawOffset = this.getRenderYawOffset(yaw, prevRenderYawOffset);
        prevRotationYawHead = rotationYawHead;
        rotationYawHead = yaw;
        renderPitch = pitch;
    }

    private float getRenderYawOffset(float yaw, float offsetIn) {
        float f;
        float offset;
        double zDif;
        double d;
        float result = offsetIn;
        double xDif = RotationManager.mc.player.getX() - RotationManager.mc.player.xo;
        if (xDif * xDif + d * (zDif = RotationManager.mc.player.getZ() - RotationManager.mc.player.zo) > 0.002500000176951289) {
            offset = (float)Mth.method_15349((double)zDif, (double)xDif) * 57.295776f - 90.0f;
            float wrap = Mth.method_15379((float)(Mth.method_15393((float)yaw) - offset));
            float f2 = result = 95.0f < wrap && wrap < 265.0f ? offset - 180.0f : offset;
        }
        if (RotationManager.mc.player.attackAnim > 0.0f) {
            result = yaw;
        }
        result = offsetIn + Mth.method_15393((float)(result - offsetIn)) * 0.3f;
        offset = Mth.method_15393((float)(yaw - result));
        if (f < -75.0f) {
            offset = -75.0f;
        } else if (offset >= 75.0f) {
            offset = 75.0f;
        }
        result = yaw - offset;
        if (offset * offset > 2500.0f) {
            result += offset * 0.2f;
        }
        return result;
    }

    public static float[] getRotation(Vec3 eyesPos, Vec3 vec) {
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[]{Mth.method_15393((float)yaw), Mth.method_15393((float)pitch)};
    }

    public static float[] getRotation(Vec3 vec) {
        Vec3 eyesPos = RotationManager.mc.player.getEyePosition();
        return RotationManager.getRotation(eyesPos, vec);
    }

    public static float getRenderPitch() {
        return renderPitch;
    }

    public static float getRotationYawHead() {
        return rotationYawHead;
    }

    public static float getRenderYawOffset() {
        return renderYawOffset;
    }

    public static float getPrevRenderPitch() {
        return prevRenderPitch;
    }

    public static float getPrevRotationYawHead() {
        return prevRotationYawHead;
    }

    public static float getPrevRenderYawOffset() {
        return prevRenderYawOffset;
    }

    public float getLastYaw() {
        return this.lastYaw;
    }

    public void setLastYaw(float lastYaw) {
        this.lastYaw = lastYaw;
        if (AntiCheat.INSTANCE.forceSync.getValue() && Aevum.SERVER.playerNull.passedS(0.15)) {
            ((IClientPlayerEntity)RotationManager.mc.player).setLastYaw(lastYaw);
        }
    }

    public float getLastPitch() {
        return this.lastPitch;
    }

    public void setLastPitch(float lastPitch) {
        this.lastPitch = lastPitch;
        if (AntiCheat.INSTANCE.forceSync.getValue() && Aevum.SERVER.playerNull.passedS(0.15)) {
            ((IClientPlayerEntity)RotationManager.mc.player).setLastPitch(lastPitch);
        }
    }
}

