/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BakedModel
 *  net.minecraft.ItemStack
 *  net.minecraft.PoseStack
 *  net.minecraft.VertexConsumer
 *  net.minecraft.MultiBufferSource
 *  net.minecraft.ItemDisplayContext
 *  net.minecraft.ItemRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.api.utils.render.SimpleItemModel;
import dev.aevum.mod.modules.impl.render.NoRender;
import net.minecraft.BakedModel;
import net.minecraft.ItemStack;
import net.minecraft.PoseStack;
import net.minecraft.VertexConsumer;
import net.minecraft.MultiBufferSource;
import net.minecraft.ItemDisplayContext;
import net.minecraft.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemRenderer.class})
public class MixinItemRenderer {
    @Unique
    private final SimpleItemModel flattenedModel = new SimpleItemModel();
    @Unique
    private ItemDisplayContext renderMode;

    @Inject(method={"renderItem*"}, at={@At(value="HEAD")})
    private void getRenderType(ItemStack itemStack, ItemDisplayContext transformationMode, boolean leftHand, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        this.renderMode = transformationMode;
    }

    @ModifyVariable(method={"renderBakedItemModel"}, at=@At(value="HEAD"), index=1, argsOnly=true, require=0)
    private BakedModel replaceItemModelClass(BakedModel model, BakedModel arg, ItemStack stack, int light, int overlay, PoseStack matrices, VertexConsumer vertices) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.fastItem.getValue() && !NoRender.INSTANCE.renderSidesOfItems.getValue() && !stack.method_7960() && !model.method_4712() && this.renderMode == ItemDisplayContext.GROUND) {
            this.flattenedModel.setItem(model);
            return this.flattenedModel;
        }
        return model;
    }
}

