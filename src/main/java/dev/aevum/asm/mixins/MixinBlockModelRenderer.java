/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BakedModel
 *  net.minecraft.BlockAndTintGetter
 *  net.minecraft.BlockPos
 *  net.minecraft.BlockState
 *  net.minecraft.PoseStack
 *  net.minecraft.VertexConsumer
 *  net.minecraft.RandomSource
 *  net.minecraft.ModelBlockRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.mod.modules.impl.render.Xray;
import net.minecraft.BakedModel;
import net.minecraft.BlockAndTintGetter;
import net.minecraft.BlockPos;
import net.minecraft.BlockState;
import net.minecraft.PoseStack;
import net.minecraft.VertexConsumer;
import net.minecraft.RandomSource;
import net.minecraft.ModelBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ModelBlockRenderer.class})
public abstract class MixinBlockModelRenderer {
    @Inject(method={"renderSmooth", "renderFlat"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderSmooth(BlockAndTintGetter world, BakedModel model, BlockState state, BlockPos pos, PoseStack matrices, VertexConsumer vertexConsumer, boolean cull, RandomSource random, long seed, int overlay, CallbackInfo info) {
        if (Xray.shouldBlock(state)) {
            info.cancel();
        }
    }
}

