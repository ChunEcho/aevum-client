/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemStack
 *  net.minecraft.ItemInHandRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.aevum.asm.accessors;

import net.minecraft.ItemStack;
import net.minecraft.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ItemInHandRenderer.class})
public interface IHeldItemRenderer {
    @Accessor(value="equipProgressMainHand")
    public float getEquippedProgressMainHand();

    @Accessor(value="equipProgressMainHand")
    public void setEquippedProgressMainHand(float var1);

    @Accessor(value="equipProgressOffHand")
    public float getEquippedProgressOffHand();

    @Accessor(value="equipProgressOffHand")
    public void setEquippedProgressOffHand(float var1);

    @Accessor(value="mainHand")
    public void setItemStackMainHand(ItemStack var1);

    @Accessor(value="offHand")
    public void setItemStackOffHand(ItemStack var1);
}

