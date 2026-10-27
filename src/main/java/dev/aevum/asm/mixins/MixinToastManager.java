/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.GuiGraphics
 *  net.minecraft.ToastComponent
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.mod.modules.impl.render.NoRender;
import net.minecraft.GuiGraphics;
import net.minecraft.ToastComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ToastComponent.class})
public class MixinToastManager {
    @Inject(method={"draw"}, at={@At(value="HEAD")}, cancellable=true)
    private void hookDraw(GuiGraphics context, CallbackInfo ci) {
        if (NoRender.INSTANCE.guiToast.getValue()) {
            ci.cancel();
        }
    }
}

