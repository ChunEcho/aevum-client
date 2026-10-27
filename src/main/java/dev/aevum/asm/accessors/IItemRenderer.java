/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BakedModel
 *  net.minecraft.ItemStack
 *  net.minecraft.PoseStack
 *  net.minecraft.VertexConsumer
 *  net.minecraft.BlockEntityWithoutLevelRenderer
 *  net.minecraft.ItemRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package dev.aevum.asm.accessors;

import net.minecraft.BakedModel;
import net.minecraft.ItemStack;
import net.minecraft.PoseStack;
import net.minecraft.VertexConsumer;
import net.minecraft.BlockEntityWithoutLevelRenderer;
import net.minecraft.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={ItemRenderer.class})
public interface IItemRenderer {
    @Accessor(value="builtinModelItemRenderer")
    public BlockEntityWithoutLevelRenderer hookGetBuiltinModelItemRenderer();

    @Invoker(value="renderBakedItemModel")
    public void hookRenderBakedItemModel(BakedModel var1, ItemStack var2, int var3, int var4, PoseStack var5, VertexConsumer var6);
}

