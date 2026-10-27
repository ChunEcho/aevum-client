/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Vec3
 *  net.minecraft.EntityDimensions
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.aevum.asm.accessors;

import net.minecraft.Entity;
import net.minecraft.Vec3;
import net.minecraft.EntityDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={Entity.class})
public interface IEntity {
    @Mutable
    @Accessor(value="pos")
    public void setPos(Vec3 var1);

    @Accessor(value="dimensions")
    public EntityDimensions getDimensions();
}

