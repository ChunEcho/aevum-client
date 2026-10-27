/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.MultiPlayerGameMode
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.aevum.asm.accessors;

import net.minecraft.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={MultiPlayerGameMode.class})
public interface IClientPlayerInteractionManager {
    @Accessor(value="lastSelectedSlot")
    public int getLastSelectedSlot();

    @Accessor(value="lastSelectedSlot")
    public void setLastSelectedSlot(int var1);
}

