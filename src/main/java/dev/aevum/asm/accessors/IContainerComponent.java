/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemStack
 *  net.minecraft.NonNullList
 *  net.minecraft.ItemContainerContents
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.aevum.asm.accessors;

import net.minecraft.ItemStack;
import net.minecraft.NonNullList;
import net.minecraft.ItemContainerContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ItemContainerContents.class})
public interface IContainerComponent {
    @Accessor
    public NonNullList<ItemStack> getStacks();
}

