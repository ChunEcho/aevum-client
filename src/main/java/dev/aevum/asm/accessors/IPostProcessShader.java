/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.RenderTarget
 *  net.minecraft.PostPass
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.aevum.asm.accessors;

import net.minecraft.RenderTarget;
import net.minecraft.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={PostPass.class})
public interface IPostProcessShader {
    @Mutable
    @Accessor(value="input")
    public void setInput(RenderTarget var1);

    @Mutable
    @Accessor(value="output")
    public void setOutput(RenderTarget var1);
}

