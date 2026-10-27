/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.AABB
 *  net.minecraft.Vec3
 *  net.minecraft.Mth
 */
package dev.aevum.api.utils.math;

import dev.aevum.api.utils.Wrapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.Entity;
import net.minecraft.LivingEntity;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.AABB;
import net.minecraft.Vec3;
import net.minecraft.Mth;

public class MathUtil
implements Wrapper {
    public static float clamp(float num, float min, float max) {
        return num < min ? min : Math.min(num, max);
    }

    public static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    public static double round(double value, int places) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double square(double input) {
        return input * input;
    }

    public static float random(float min, float max) {
        return (float)(Math.random() * (double)(max - min) + (double)min);
    }

    public static double random(double min, double max) {
        return (float)(Math.random() * (max - min) + min);
    }

    public static float rad(float angle) {
        return (float)((double)angle * Math.PI / 180.0);
    }

    public static double interpolate(double previous, double current, double delta) {
        return previous + (current - previous) * delta;
    }

    public static float interpolate(float previous, float current, float delta) {
        return previous + (current - previous) * delta;
    }

    public static Direction getFacingOrder(float yaw, float pitch) {
        float f = pitch * ((float)Math.PI / 180);
        float g = -yaw * ((float)Math.PI / 180);
        float h = Mth.method_15374((float)f);
        float i = Mth.method_15362((float)f);
        float j = Mth.method_15374((float)g);
        float k = Mth.method_15362((float)g);
        boolean bl = j > 0.0f;
        boolean bl2 = h < 0.0f;
        boolean bl3 = k > 0.0f;
        float l = bl ? j : -j;
        float m = bl2 ? -h : h;
        float n = bl3 ? k : -k;
        float o = l * i;
        float p = n * i;
        Direction direction = bl ? Direction.EAST : Direction.WEST;
        Direction direction2 = bl2 ? Direction.UP : Direction.DOWN;
        Direction direction3 = bl3 ? Direction.SOUTH : Direction.NORTH;
        Direction direction4 = direction3;
        if (l > n) {
            if (m > o) {
                return direction2;
            }
            return direction;
        }
        if (m > p) {
            return direction2;
        }
        return direction3;
    }

    public static Direction getDirectionFromEntityLiving(BlockPos pos, LivingEntity entity) {
        if (Math.abs(entity.getX() - ((double)pos.method_10263() + 0.5)) < 2.0 && Math.abs(entity.getZ() - ((double)pos.getZ() + 0.5)) < 2.0) {
            double d0 = entity.getY() + (double)entity.method_18381(entity.getPose());
            if (d0 - (double)pos.method_10264() > 2.0) {
                return Direction.UP;
            }
            if ((double)pos.method_10264() - d0 > 0.0) {
                return Direction.DOWN;
            }
        }
        return entity.getDirection().getOpposite();
    }

    public static Vec3 getRenderPosition(Entity entity) {
        return MathUtil.getRenderPosition(entity, mc.getTimer().method_60637(true));
    }

    public static Vec3 getRenderPosition(Entity entity, float tickDelta) {
        return new Vec3(entity.xo + (entity.getX() - entity.xo) * (double)tickDelta, entity.yo + (entity.getY() - entity.yo) * (double)tickDelta, entity.zo + (entity.getZ() - entity.zo) * (double)tickDelta);
    }

    public static double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dX = x2 - x1;
        double dY = y2 - y1;
        double dZ = z2 - z1;
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public static Vec3 getPointToBoxFromBottom(Vec3 eye, Vec3 bottom, double range, double boxSize, double step) {
        Vec3 target = null;
        double halfWidth = boxSize / 2.0;
        double minOffset = Double.MAX_VALUE;
        boolean xPlus = bottom.x < eye.x;
        boolean zPlus = bottom.z < eye.z;
        double rangeSq = range * range;
        for (double yOffset = 0.0; yOffset <= boxSize; yOffset += step) {
            for (double xOffset = 0.0; xOffset <= halfWidth; xOffset += step) {
                for (double zOffset = 0.0; zOffset <= halfWidth; zOffset += step) {
                    double y = bottom.y + yOffset;
                    if (yOffset != 0.0 && y > eye.y) {
                        return target;
                    }
                    double x = bottom.x + (xPlus ? xOffset : -xOffset);
                    double z = bottom.z + (zPlus ? zOffset : -zOffset);
                    double dxToEye = x - eye.x;
                    double dyToEye = y - eye.y;
                    double dzToEye = z - eye.z;
                    double distSq = dxToEye * dxToEye + dyToEye * dyToEye + dzToEye * dzToEye;
                    double offsets = xOffset + yOffset + zOffset;
                    if (!(distSq <= rangeSq) || !(offsets < minOffset)) continue;
                    minOffset = offsets;
                    target = new Vec3(x, y, z);
                }
            }
        }
        return target;
    }

    public static Vec3 getClosestPointToBox(Vec3 pos, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        double closestX = Math.max(minX, Math.min(pos.x, maxX));
        double closestY = Math.max(minY, Math.min(pos.y, maxY));
        double closestZ = Math.max(minZ, Math.min(pos.z, maxZ));
        return new Vec3(closestX, closestY, closestZ);
    }

    public static Vec3 getClosestPointToBox(Vec3 eyePos, AABB boundingBox) {
        return MathUtil.getClosestPointToBox(eyePos, boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
    }

    public static Vec3 getClosestPoint(Entity entity) {
        return MathUtil.getClosestPointToBox(MathUtil.mc.player.getEyePosition(), entity.method_5829());
    }
}

