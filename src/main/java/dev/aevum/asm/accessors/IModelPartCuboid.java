/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ModelPart$Polygon
 *  net.minecraft.ModelPart$Cube
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.aevum.asm.accessors;

import net.minecraft.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ModelPart.ModelPart$Cube.class})
public interface IModelPartCuboid {
    @Accessor(value="sides")
    public ModelPart.ModelPart$Polygon[] getSides();
}

