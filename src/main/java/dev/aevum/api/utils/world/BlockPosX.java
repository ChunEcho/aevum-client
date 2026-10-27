/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockPos
 *  net.minecraft.Vec3
 *  net.minecraft.Mth
 */
package dev.aevum.api.utils.world;

import net.minecraft.BlockPos;
import net.minecraft.Vec3;
import net.minecraft.Mth;

public class BlockPosX
extends BlockPos {
    public BlockPosX(double x, double y, double z) {
        super(Mth.method_15357((double)x), Mth.method_15357((double)y), Mth.method_15357((double)z));
    }

    public BlockPosX(double x, double y, double z, boolean fix) {
        this(x, y + (fix ? 0.3 : 0.0), z);
    }

    public BlockPosX(Vec3 vec3d) {
        this(vec3d.x, vec3d.y, vec3d.z);
    }

    public BlockPosX(Vec3 vec3d, boolean fix) {
        this(vec3d.x, vec3d.y, vec3d.z, fix);
    }
}

