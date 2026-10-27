/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Player
 *  net.minecraft.Component
 *  net.minecraft.PoseStack
 *  net.minecraft.MultiBufferSource
 *  net.minecraft.EntityRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.mod.modules.impl.render.NameTags;
import net.minecraft.Entity;
import net.minecraft.Player;
import net.minecraft.Component;
import net.minecraft.PoseStack;
import net.minecraft.MultiBufferSource;
import net.minecraft.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={EntityRenderer.class})
public abstract class MixinEntityRenderer<T extends Entity> {
    @Inject(method={"renderLabelIfPresent"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderLabel(T entity, Component text, PoseStack matrices, MultiBufferSource vertexConsumers, int light, float tickDelta, CallbackInfo ci) {
        if (entity instanceof Player && NameTags.INSTANCE.isOn()) {
            ci.cancel();
        }
    }
}

