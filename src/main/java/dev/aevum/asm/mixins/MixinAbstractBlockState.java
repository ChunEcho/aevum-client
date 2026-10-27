/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockBehaviour$BlockStateBase
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.aevum.asm.mixins;

import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.render.Ambience;
import net.minecraft.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={BlockBehaviour.BlockBehaviour$BlockStateBase.class})
public class MixinAbstractBlockState {
    @Inject(method={"getLuminance"}, at={@At(value="HEAD")}, cancellable=true)
    public void getLuminanceHook(CallbackInfoReturnable<Integer> cir) {
        if (Module.nullCheck()) {
            return;
        }
        if (Ambience.INSTANCE.customLuminance.getValue()) {
            cir.setReturnValue((Object)Ambience.INSTANCE.luminance.getValueInt());
        }
    }
}

