/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.EndCrystal
 *  net.minecraft.PoseStack
 *  net.minecraft.MultiBufferSource
 *  net.minecraft.EntityRendererProvider$Context
 *  net.minecraft.EndCrystalRenderer
 *  net.minecraft.EntityRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.mod.modules.impl.render.Chams;
import net.minecraft.EndCrystal;
import net.minecraft.PoseStack;
import net.minecraft.MultiBufferSource;
import net.minecraft.EntityRendererProvider;
import net.minecraft.EndCrystalRenderer;
import net.minecraft.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={EndCrystalRenderer.class})
public abstract class MixinEndCrystalEntityRenderer
extends EntityRenderer<EndCrystal> {
    protected MixinEndCrystalEntityRenderer(EntityRendererProvider.EntityRendererProvider$Context ctx) {
        super(ctx);
    }

    @Inject(method={"render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at={@At(value="HEAD")}, cancellable=true)
    public void onRenderHead(EndCrystal entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
        Chams module = Chams.INSTANCE;
        if (module != null && module.isOn() && module.crystal.getValue() && module.cancelVanilla.getValue()) {
            ci.cancel();
        }
    }

    @Redirect(method={"render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V"))
    public void redirectScale(PoseStack matrices, float x, float y, float z) {
        Chams module = Chams.INSTANCE;
        if (module != null && module.isOn() && module.crystal.getValue()) {
            float scale = module.crystalScale.getValueFloat();
            matrices.method_22905(x * scale, y * scale, z * scale);
        } else {
            matrices.method_22905(x, y, z);
        }
    }
}

