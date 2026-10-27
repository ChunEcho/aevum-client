/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.BlockGetter
 *  net.minecraft.ItemLike
 *  net.minecraft.Block
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.BlockState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.aevum.asm.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.aevum.mod.modules.impl.movement.NoSlow;
import dev.aevum.mod.modules.impl.render.Xray;
import net.minecraft.BlockGetter;
import net.minecraft.ItemLike;
import net.minecraft.Block;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Block.class})
public abstract class MixinBlock
implements ItemLike {
    @Inject(at={@At(value="HEAD")}, method={"getVelocityMultiplier()F"}, cancellable=true)
    private void onGetVelocityMultiplier(CallbackInfoReturnable<Float> cir) {
        if (NoSlow.INSTANCE.soulSand() && cir.getReturnValueF() < 1.0f) {
            cir.setReturnValue((Object)Float.valueOf(1.0f));
        }
    }

    @ModifyReturnValue(method={"shouldDrawSide"}, at={@At(value="RETURN")})
    private static boolean onShouldDrawSide(boolean original, BlockState state, BlockGetter world, BlockPos pos, Direction side, BlockPos blockPos) {
        Xray xray = Xray.INSTANCE;
        if (xray.isOn()) {
            return xray.modifyDrawSide(state, world, pos, side, original);
        }
        return original;
    }
}

