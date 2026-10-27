/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.ProjectileUtil
 *  net.minecraft.AABB
 *  net.minecraft.HitResult
 *  net.minecraft.Vec3
 *  net.minecraft.Mth
 *  net.minecraft.ClipContext
 *  net.minecraft.ClipContext$Fluid
 *  net.minecraft.ClipContext$Block
 *  net.minecraft.EntityHitResult
 */
package dev.aevum.api.utils.world;

import dev.aevum.api.utils.Wrapper;
import net.minecraft.Entity;
import net.minecraft.LivingEntity;
import net.minecraft.ProjectileUtil;
import net.minecraft.AABB;
import net.minecraft.HitResult;
import net.minecraft.Vec3;
import net.minecraft.Mth;
import net.minecraft.ClipContext;
import net.minecraft.EntityHitResult;

public class InteractUtil
implements Wrapper {
    public static HitResult getRtxTarget(float yaw, float pitch, double x, double y, double z) {
        AABB box;
        HitResult result = InteractUtil.rayTrace(5.0, yaw, pitch, x, y, z);
        Vec3 vec3d = new Vec3(x, y, z);
        double distancePow2 = 25.0;
        if (result != null) {
            distancePow2 = result.getLocation().method_1025(vec3d);
        }
        Vec3 vec3d2 = InteractUtil.getRotationVector(pitch, yaw);
        Vec3 vec3d3 = vec3d.method_1031(vec3d2.x * 5.0, vec3d2.y * 5.0, vec3d2.z * 5.0);
        EntityHitResult entityHitResult = ProjectileUtil.method_18075((Entity)InteractUtil.mc.player, (Vec3)vec3d, (Vec3)vec3d3, (AABB)(box = new AABB(x - 0.3, y, z - 0.3, x + 0.3, y + 1.8, z + 0.3).method_18804(vec3d2.method_1021(5.0)).method_1009(1.0, 1.0, 1.0)), entity -> !entity.isSpectator() && entity.isPickable(), (double)distancePow2);
        if (entityHitResult != null) {
            Entity entity2 = entityHitResult.getEntity();
            Vec3 vec3d4 = entityHitResult.getLocation();
            double g = vec3d.method_1025(vec3d4);
            if ((g < distancePow2 || result == null) && entity2 instanceof LivingEntity) {
                return entityHitResult;
            }
        }
        return result;
    }

    public static HitResult rayTrace(double dst, float yaw, float pitch, double x, double y, double z) {
        Vec3 vec3d = new Vec3(x, y, z);
        Vec3 vec3d2 = InteractUtil.getRotationVector(pitch, yaw);
        Vec3 vec3d3 = vec3d.method_1031(vec3d2.x * dst, vec3d2.y * dst, vec3d2.z * dst);
        return InteractUtil.mc.level.method_17742(new ClipContext(vec3d, vec3d3, ClipContext.ClipContext$Block.OUTLINE, ClipContext.ClipContext$Fluid.NONE, (Entity)InteractUtil.mc.player));
    }

    private static Vec3 getRotationVector(float yaw, float pitch) {
        return new Vec3((double)(Mth.method_15374((float)(-pitch * ((float)Math.PI / 180))) * Mth.method_15362((float)(yaw * ((float)Math.PI / 180)))), (double)(-Mth.method_15374((float)(yaw * ((float)Math.PI / 180)))), (double)(Mth.method_15362((float)(-pitch * ((float)Math.PI / 180))) * Mth.method_15362((float)(yaw * ((float)Math.PI / 180)))));
    }
}

