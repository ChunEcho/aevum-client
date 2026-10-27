/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ParticleEngine
 *  net.minecraft.Particle
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.Aevum;
import dev.aevum.api.events.impl.ParticleEvent;
import net.minecraft.ParticleEngine;
import net.minecraft.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ParticleEngine.class})
public class MixinParticleManager {
    @Inject(at={@At(value="HEAD")}, method={"addParticle(Lnet/minecraft/client/particle/Particle;)V"}, cancellable=true)
    public void onAddParticle(Particle particle, CallbackInfo ci) {
        ParticleEvent event = ParticleEvent.get(particle);
        Aevum.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}

