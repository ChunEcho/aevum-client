/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.SpriteSet
 *  net.minecraft.ClientLevel
 *  net.minecraft.TotemParticle
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.Aevum;
import dev.aevum.api.events.impl.TotemParticleEvent;
import dev.aevum.asm.mixins.MixinParticle;
import java.awt.Color;
import net.minecraft.SpriteSet;
import net.minecraft.ClientLevel;
import net.minecraft.TotemParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={TotemParticle.class})
public abstract class MixinTotemParticle
extends MixinParticle {
    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void hookInit(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteProvider, CallbackInfo ci) {
        TotemParticleEvent event = TotemParticleEvent.get(velocityX, velocityY, velocityZ);
        Aevum.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            this.xd = event.velocityX;
            this.yd = event.velocityY;
            this.zd = event.velocityZ;
            Color color = event.color;
            if (color != null) {
                this.method_3084((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f);
                this.setAlpha((float)color.getAlpha() / 255.0f);
            }
        }
    }
}

