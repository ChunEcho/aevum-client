/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Camera
 *  net.minecraft.GameRenderer
 *  org.joml.Matrix4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package dev.aevum.asm.accessors;

import net.minecraft.Camera;
import net.minecraft.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={GameRenderer.class})
public interface IGameRenderer {
    @Invoker(value="renderHand")
    public void IRenderHand(Camera var1, float var2, Matrix4f var3);
}

