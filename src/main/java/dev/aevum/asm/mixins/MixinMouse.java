/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.KeyMapping
 *  net.minecraft.Minecraft
 *  net.minecraft.MouseHandler
 *  net.minecraft.InputConstants
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.Aevum;
import dev.aevum.api.interfaces.IMouseHook;
import net.minecraft.KeyMapping;
import net.minecraft.Minecraft;
import net.minecraft.MouseHandler;
import net.minecraft.InputConstants;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MouseHandler.class})
public class MixinMouse
implements IMouseHook {
    @Shadow
    private boolean mouseGrabbed;
    @Final
    @Shadow
    private Minecraft minecraft;
    @Shadow
    private double xpos;
    @Shadow
    private double ypos;

    @Inject(method={"onMouseButton"}, at={@At(value="HEAD")})
    private void onMouse(long window, int button, int action, int mods, CallbackInfo ci) {
        int key = -(button + 2);
        if (action == 1) {
            Aevum.MODULE.onKeyPressed(key);
        }
        if (action == 0) {
            Aevum.MODULE.onKeyReleased(key);
        }
    }

    @Override
    public void aevumClient$lock() {
        if (this.minecraft.isWindowActive() && !this.mouseGrabbed) {
            if (!Minecraft.ON_OSX) {
                KeyMapping.method_1424();
            }
            this.mouseGrabbed = true;
            this.xpos = (double)this.minecraft.getWindow().getScreenWidth() / 2.0;
            this.ypos = (double)this.minecraft.getWindow().getScreenHeight() / 2.0;
            InputConstants.method_15984((long)this.minecraft.getWindow().getWindow(), (int)212995, (double)this.xpos, (double)this.ypos);
        }
    }
}

