/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Item$TooltipContext
 *  net.minecraft.ItemStack
 *  net.minecraft.TooltipFlag
 *  net.minecraft.ShulkerBoxBlock
 *  net.minecraft.Component
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.mod.modules.impl.misc.ShulkerViewer;
import java.util.List;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.TooltipFlag;
import net.minecraft.ShulkerBoxBlock;
import net.minecraft.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ShulkerBoxBlock.class})
public class MixinShulkerBoxBlock {
    @Inject(method={"appendTooltip"}, at={@At(value="HEAD")}, cancellable=true)
    private void onAppendTooltip(ItemStack stack, Item.Item$TooltipContext context, List<Component> tooltip, TooltipFlag options, CallbackInfo ci) {
        if (ShulkerViewer.INSTANCE.isOn() && ShulkerViewer.INSTANCE.toolTips.getValue()) {
            ci.cancel();
        }
    }
}

