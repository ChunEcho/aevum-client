/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.MobEffect
 *  net.minecraft.MobEffectUtil
 *  net.minecraft.MobEffects
 *  net.minecraft.Entity
 *  net.minecraft.Entity$RemovalReason
 *  net.minecraft.EntityType
 *  net.minecraft.LivingEntity
 *  net.minecraft.Attribute
 *  net.minecraft.AttributeModifier
 *  net.minecraft.AttributeInstance
 *  net.minecraft.Level
 *  net.minecraft.Minecraft
 *  net.minecraft.AttributeMap
 *  net.minecraft.Attributes
 *  net.minecraft.Holder
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.aevum.asm.mixins;

import dev.aevum.Aevum;
import dev.aevum.api.events.impl.LerpToEvent;
import dev.aevum.api.events.impl.SprintEvent;
import dev.aevum.mod.modules.impl.misc.Swing;
import dev.aevum.mod.modules.impl.movement.ElytraFly;
import dev.aevum.mod.modules.impl.movement.NoSlow;
import dev.aevum.mod.modules.impl.movement.Velocity;
import dev.aevum.mod.modules.impl.player.AntiEffects;
import dev.aevum.mod.modules.impl.render.ViewModel;
import net.minecraft.MobEffect;
import net.minecraft.MobEffectUtil;
import net.minecraft.MobEffects;
import net.minecraft.Entity;
import net.minecraft.EntityType;
import net.minecraft.LivingEntity;
import net.minecraft.Attribute;
import net.minecraft.AttributeModifier;
import net.minecraft.AttributeInstance;
import net.minecraft.Level;
import net.minecraft.Minecraft;
import net.minecraft.AttributeMap;
import net.minecraft.Attributes;
import net.minecraft.Holder;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntity.class})
public abstract class MixinLivingEntity
extends Entity {
    @Final
    @Shadow
    private static AttributeModifier SPEED_MODIFIER_SPRINTING;
    @Unique
    private boolean previousElytra = false;
    @Unique
    private long lastLerp = 0L;

    public MixinLivingEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow
    @Nullable
    public AttributeInstance method_5996(Holder<Attribute> attribute) {
        return this.getAttributes().method_45329(attribute);
    }

    @Shadow
    public AttributeMap getAttributes() {
        return null;
    }

    @Shadow
    public abstract void method_5650(Entity.Entity$RemovalReason var1);

    @Inject(method={"getHandSwingDuration"}, at={@At(value="HEAD")}, cancellable=true)
    private void getArmSwingAnimationEnd(CallbackInfoReturnable<Integer> info) {
        Swing swingMod = Swing.INSTANCE;
        if (swingMod != null && swingMod.isOn()) {
            LivingEntity livingEntity = (LivingEntity)this;
            double speedVal = swingMod.speed.getValue();
            if (speedVal <= 0.01) {
                return;
            }
            int base = (int)((swingMod.type.getValue() == Swing.AnimMode.ONE_EIGHT ? 7.0 : 6.0) / speedVal);
            if (MobEffectUtil.method_5576((LivingEntity)livingEntity)) {
                info.setReturnValue((Object)(base - (1 + MobEffectUtil.getDigSpeedAmplification((LivingEntity)livingEntity))));
            } else if (livingEntity.method_6059(MobEffects.DIG_SLOWDOWN)) {
                info.setReturnValue((Object)(base + (1 + livingEntity.method_6112(MobEffects.DIG_SLOWDOWN).method_5578()) * 2));
            } else {
                info.setReturnValue((Object)base);
            }
            return;
        }
        if (ViewModel.INSTANCE.isOn() && ViewModel.INSTANCE.slowAnimation.getValue()) {
            info.setReturnValue((Object)ViewModel.INSTANCE.slowAnimationVal.getValueInt());
        }
    }

    @Inject(method={"isFallFlying"}, at={@At(value="TAIL")}, cancellable=true)
    public void recastOnLand(CallbackInfoReturnable<Boolean> cir) {
        boolean elytra = (Boolean)cir.getReturnValue();
        if (this.previousElytra && !elytra && ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.mode.is(ElytraFly.Mode.Bounce)) {
            cir.setReturnValue((Object)ElytraFly.recastElytra(Minecraft.getInstance().player));
        }
        this.previousElytra = elytra;
    }

    @Redirect(method={"travel"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z"), require=0)
    private boolean travelEffectHook(LivingEntity instance, Holder<MobEffect> effect) {
        if (AntiEffects.INSTANCE.isOn()) {
            if (effect == MobEffects.SLOW_FALLING && AntiEffects.INSTANCE.slowFalling.getValue()) {
                return false;
            }
            if (effect == MobEffects.LEVITATION && AntiEffects.INSTANCE.levitation.getValue()) {
                return false;
            }
        }
        return instance.method_6059(effect);
    }

    @Redirect(method={"applyMovementInput"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;isClimbing()Z"), require=0)
    public boolean climbingHook(LivingEntity instance) {
        if (Velocity.INSTANCE.isOn() && Velocity.INSTANCE.noClimb.getValue() && LivingEntity.class.cast((Object)this) == Minecraft.getInstance().player) {
            return false;
        }
        return instance.onClimbable();
    }

    @Redirect(method={"applyClimbingSpeed"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;isClimbing()Z"), require=0)
    public boolean climbingHook2(LivingEntity instance) {
        if (NoSlow.INSTANCE.climb() && LivingEntity.class.cast((Object)this) == Minecraft.getInstance().player) {
            return false;
        }
        return instance.onClimbable();
    }

    @Inject(method={"updateTrackedPositionAndAngles"}, at={@At(value="HEAD")})
    private void lerpToHook(double x, double y, double z, float yRot, float xRot, int steps, CallbackInfo ci) {
        Aevum.EVENT_BUS.post(LerpToEvent.get((LivingEntity)LivingEntity.class.cast((Object)this), x, y, z, yRot, xRot, this.lastLerp));
        this.lastLerp = System.currentTimeMillis();
    }

    @Inject(method={"setSprinting"}, at={@At(value="HEAD")}, cancellable=true)
    public void setSprintingHook(boolean sprinting, CallbackInfo ci) {
        if (LivingEntity.class.cast((Object)this) == Minecraft.getInstance().player) {
            SprintEvent event = SprintEvent.get();
            Aevum.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                ci.cancel();
                sprinting = event.isSprint();
                super.method_5728(sprinting);
                AttributeInstance entityAttributeInstance = this.method_5996((Holder<Attribute>)Attributes.MOVEMENT_SPEED);
                entityAttributeInstance.method_6200(SPEED_MODIFIER_SPRINTING.comp_2447());
                if (sprinting) {
                    entityAttributeInstance.method_26835(SPEED_MODIFIER_SPRINTING);
                }
            }
        }
    }
}

