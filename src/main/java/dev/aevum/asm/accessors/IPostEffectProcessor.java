/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.PostChain
 *  net.minecraft.PostPass
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.aevum.asm.accessors;

import java.util.List;
import net.minecraft.PostChain;
import net.minecraft.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={PostChain.class})
public interface IPostEffectProcessor {
    @Accessor
    public List<PostPass> getPasses();
}

