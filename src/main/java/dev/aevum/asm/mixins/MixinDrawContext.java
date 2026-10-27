/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockItem
 *  net.minecraft.ItemStack
 *  net.minecraft.ShulkerBoxBlock
 *  net.minecraft.Font
 *  net.minecraft.GuiGraphics
 *  net.minecraft.PoseStack
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.mod.modules.impl.misc.ShulkerViewer;
import net.minecraft.BlockItem;
import net.minecraft.ItemStack;
import net.minecraft.ShulkerBoxBlock;
import net.minecraft.Font;
import net.minecraft.GuiGraphics;
import net.minecraft.PoseStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GuiGraphics.class})
public class MixinDrawContext {
    @Unique
    private static final ItemStack[] ITEMS = new ItemStack[27];
    @Final
    @Shadow
    private PoseStack pose;

    @Inject(method={"drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V"}, at={@At(value="TAIL")})
    public void onDrawItem(Font textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        if (ShulkerViewer.INSTANCE.isOn() && ShulkerViewer.INSTANCE.icon.getValue() && stack.method_7909() instanceof BlockItem && ((BlockItem)stack.method_7909()).getBlock() instanceof ShulkerBoxBlock) {
            ShulkerViewer.getItemsInContainerItem(stack, ITEMS);
            for (ItemStack itemStack : ITEMS) {
                if (itemStack.method_7960()) continue;
                this.pose.method_22903();
                this.pose.method_22905(0.5f, 0.5f, 1.0f);
                this.method_51427(itemStack, x * 2 + 20, y * 2 + 20);
                this.pose.method_22909();
                return;
            }
        }
    }

    @Shadow
    public void method_51427(ItemStack item, int x, int y) {
    }
}

