/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockPos
 *  net.minecraft.LevelLightEngine
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.aevum.asm.mixins;

import dev.aevum.mod.modules.impl.render.NoRender;
import net.minecraft.BlockPos;
import net.minecraft.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LevelLightEngine.class})
public class MixinLightingProvider {
    @Inject(at={@At(value="HEAD")}, method={"checkBlock"}, cancellable=true)
    public void checkBlock(BlockPos pos, CallbackInfo ci) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.lightsUpdate.getValue()) {
            ci.cancel();
        }
    }

    @Inject(at={@At(value="RETURN")}, method={"doLightUpdates"}, cancellable=true)
    public void doLightUpdates(CallbackInfoReturnable<Integer> cir) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.lightsUpdate.getValue()) {
            cir.setReturnValue((Object)0);
        }
    }
}

