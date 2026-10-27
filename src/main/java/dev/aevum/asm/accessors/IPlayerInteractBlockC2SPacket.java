/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.ServerboundUseItemOnPacket
 *  net.minecraft.BlockHitResult
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.aevum.asm.accessors;

import net.minecraft.InteractionHand;
import net.minecraft.ServerboundUseItemOnPacket;
import net.minecraft.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ServerboundUseItemOnPacket.class})
public interface IPlayerInteractBlockC2SPacket {
    @Accessor(value="blockHitResult")
    public BlockHitResult getBlockHitResult();

    @Accessor(value="hand")
    public InteractionHand getHand();
}

