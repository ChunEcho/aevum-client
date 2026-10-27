/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ServerboundContainerClosePacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.aevum.asm.accessors;

import net.minecraft.ServerboundContainerClosePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ServerboundContainerClosePacket.class})
public interface ICloseHandledScreenC2SPacket {
    @Accessor(value="syncId")
    public int getSyncId();
}

