/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.ServerboundPlayerActionPacket
 *  net.minecraft.ServerboundPlayerActionPacket$Action
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.aevum.asm.accessors;

import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.ServerboundPlayerActionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ServerboundPlayerActionPacket.class})
public interface IPlayerActionC2SPacket {
    @Accessor(value="pos")
    public BlockPos getPos();

    @Accessor(value="direction")
    public Direction getDirection();

    @Accessor(value="action")
    public ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action getAction();
}

