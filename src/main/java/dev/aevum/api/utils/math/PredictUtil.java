/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Player
 *  net.minecraft.BlockPos
 *  net.minecraft.AABB
 *  net.minecraft.Vec3
 */
package dev.aevum.api.utils.math;

import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.world.BlockPosX;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.asm.accessors.IEntity;
import dev.aevum.mod.modules.impl.client.AntiCheat;
import net.minecraft.Entity;
import net.minecraft.Player;
import net.minecraft.BlockPos;
import net.minecraft.AABB;
import net.minecraft.Vec3;

public class PredictUtil
implements Wrapper {
    public static Vec3 getPos(Player entity, int ticks) {
        if (ticks <= 0) {
            return entity.position();
        }
        return PredictUtil.getPos(entity, AntiCheat.INSTANCE.maxMotionY.getValue(), AntiCheat.INSTANCE.predictTicks.getValueInt(), AntiCheat.INSTANCE.simulation.getValueInt(), AntiCheat.INSTANCE.step.getValue(), AntiCheat.INSTANCE.doubleStep.getValue(), AntiCheat.INSTANCE.jump.getValue(), AntiCheat.INSTANCE.inBlockPause.getValue());
    }

    public static Vec3 getPos(Player e, double maxMotionY, int ticks, int simulation, boolean step, boolean doubleStep, boolean jump, boolean inBlockPause) {
        double velocityZ;
        double velocityY;
        double velocityX;
        if (inBlockPause && BlockUtil.canCollide((Entity)e, e.method_5829())) {
            return e.position();
        }
        if (AntiCheat.INSTANCE.motion.is(AntiCheat.Motion.Position)) {
            velocityX = e.getX() - e.xo;
            velocityY = e.getY() - e.yo;
            velocityZ = e.getZ() - e.zo;
            if (velocityY > maxMotionY) {
                velocityY = maxMotionY;
            }
        } else {
            velocityX = e.getDeltaMovement().x;
            velocityY = e.getDeltaMovement().y;
            velocityZ = e.getDeltaMovement().z;
        }
        double motionX = velocityX;
        double motionY = velocityY;
        double motionZ = velocityZ;
        double x = e.getX();
        double y = e.getY();
        double z = e.getZ();
        Vec3 lastPos = new Vec3(x, y, z);
        if (motionX == 0.0 && motionY == 0.0 && motionZ == 0.0) {
            return lastPos;
        }
        for (int i = 0; i < ticks; ++i) {
            lastPos = new Vec3(x, y, z);
            boolean move = false;
            boolean fall = false;
            block1: for (int yTime = simulation; yTime >= 0; --yTime) {
                for (int xTime = simulation; xTime >= 0; --xTime) {
                    double xFactor = (double)xTime / (double)simulation;
                    double yFactor = (double)yTime / (double)simulation;
                    if (!PredictUtil.canMove(lastPos.method_1031(motionX * xFactor, motionY * yFactor, motionZ * xFactor), e)) continue;
                    if (Math.abs(motionX * xFactor) + Math.abs(motionZ * xFactor) + Math.abs(motionY * yFactor) <= 0.05) {
                        if (step && !PredictUtil.canMove(lastPos.method_1031(velocityX, 0.0, velocityZ), e) && PredictUtil.canMove(lastPos.method_1031(velocityX, 1.1, velocityZ), e)) {
                            y += 1.05;
                            motionY = 0.03;
                            for (yTime2 = simulation; yTime2 >= 0; --yTime2) {
                                for (xTime2 = simulation; xTime2 >= 0; --xTime2) {
                                    xFactor2 = (double)xTime2 / (double)simulation;
                                    yFactor2 = (double)yTime2 / (double)simulation;
                                    if (!PredictUtil.canMove(lastPos.method_1031(motionX * xFactor2, motionY * yFactor2, motionZ * xFactor2), e)) continue;
                                    move = true;
                                    x += motionX * xFactor2;
                                    z += motionZ * xFactor2;
                                    if (yTime2 <= 0) break block1;
                                    y += motionY * yFactor2;
                                    fall = true;
                                    break block1;
                                }
                            }
                        } else if (doubleStep && !PredictUtil.canMove(lastPos.method_1031(velocityX, 0.0, velocityZ), e) && PredictUtil.canMove(lastPos.method_1031(velocityX, 2.1, velocityZ), e)) {
                            y += 2.05;
                            motionY = 0.03;
                            for (yTime2 = simulation; yTime2 >= 0; --yTime2) {
                                for (xTime2 = simulation; xTime2 >= 0; --xTime2) {
                                    xFactor2 = (double)xTime2 / (double)simulation;
                                    yFactor2 = (double)yTime2 / (double)simulation;
                                    if (!PredictUtil.canMove(lastPos.method_1031(motionX * xFactor2, motionY * yFactor2, motionZ * xFactor2), e)) continue;
                                    move = true;
                                    x += motionX * xFactor2;
                                    z += motionZ * xFactor2;
                                    if (yTime2 <= 0) break block1;
                                    y += motionY * yFactor2;
                                    fall = true;
                                    break block1;
                                }
                            }
                        }
                        return lastPos;
                    }
                    move = true;
                    x += motionX * xFactor;
                    z += motionZ * xFactor;
                    if (yTime <= 0) break block1;
                    y += motionY * yFactor;
                    fall = true;
                    break block1;
                }
            }
            if (!move) {
                return lastPos;
            }
            if (!e.isFallFlying()) {
                motionX *= 0.99;
                motionZ *= 0.99;
                motionY *= 0.99;
                motionY -= (double)0.05f;
            }
            if (fall) continue;
            if (e.onGround()) {
                motionX = velocityX;
                motionZ = velocityZ;
                motionY = 0.0;
                continue;
            }
            if (jump) {
                motionX = velocityX;
                motionZ = velocityZ;
                motionY = 0.333;
                continue;
            }
            motionY = 0.0;
        }
        return lastPos;
    }

    public static boolean canMove(Vec3 pos, Player player) {
        return !BlockUtil.canCollide((Entity)player, ((IEntity)player).getDimensions().method_30757(pos)) || new AABB((BlockPos)new BlockPosX(pos)).method_994(player.method_5829());
    }
}

