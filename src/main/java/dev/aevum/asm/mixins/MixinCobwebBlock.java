/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Level
 *  net.minecraft.BlockPos
 *  net.minecraft.Vec3
 *  net.minecraft.WebBlock
 *  net.minecraft.BlockState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.api.utils.Wrapper;
import dev.aevum.mod.modules.impl.movement.FastWeb;
import net.minecraft.Entity;
import net.minecraft.Level;
import net.minecraft.BlockPos;
import net.minecraft.Vec3;
import net.minecraft.WebBlock;
import net.minecraft.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={WebBlock.class})
public class MixinCobwebBlock {
    @Inject(at={@At(value="HEAD")}, method={"onEntityCollision"}, cancellable=true)
    private void onGetVelocityMultiplier(BlockState state, Level world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (FastWeb.INSTANCE.isOn() && (Wrapper.mc.options.keyShift.isDown() || !FastWeb.INSTANCE.onlySneak.getValue())) {
            if (FastWeb.INSTANCE.mode.is(FastWeb.Mode.Ignore)) {
                ci.cancel();
                entity.method_38785();
            } else if (FastWeb.INSTANCE.mode.is(FastWeb.Mode.Custom)) {
                ci.cancel();
                entity.method_5844(state, new Vec3(FastWeb.INSTANCE.xZSlow.getValue() / 100.0, FastWeb.INSTANCE.ySlow.getValue() / 100.0, FastWeb.INSTANCE.xZSlow.getValue() / 100.0));
            }
        }
    }
}

