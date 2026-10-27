/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.LivingEntity
 *  net.minecraft.Player
 *  net.minecraft.Minecraft
 *  net.minecraft.PoseStack
 *  net.minecraft.MultiBufferSource
 *  net.minecraft.EntityModel
 *  net.minecraft.LivingEntityRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package dev.aevum.asm.mixins;

import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.core.impl.RotationManager;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import dev.aevum.mod.modules.impl.render.NoRender;
import java.awt.Color;
import net.minecraft.LivingEntity;
import net.minecraft.Player;
import net.minecraft.Minecraft;
import net.minecraft.PoseStack;
import net.minecraft.MultiBufferSource;
import net.minecraft.EntityModel;
import net.minecraft.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={LivingEntityRenderer.class})
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> {
    @Unique
    private LivingEntity lastEntity;
    @Unique
    private float originalYaw;
    @Unique
    private float originalHeadYaw;
    @Unique
    private float originalBodyYaw;
    @Unique
    private float originalPitch;
    @Unique
    private float originalPrevYaw;
    @Unique
    private float originalPrevHeadYaw;
    @Unique
    private float originalPrevBodyYaw;

    @Inject(method={"render*"}, at={@At(value="HEAD")})
    public void onRenderPre(T livingEntity, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null && livingEntity == Minecraft.getInstance().player && ClientSetting.INSTANCE.rotations.getValue()) {
            this.originalYaw = livingEntity.getYRot();
            this.originalHeadYaw = ((LivingEntity)livingEntity).yHeadRot;
            this.originalBodyYaw = ((LivingEntity)livingEntity).yBodyRot;
            this.originalPitch = livingEntity.getXRot();
            this.originalPrevYaw = ((LivingEntity)livingEntity).yRotO;
            this.originalPrevHeadYaw = ((LivingEntity)livingEntity).yHeadRotO;
            this.originalPrevBodyYaw = ((LivingEntity)livingEntity).yBodyRotO;
            livingEntity.method_36456(RotationManager.getRenderYawOffset());
            ((LivingEntity)livingEntity).yHeadRot = RotationManager.getRotationYawHead();
            ((LivingEntity)livingEntity).yBodyRot = RotationManager.getRenderYawOffset();
            livingEntity.method_36457(RotationManager.getRenderPitch());
            ((LivingEntity)livingEntity).yRotO = RotationManager.getPrevRenderYawOffset();
            ((LivingEntity)livingEntity).yHeadRotO = RotationManager.getPrevRotationYawHead();
            ((LivingEntity)livingEntity).yBodyRotO = RotationManager.getPrevRenderYawOffset();
            ((LivingEntity)livingEntity).xRotO = RotationManager.getPrevRenderPitch();
        }
        this.lastEntity = livingEntity;
    }

    @Inject(method={"render*"}, at={@At(value="TAIL")})
    public void onRenderPost(T livingEntity, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null && livingEntity == Minecraft.getInstance().player && ClientSetting.INSTANCE.rotations.getValue()) {
            livingEntity.method_36456(this.originalYaw);
            ((LivingEntity)livingEntity).yHeadRot = this.originalHeadYaw;
            ((LivingEntity)livingEntity).yBodyRot = this.originalBodyYaw;
            livingEntity.method_36457(this.originalPitch);
            ((LivingEntity)livingEntity).yRotO = this.originalPrevYaw;
            ((LivingEntity)livingEntity).yHeadRotO = this.originalPrevHeadYaw;
            ((LivingEntity)livingEntity).yBodyRotO = this.originalPrevBodyYaw;
            ((LivingEntity)livingEntity).xRotO = this.originalPitch;
        }
    }

    @ModifyArgs(method={"render*"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"))
    private void renderHook(Args args) {
        Player pl;
        LivingEntity livingEntity;
        float alpha = -1.0f;
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.antiPlayerCollision.getValue() && this.lastEntity != Wrapper.mc.player && (livingEntity = this.lastEntity) instanceof Player && !(pl = (Player)livingEntity).isInvisible()) {
            alpha = MathUtil.clamp((float)(Wrapper.mc.player.method_5707(this.lastEntity.position()) / 3.0) + 0.2f, 0.0f, 1.0f);
        }
        if (alpha != -1.0f) {
            args.set(4, (Object)this.applyOpacity(0x26FFFFFF, alpha));
        }
    }

    @Unique
    int applyOpacity(int color_int, float opacity) {
        opacity = Math.min(1.0f, Math.max(0.0f, opacity));
        Color color = new Color(color_int);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)((float)color.getAlpha() * opacity)).getRGB();
    }
}

