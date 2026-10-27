/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.MobEffectInstance
 *  net.minecraft.MobEffects
 */
package dev.aevum.api.utils.player;

import dev.aevum.api.utils.Wrapper;
import dev.aevum.asm.accessors.IVec3d;
import dev.aevum.mod.modules.impl.movement.HoleSnap;
import net.minecraft.MobEffectInstance;
import net.minecraft.MobEffects;

public class MovementUtil
implements Wrapper {
    public static boolean isMoving() {
        return (double)MovementUtil.mc.player.input.forwardImpulse != 0.0 || (double)MovementUtil.mc.player.input.leftImpulse != 0.0 || HoleSnap.INSTANCE.isOn();
    }

    public static boolean isStatic() {
        return MovementUtil.mc.player.getDeltaMovement().method_10216() == 0.0 && MovementUtil.mc.player.onGround() && MovementUtil.mc.player.getDeltaMovement().method_10215() == 0.0;
    }

    public static boolean isJumping() {
        return MovementUtil.mc.player.input.jumping;
    }

    public static double getDistance2D() {
        double xDist = MovementUtil.mc.player.getX() - MovementUtil.mc.player.xo;
        double zDist = MovementUtil.mc.player.getZ() - MovementUtil.mc.player.zo;
        return Math.sqrt(xDist * xDist + zDist * zDist);
    }

    public static double getJumpSpeed() {
        double defaultSpeed = 0.0;
        if (MovementUtil.mc.player.method_6059(MobEffects.JUMP)) {
            int amplifier = ((MobEffectInstance)MovementUtil.mc.player.getActiveEffectsMap().get(MobEffects.JUMP)).method_5578();
            defaultSpeed += (double)(amplifier + 1) * 0.1;
        }
        return defaultSpeed;
    }

    public static double[] directionSpeed(double speed) {
        float forward = MovementUtil.mc.player.input.forwardImpulse;
        float side = MovementUtil.mc.player.input.leftImpulse;
        return MovementUtil.directionSpeed(speed, forward, side);
    }

    private static double[] directionSpeed(double speed, float forward, float side) {
        float yaw = MovementUtil.mc.player.yRotO + (MovementUtil.mc.player.getYRot() - MovementUtil.mc.player.yRotO) * mc.getTimer().method_60637(true);
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += (float)(forward > 0.0f ? -45 : 45);
            } else if (side < 0.0f) {
                yaw += (float)(forward > 0.0f ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        double posX = (double)forward * speed * cos + (double)side * speed * sin;
        double posZ = (double)forward * speed * sin - (double)side * speed * cos;
        return new double[]{posX, posZ};
    }

    public static double getMotionX() {
        return MovementUtil.mc.player.getDeltaMovement().x;
    }

    public static void setMotionX(double x) {
        ((IVec3d)MovementUtil.mc.player.getDeltaMovement()).setX(x);
    }

    public static double getMotionY() {
        return MovementUtil.mc.player.getDeltaMovement().y;
    }

    public static void setMotionY(double y) {
        ((IVec3d)MovementUtil.mc.player.getDeltaMovement()).setY(y);
    }

    public static double getMotionZ() {
        return MovementUtil.mc.player.getDeltaMovement().z;
    }

    public static void setMotionZ(double z) {
        ((IVec3d)MovementUtil.mc.player.getDeltaMovement()).setZ(z);
    }

    public static double getSpeed(boolean slowness) {
        double defaultSpeed = 0.2873;
        return MovementUtil.getSpeed(slowness, defaultSpeed);
    }

    public static double getSpeed(boolean slowness, double defaultSpeed) {
        int amplifier;
        if (MovementUtil.mc.player.method_6059(MobEffects.MOVEMENT_SPEED)) {
            amplifier = ((MobEffectInstance)MovementUtil.mc.player.getActiveEffectsMap().get(MobEffects.MOVEMENT_SPEED)).method_5578();
            defaultSpeed *= 1.0 + 0.2 * (double)(amplifier + 1);
        }
        if (slowness && MovementUtil.mc.player.method_6059(MobEffects.MOVEMENT_SLOWDOWN)) {
            amplifier = ((MobEffectInstance)MovementUtil.mc.player.getActiveEffectsMap().get(MobEffects.MOVEMENT_SLOWDOWN)).method_5578();
            defaultSpeed /= 1.0 + 0.2 * (double)(amplifier + 1);
        }
        if (MovementUtil.mc.player.isShiftKeyDown()) {
            defaultSpeed /= 5.0;
        }
        return defaultSpeed;
    }
}

