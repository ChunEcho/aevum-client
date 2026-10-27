/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.SoundInstance
 *  net.minecraft.SoundEngine
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.Aevum;
import dev.aevum.api.events.impl.PlaySoundEvent;
import net.minecraft.SoundInstance;
import net.minecraft.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={SoundEngine.class})
public class MixinSoundSystem {
    @Inject(method={"play(Lnet/minecraft/client/sound/SoundInstance;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void onPlay(SoundInstance soundInstance, CallbackInfo info) {
        PlaySoundEvent event = PlaySoundEvent.get(soundInstance);
        Aevum.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }
}

