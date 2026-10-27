/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.MobEffect
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.Camera
 *  net.minecraft.ClientLevel
 *  net.minecraft.Holder
 *  net.minecraft.FogRenderer
 *  net.minecraft.FogRenderer$FogMode
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.aevum.asm.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.mod.modules.impl.render.Ambience;
import dev.aevum.mod.modules.impl.render.NoRender;
import dev.aevum.mod.modules.impl.render.Xray;
import java.awt.Color;
import net.minecraft.MobEffect;
import net.minecraft.Entity;
import net.minecraft.LivingEntity;
import net.minecraft.Camera;
import net.minecraft.ClientLevel;
import net.minecraft.Holder;
import net.minecraft.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={FogRenderer.class})
public class MixinBackgroundRenderer {
    @Redirect(method={"render"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z", ordinal=0, remap=false), require=0)
    private static boolean nightVisionHook(LivingEntity instance, Holder<MobEffect> effect) {
        if (Ambience.INSTANCE.isOn() && Ambience.INSTANCE.fullBright.getValue()) {
            return true;
        }
        return instance.method_6059(effect);
    }

    @Inject(method={"applyFog"}, at={@At(value="TAIL")})
    private static void onApplyFog(Camera camera, FogRenderer.FogRenderer$FogMode fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo info) {
        if (Ambience.INSTANCE.isOn()) {
            if (Ambience.INSTANCE.fog.booleanValue) {
                RenderSystem.setShaderFogColor((float)((float)Ambience.INSTANCE.fog.getValue().getRed() / 255.0f), (float)((float)Ambience.INSTANCE.fog.getValue().getGreen() / 255.0f), (float)((float)Ambience.INSTANCE.fog.getValue().getBlue() / 255.0f), (float)((float)Ambience.INSTANCE.fog.getValue().getAlpha() / 255.0f));
            }
            if (Ambience.INSTANCE.fogDistance.getValue()) {
                RenderSystem.setShaderFogStart((float)Ambience.INSTANCE.fogStart.getValueFloat());
                RenderSystem.setShaderFogEnd((float)Ambience.INSTANCE.fogEnd.getValueFloat());
            }
        }
        if ((NoRender.INSTANCE.isOn() && NoRender.INSTANCE.fog.getValue() || Xray.INSTANCE.isOn()) && fogType == FogRenderer.FogRenderer$FogMode.FOG_TERRAIN) {
            RenderSystem.setShaderFogStart((float)(viewDistance * 4.0f));
            RenderSystem.setShaderFogEnd((float)(viewDistance * 4.25f));
        }
    }

    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true)
    private static void hookRender(Camera camera, float tickDelta, ClientLevel world, int viewDistance, float skyDarkness, CallbackInfo ci) {
        if (Ambience.INSTANCE.isOn() && Ambience.INSTANCE.dimensionColor.booleanValue) {
            Color color = Ambience.INSTANCE.dimensionColor.getValue();
            ci.cancel();
            RenderSystem.clearColor((float)((float)color.getRed() / 255.0f), (float)((float)color.getGreen() / 255.0f), (float)((float)color.getBlue() / 255.0f), (float)0.0f);
        }
    }

    @Inject(method={"getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;"}, at={@At(value="HEAD")}, cancellable=true)
    private static void onGetFogModifier(Entity entity, float tickDelta, CallbackInfoReturnable<Object> info) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.blindness.getValue()) {
            info.setReturnValue(null);
        }
    }
}

