/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.DeltaTracker$Timer
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.aevum.asm.mixins;

import dev.aevum.Aevum;
import dev.aevum.api.events.impl.TimerEvent;
import net.minecraft.DeltaTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={DeltaTracker.DeltaTracker$Timer.class})
public class MixinRenderTickCounter {
    @Shadow
    private float deltaTicks;
    @Shadow
    private float deltaTickResidual;
    @Shadow
    private long lastMs;
    @Final
    @Shadow
    private float msPerTick;

    @Inject(method={"beginRenderTick(J)I"}, at={@At(value="HEAD")}, cancellable=true)
    private void beginRenderTickHook(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        TimerEvent event = TimerEvent.getEvent();
        Aevum.EVENT_BUS.post(event);
        if (!event.isCancelled()) {
            float timer;
            float f = timer = event.isModified() ? event.get() : Aevum.TIMER.get();
            if (timer == 1.0f) {
                return;
            }
            this.deltaTicks = (float)(timeMillis - this.lastMs) / this.msPerTick * timer;
            this.lastMs = timeMillis;
            this.deltaTickResidual += this.deltaTicks;
            int i = (int)this.deltaTickResidual;
            this.deltaTickResidual -= (float)i;
            cir.setReturnValue((Object)i);
        }
    }
}

