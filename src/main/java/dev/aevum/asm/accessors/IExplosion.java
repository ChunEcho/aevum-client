/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.DamageSource
 *  net.minecraft.Entity
 *  net.minecraft.Explosion
 *  net.minecraft.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.aevum.asm.accessors;

import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.Explosion;
import net.minecraft.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={Explosion.class})
public interface IExplosion {
    @Mutable
    @Accessor(value="x")
    public void setX(double var1);

    @Mutable
    @Accessor(value="y")
    public void setY(double var1);

    @Mutable
    @Accessor(value="z")
    public void setZ(double var1);

    @Mutable
    @Accessor(value="power")
    public void setPower(float var1);

    @Mutable
    @Accessor(value="entity")
    public void setEntity(Entity var1);

    @Mutable
    @Accessor(value="world")
    public Level getWorld();

    @Mutable
    @Accessor(value="world")
    public void setWorld(Level var1);

    @Mutable
    @Accessor(value="damageSource")
    public DamageSource getDamageSource();
}

