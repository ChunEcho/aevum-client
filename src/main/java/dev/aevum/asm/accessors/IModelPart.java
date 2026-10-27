/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ModelPart
 *  net.minecraft.ModelPart$Cube
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.aevum.asm.accessors;

import java.util.List;
import java.util.Map;
import net.minecraft.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ModelPart.class})
public interface IModelPart {
    @Accessor(value="cuboids")
    public List<ModelPart.ModelPart$Cube> getCuboids();

    @Accessor(value="children")
    public Map<String, ModelPart> getChildren();
}

