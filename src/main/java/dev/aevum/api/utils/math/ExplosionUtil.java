/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.LivingEntity
 *  net.minecraft.BlockPos
 *  net.minecraft.Vec3
 */
package dev.aevum.api.utils.math;

import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.math.DamageUtils;
import net.minecraft.LivingEntity;
import net.minecraft.BlockPos;
import net.minecraft.Vec3;

public class ExplosionUtil
implements Wrapper {
    public static float anchorDamage(BlockPos pos, LivingEntity target, LivingEntity predict) {
        return DamageUtils.anchorDamage(target, predict, pos.method_46558());
    }

    public static float calculateDamage(Vec3 pos, LivingEntity entity, LivingEntity predict, float power) {
        return DamageUtils.explosionDamage(entity, predict, pos, power * 2.0f);
    }
}

