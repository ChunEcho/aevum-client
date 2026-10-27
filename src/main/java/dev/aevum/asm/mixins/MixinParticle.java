/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Particle
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 */
package dev.aevum.asm.mixins;

import net.minecraft.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={Particle.class})
public abstract class MixinParticle {
    @Shadow
    protected double xd;
    @Shadow
    protected double yd;
    @Shadow
    protected double zd;

    @Shadow
    public abstract void method_3084(float var1, float var2, float var3);

    @Shadow
    protected void setAlpha(float alpha) {
    }
}

