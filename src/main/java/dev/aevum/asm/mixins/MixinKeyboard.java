/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.KeyboardHandler
 *  net.minecraft.GameNarrator
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.Aevum;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.core.impl.CommandManager;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import net.minecraft.ChatFormatting;
import net.minecraft.KeyboardHandler;
import net.minecraft.GameNarrator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={KeyboardHandler.class})
public class MixinKeyboard
implements Wrapper {
    @Inject(method={"onKey"}, at={@At(value="HEAD")})
    private void onKey(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        block4: {
            try {
                if (action == 1) {
                    Aevum.MODULE.onKeyPressed(key);
                }
                if (action == 0) {
                    Aevum.MODULE.onKeyReleased(key);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                if (!ClientSetting.INSTANCE.debug.getValue()) break block4;
                CommandManager.sendMessage(String.valueOf(ChatFormatting.DARK_RED) + "[ERROR] onKey " + e.getMessage());
            }
        }
    }

    @Redirect(method={"onKey"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/util/NarratorManager;isActive()Z"), require=0)
    public boolean hook(GameNarrator instance) {
        return false;
    }
}

