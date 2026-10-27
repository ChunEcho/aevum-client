/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BakedModel
 *  net.minecraft.Entity
 *  net.minecraft.ItemEntity
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Mth
 *  net.minecraft.PoseStack
 *  net.minecraft.MultiBufferSource
 *  net.minecraft.OverlayTexture
 *  net.minecraft.EntityRendererProvider$Context
 *  net.minecraft.RandomSource
 *  net.minecraft.ItemDisplayContext
 *  net.minecraft.EntityRenderer
 *  net.minecraft.ItemEntityRenderer
 *  net.minecraft.ItemRenderer
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.mod.modules.impl.render.NoRender;
import net.minecraft.BakedModel;
import net.minecraft.Entity;
import net.minecraft.ItemEntity;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Mth;
import net.minecraft.PoseStack;
import net.minecraft.MultiBufferSource;
import net.minecraft.OverlayTexture;
import net.minecraft.EntityRendererProvider;
import net.minecraft.RandomSource;
import net.minecraft.ItemDisplayContext;
import net.minecraft.EntityRenderer;
import net.minecraft.ItemEntityRenderer;
import net.minecraft.ItemRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemEntityRenderer.class})
public abstract class MixinItemEntityRenderer
extends EntityRenderer<ItemEntity> {
    @Final
    @Shadow
    private ItemRenderer itemRenderer;
    @Final
    @Shadow
    private RandomSource random;

    protected MixinItemEntityRenderer(EntityRendererProvider.EntityRendererProvider$Context ctx) {
        super(ctx);
    }

    @Unique
    private static int getRenderedAmount(ItemStack stackSize) {
        int count = stackSize.getCount();
        if (count <= 1) {
            return 1;
        }
        if (count <= 16) {
            return 2;
        }
        if (count <= 32) {
            return 3;
        }
        return count <= 48 ? 4 : 5;
    }

    @Inject(method={"render*"}, at={@At(value="HEAD")}, cancellable=true)
    public void render(ItemEntity itemEntity, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, CallbackInfo ci) {
        float t;
        float s;
        if (!NoRender.INSTANCE.isOn() || !NoRender.INSTANCE.fastItem.getValue()) {
            return;
        }
        matrixStack.method_22903();
        ItemStack itemStack = itemEntity.getItem();
        long j = itemStack.method_7960() ? 187L : (long)(Item.method_7880((Item)itemStack.method_7909()) + itemStack.getDamageValue());
        this.random.method_43052(j);
        BakedModel bakedModel = this.itemRenderer.method_4019(itemStack, itemEntity.level(), null, itemEntity.method_5628());
        boolean hasDepth = bakedModel.method_4712();
        this.shadowRadius = NoRender.INSTANCE.castShadow.getValue() ? 0.15f : 0.0f;
        float l = Mth.method_15374((float)(((float)itemEntity.getAge() + g) / 10.0f + itemEntity.bobOffs)) * 0.1f + 0.1f;
        float m = bakedModel.method_4709().getTransform((ItemDisplayContext)ItemDisplayContext.GROUND).scale.y();
        matrixStack.method_46416(0.0f, l + 0.25f * m, 0.0f);
        matrixStack.method_22907(this.entityRenderDispatcher.method_24197());
        float o = bakedModel.method_4709().ground.scale.x();
        float p = bakedModel.method_4709().ground.scale.y();
        float q = bakedModel.method_4709().ground.scale.z();
        int renderedAmount = MixinItemEntityRenderer.getRenderedAmount(itemStack);
        if (!hasDepth) {
            float r = -0.0f * (float)(renderedAmount - 1) * 0.5f * o;
            s = -0.0f * (float)(renderedAmount - 1) * 0.5f * p;
            t = -0.09375f * (float)(renderedAmount - 1) * 0.5f * q;
            matrixStack.method_46416(r, s, t);
        }
        for (int u = 0; u < renderedAmount; ++u) {
            matrixStack.method_22903();
            if (u > 0) {
                if (hasDepth) {
                    s = (this.random.method_43057() * 2.0f - 1.0f) * 0.15f;
                    t = (this.random.method_43057() * 2.0f - 1.0f) * 0.15f;
                    float v = (this.random.method_43057() * 2.0f - 1.0f) * 0.15f;
                    matrixStack.method_46416(s, t, v);
                } else {
                    s = (this.random.method_43057() * 2.0f - 1.0f) * 0.15f * 0.5f;
                    t = (this.random.method_43057() * 2.0f - 1.0f) * 0.15f * 0.5f;
                    matrixStack.method_46416(s, t, 0.0f);
                }
            }
            this.itemRenderer.method_23179(itemStack, ItemDisplayContext.GROUND, false, matrixStack, vertexConsumerProvider, i, OverlayTexture.NO_OVERLAY, bakedModel);
            matrixStack.method_22909();
            if (hasDepth) continue;
            matrixStack.method_46416(0.0f * o, 0.0f * p, 0.0425f * q);
        }
        matrixStack.method_22909();
        super.method_3936((Entity)itemEntity, f, g, matrixStack, vertexConsumerProvider, i);
        ci.cancel();
    }
}

