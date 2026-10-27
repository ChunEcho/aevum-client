/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockAndTintGetter
 *  net.minecraft.BlockPos
 *  net.minecraft.BlockState
 *  net.minecraft.FluidState
 *  net.minecraft.VertexConsumer
 *  net.minecraft.LiquidBlockRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.mod.modules.impl.render.Xray;
import net.minecraft.BlockAndTintGetter;
import net.minecraft.BlockPos;
import net.minecraft.BlockState;
import net.minecraft.FluidState;
import net.minecraft.VertexConsumer;
import net.minecraft.LiquidBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LiquidBlockRenderer.class})
public abstract class MixinFluidRenderer {
    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRender(BlockAndTintGetter world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo info) {
        if (Xray.shouldBlock(fluidState.createLegacyBlock())) {
            info.cancel();
        }
    }
}

