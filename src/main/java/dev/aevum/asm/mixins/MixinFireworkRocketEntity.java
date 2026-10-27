/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.LivingEntity
 *  net.minecraft.FireworkRocketEntity
 *  net.minecraft.ParticleOptions
 *  net.minecraft.ParticleTypes
 *  net.minecraft.Vec3
 *  net.minecraft.SoundEvents
 *  net.minecraft.SoundSource
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.Aevum;
import dev.aevum.api.events.impl.FireworkShooterRotationEvent;
import dev.aevum.api.events.impl.RemoveFireworkEvent;
import dev.aevum.api.utils.Wrapper;
import net.minecraft.LivingEntity;
import net.minecraft.FireworkRocketEntity;
import net.minecraft.ParticleOptions;
import net.minecraft.ParticleTypes;
import net.minecraft.Vec3;
import net.minecraft.SoundEvents;
import net.minecraft.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={FireworkRocketEntity.class})
public class MixinFireworkRocketEntity
implements Wrapper {
    @Shadow
    private int life;

    @Inject(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/projectile/FireworkRocketEntity;updateRotation()V", shift=At.Shift.AFTER)}, cancellable=true)
    private void hookTickPre(CallbackInfo ci) {
        FireworkRocketEntity rocketEntity = (FireworkRocketEntity)FireworkRocketEntity.class.cast(this);
        RemoveFireworkEvent removeFireworkEvent = RemoveFireworkEvent.get(rocketEntity);
        Aevum.EVENT_BUS.post(removeFireworkEvent);
        if (removeFireworkEvent.isCancelled()) {
            ci.cancel();
            if (this.life == 0 && !rocketEntity.isSilent()) {
                MixinFireworkRocketEntity.mc.level.method_43128(null, rocketEntity.getX(), rocketEntity.getY(), rocketEntity.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 3.0f, 1.0f);
            }
            ++this.life;
            if (MixinFireworkRocketEntity.mc.level.isClientSide) {
                MixinFireworkRocketEntity.mc.level.method_8406((ParticleOptions)ParticleTypes.FIREWORK, rocketEntity.getX(), rocketEntity.getY(), rocketEntity.getZ(), MixinFireworkRocketEntity.mc.level.random.method_43059() * 0.05, -rocketEntity.getDeltaMovement().y * 0.5, MixinFireworkRocketEntity.mc.level.random.method_43059() * 0.05);
            }
        }
    }

    @Redirect(method={"tick"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"), require=0)
    public Vec3 hook(LivingEntity instance) {
        FireworkShooterRotationEvent event = FireworkShooterRotationEvent.get(instance, instance.getYRot(), instance.getXRot());
        Aevum.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            return event.getRotationVector();
        }
        return instance.getLookAngle();
    }
}

