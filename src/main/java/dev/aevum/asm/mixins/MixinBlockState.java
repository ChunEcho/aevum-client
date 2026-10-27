/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  net.minecraft.InteractionResult
 *  net.minecraft.Player
 *  net.minecraft.Level
 *  net.minecraft.Block
 *  net.minecraft.BlockState
 *  net.minecraft.Property
 *  net.minecraft.BlockHitResult
 *  net.minecraft.BlockBehaviour$BlockStateBase
 *  org.spongepowered.asm.mixin.Mixin
 */
package dev.aevum.asm.mixins;

import com.mojang.serialization.MapCodec;
import dev.aevum.Aevum;
import dev.aevum.api.events.impl.BlockActivateEvent;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.InteractionResult;
import net.minecraft.Player;
import net.minecraft.Level;
import net.minecraft.Block;
import net.minecraft.BlockState;
import net.minecraft.Property;
import net.minecraft.BlockHitResult;
import net.minecraft.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={BlockState.class})
public abstract class MixinBlockState
extends BlockBehaviour.BlockBehaviour$BlockStateBase {
    public MixinBlockState(Block block, Reference2ObjectArrayMap<Property<?>, Comparable<?>> propertyMap, MapCodec<BlockState> mapCodec) {
        super(block, propertyMap, mapCodec);
    }

    public InteractionResult method_55781(Level world, Player player, BlockHitResult hit) {
        Aevum.EVENT_BUS.post(BlockActivateEvent.get((BlockState)BlockState.class.cast((Object)this)));
        return super.method_55781(world, player, hit);
    }
}

