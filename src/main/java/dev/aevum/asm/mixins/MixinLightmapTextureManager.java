/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.NativeImage
 *  net.minecraft.DynamicTexture
 *  net.minecraft.MobEffect
 *  net.minecraft.MobEffectInstance
 *  net.minecraft.MobEffects
 *  net.minecraft.LivingEntity
 *  net.minecraft.DimensionType
 *  net.minecraft.Minecraft
 *  net.minecraft.Mth
 *  net.minecraft.ClientLevel
 *  net.minecraft.Holder
 *  net.minecraft.LocalPlayer
 *  net.minecraft.GameRenderer
 *  net.minecraft.LightTexture
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.aevum.asm.mixins;

import dev.aevum.mod.modules.impl.render.Ambience;
import dev.aevum.mod.modules.impl.render.NoRender;
import java.awt.Color;
import net.minecraft.NativeImage;
import net.minecraft.DynamicTexture;
import net.minecraft.MobEffect;
import net.minecraft.MobEffectInstance;
import net.minecraft.MobEffects;
import net.minecraft.LivingEntity;
import net.minecraft.DimensionType;
import net.minecraft.Minecraft;
import net.minecraft.Mth;
import net.minecraft.ClientLevel;
import net.minecraft.Holder;
import net.minecraft.LocalPlayer;
import net.minecraft.GameRenderer;
import net.minecraft.LightTexture;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LightTexture.class})
public class MixinLightmapTextureManager {
    @Final
    @Shadow
    private DynamicTexture lightTexture;
    @Final
    @Shadow
    private NativeImage lightPixels;
    @Shadow
    private boolean updateLightTexture;
    @Shadow
    private float blockLightRedFlicker;
    @Final
    @Shadow
    private GameRenderer renderer;
    @Final
    @Shadow
    private Minecraft minecraft;

    @Shadow
    private static void method_46557(Vector3f vec) {
        vec.set(Mth.method_15363((float)vec.x, (float)0.0f, (float)1.0f), Mth.method_15363((float)vec.y, (float)0.0f, (float)1.0f), Mth.method_15363((float)vec.z, (float)0.0f, (float)1.0f));
    }

    @Shadow
    public static float method_23284(DimensionType type, int lightLevel) {
        float f = (float)lightLevel / 15.0f;
        float g = f / (4.0f - 3.0f * f);
        return Mth.method_16439((float)type.comp_656(), (float)g, (float)1.0f);
    }

    @Redirect(method={"update"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z", ordinal=0, remap=false), require=0)
    private boolean nightVisionHook(LocalPlayer instance, Holder<MobEffect> registryEntry) {
        if (Ambience.INSTANCE.isOn() && Ambience.INSTANCE.fullBright.getValue()) {
            return true;
        }
        return instance.method_6059(registryEntry);
    }

    @Inject(method={"update"}, at={@At(value="HEAD")}, cancellable=true)
    public void updateHook(float delta, CallbackInfo ci) {
        if (Ambience.INSTANCE.isOn() && Ambience.INSTANCE.worldColor.booleanValue) {
            ci.cancel();
            if (this.updateLightTexture) {
                this.updateLightTexture = false;
                this.minecraft.getProfiler().method_15396("lightTex");
                ClientLevel clientWorld = this.minecraft.level;
                if (clientWorld != null) {
                    float f = clientWorld.method_23783(1.0f);
                    float g = clientWorld.getSkyFlashTime() > 0 ? 1.0f : f * 0.95f + 0.05f;
                    float h = ((Double)this.minecraft.options.darknessEffectScale().method_41753()).floatValue();
                    float i = this.method_42597(delta) * h;
                    float j = this.method_42596((LivingEntity)this.minecraft.player, i, delta) * h;
                    Vector3f vector3f = new Vector3f(f, f, 1.0f).lerp((Vector3fc)new Vector3f(1.0f, 1.0f, 1.0f), 0.35f);
                    float m = this.blockLightRedFlicker + 1.5f;
                    Vector3f vector3f2 = new Vector3f();
                    for (int n = 0; n < 16; ++n) {
                        for (int o = 0; o < 16; ++o) {
                            float p = MixinLightmapTextureManager.method_23284(clientWorld.method_8597(), n) * g;
                            float q = MixinLightmapTextureManager.method_23284(clientWorld.method_8597(), o) * m;
                            float s = q * ((q * 0.6f + 0.4f) * 0.6f + 0.4f);
                            float t = q * (q * q * 0.6f + 0.4f);
                            vector3f2.set(q, s, t);
                            boolean bl = clientWorld.method_28103().forceBrightLightmap();
                            if (bl) {
                                vector3f2.lerp((Vector3fc)new Vector3f(0.99f, 1.12f, 1.0f), 0.25f);
                                MixinLightmapTextureManager.method_46557(vector3f2);
                            } else {
                                Vector3f vector3f3 = new Vector3f((Vector3fc)vector3f).mul(p);
                                vector3f2.add((Vector3fc)vector3f3);
                                vector3f2.lerp((Vector3fc)new Vector3f(0.75f, 0.75f, 0.75f), 0.04f);
                                if (this.renderer.method_3195(delta) > 0.0f) {
                                    float u = this.renderer.method_3195(delta);
                                    Vector3f vector3f4 = new Vector3f((Vector3fc)vector3f2).mul(0.7f, 0.6f, 0.6f);
                                    vector3f2.lerp((Vector3fc)vector3f4, u);
                                }
                            }
                            float v = Math.max(vector3f2.x(), Math.max(vector3f2.y(), vector3f2.z()));
                            if (v < 1.0f) {
                                this.lightPixels.method_4305(o, n, new Color(Ambience.INSTANCE.worldColor.getValue().getBlue(), Ambience.INSTANCE.worldColor.getValue().getGreen(), Ambience.INSTANCE.worldColor.getValue().getRed(), Ambience.INSTANCE.worldColor.getValue().getAlpha()).getRGB());
                                continue;
                            }
                            if (!bl) {
                                if (j > 0.0f) {
                                    vector3f2.add(-j, -j, -j);
                                }
                                MixinLightmapTextureManager.method_46557(vector3f2);
                            }
                            v = ((Double)this.minecraft.options.gamma().method_41753()).floatValue();
                            Vector3f vector3f5 = new Vector3f(this.method_23795(vector3f2.x), this.method_23795(vector3f2.y), this.method_23795(vector3f2.z));
                            vector3f2.lerp((Vector3fc)vector3f5, Math.max(0.0f, v - i));
                            vector3f2.lerp((Vector3fc)new Vector3f(0.75f, 0.75f, 0.75f), 0.04f);
                            MixinLightmapTextureManager.method_46557(vector3f2);
                            vector3f2.mul(255.0f);
                            int x = (int)vector3f2.x();
                            int y = (int)vector3f2.y();
                            int z = (int)vector3f2.z();
                            this.lightPixels.method_4305(o, n, 0xFF000000 | z << 16 | y << 8 | x);
                        }
                    }
                    this.lightTexture.upload();
                    this.minecraft.getProfiler().method_15407();
                }
            }
        }
    }

    @Inject(method={"getDarknessFactor(F)F"}, at={@At(value="HEAD")}, cancellable=true)
    private void getDarknessFactor(float tickDelta, CallbackInfoReturnable<Float> info) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.darkness.getValue()) {
            info.setReturnValue((Object)Float.valueOf(0.0f));
        }
    }

    @Shadow
    private float method_23795(float x) {
        float f = 1.0f - x;
        return 1.0f - f * f * f * f;
    }

    @Shadow
    private float method_42597(float delta) {
        MobEffectInstance statusEffectInstance = this.minecraft.player.method_6112(MobEffects.DARKNESS);
        return statusEffectInstance != null ? statusEffectInstance.method_55653((LivingEntity)this.minecraft.player, delta) : 0.0f;
    }

    @Shadow
    private float method_42596(LivingEntity entity, float factor, float delta) {
        float f = 0.45f * factor;
        return Math.max(0.0f, Mth.method_15362((float)(((float)entity.tickCount - delta) * (float)Math.PI * 0.025f)) * f);
    }
}

