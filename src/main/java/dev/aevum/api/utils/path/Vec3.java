/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Vec3
 */
package dev.aevum.api.utils.path;

import net.minecraft.Vec3;

public record Vec3(double x, double y, double z) {
    public Vec3 addVector(double x, double y, double z) {
        return new Vec3(this.x + x, this.y + y, this.z + z);
    }

    public Vec3 floor() {
        return new Vec3(Math.floor(this.x), Math.floor(this.y), Math.floor(this.z));
    }

    public double squareDistanceTo(Vec3 v) {
        return Math.pow(v.x - this.x, 2.0) + Math.pow(v.y - this.y, 2.0) + Math.pow(v.z - this.z, 2.0);
    }

    public Vec3 add(Vec3 v) {
        return this.addVector(v.x(), v.y(), v.z());
    }

    public Vec3 mc() {
        return new Vec3(this.x, this.y, this.z);
    }

    @Override
    public String toString() {
        return "[" + this.x + ";" + this.y + ";" + this.z + "]";
    }

    @Override
    public boolean equals(Object obj) {
        double x1;
        double d;
        if (!(obj instanceof Vec3)) {
            return false;
        }
        Vec3 vec3 = (Vec3)obj;
        try {
            x1 = d = vec3.x();
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
        double y1 = d = vec3.y();
        double z1 = d = vec3.z();
        if (this.x != x1) {
            return false;
        }
        if (this.y != y1) {
            return false;
        }
        return this.z == z1;
    }
}

