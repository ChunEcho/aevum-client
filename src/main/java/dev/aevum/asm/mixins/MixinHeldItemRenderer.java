/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.ItemStack
 *  net.minecraft.Minecraft
 *  net.minecraft.Mth
 *  net.minecraft.PoseStack
 *  net.minecraft.MultiBufferSource
 *  net.minecraft.AbstractClientPlayer
 *  net.minecraft.ItemInHandRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.Aevum;
import dev.aevum.api.events.impl.HeldItemRendererEvent;
import dev.aevum.mod.modules.impl.misc.Swing;
import net.minecraft.InteractionHand;
import net.minecraft.ItemStack;
import net.minecraft.Minecraft;
import net.minecraft.Mth;
import net.minecraft.PoseStack;
import net.minecraft.MultiBufferSource;
import net.minecraft.AbstractClientPlayer;
import net.minecraft.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemInHandRenderer.class})
public abstract class MixinHeldItemRenderer {
    @Shadow
    private ItemStack mainHandItem;
    @Shadow
    private float mainHandHeight;

    @Redirect(method={"updateHeldItems"}, at=@At(value="INVOKE", target="Lnet/minecraft/util/math/MathHelper;clamp(FFF)F", ordinal=2))
    private float aevum$updateHeldItems(float value, float min, float max) {
        Swing swing = Swing.INSTANCE;
        if (swing == null || !swing.isOn() || swing.type.getValue() == Swing.AnimMode.Vanilla) {
            return Mth.method_15363((float)value, (float)min, (float)max);
        }
        boolean oneEight = swing.type.getValue() == Swing.AnimMode.ONE_EIGHT;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return Mth.method_15363((float)value, (float)min, (float)max);
        }
        float cooldown = client.player.getAttackStrengthScale(1.0f);
        if (oneEight) {
            cooldown = 1.0f;
        }
        float base = ItemStack.method_31577((ItemStack)this.mainHandItem, (ItemStack)client.player.getMainHandItem()) ? cooldown * cooldown * cooldown : 0.0f;
        return Mth.method_15363((float)(base - this.mainHandHeight), (float)(oneEight ? -0.6f : -0.4f), (float)0.4f);
    }

    @Inject(method={"renderFirstPersonItem"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")})
    private void onRenderItem(AbstractClientPlayer player, float tickDelta, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
        HeldItemRendererEvent event = HeldItemRendererEvent.get(hand, item, equipProgress, matrices);
        Aevum.EVENT_BUS.post(event);
    }
}

