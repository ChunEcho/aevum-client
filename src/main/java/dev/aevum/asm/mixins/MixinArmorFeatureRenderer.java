/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.LivingEntity
 *  net.minecraft.ArmorMaterial
 *  net.minecraft.ResourceLocation
 *  net.minecraft.PoseStack
 *  net.minecraft.MultiBufferSource
 *  net.minecraft.HumanoidModel
 *  net.minecraft.Holder
 *  net.minecraft.ArmorTrim
 *  net.minecraft.HumanoidArmorLayer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.mod.modules.impl.render.NoRender;
import net.minecraft.LivingEntity;
import net.minecraft.ArmorMaterial;
import net.minecraft.ResourceLocation;
import net.minecraft.PoseStack;
import net.minecraft.MultiBufferSource;
import net.minecraft.HumanoidModel;
import net.minecraft.Holder;
import net.minecraft.ArmorTrim;
import net.minecraft.HumanoidArmorLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={HumanoidArmorLayer.class})
public class MixinArmorFeatureRenderer<T extends LivingEntity, A extends HumanoidModel<T>> {
    @Inject(method={"renderArmorParts"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderArmorParts(PoseStack matrices, MultiBufferSource vertexConsumers, int light, A model, int i, ResourceLocation identifier, CallbackInfo ci) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.armorParts.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method={"renderTrim"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderArmorTrim(Holder<ArmorMaterial> armorMaterial, PoseStack matrices, MultiBufferSource vertexConsumers, int light, ArmorTrim trim, A model, boolean leggings, CallbackInfo ci) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.armorTrim.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method={"renderGlint"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderArmorGlint(PoseStack matrices, MultiBufferSource vertexConsumers, int light, A model, CallbackInfo ci) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.armorGlint.getValue()) {
            ci.cancel();
        }
    }
}

