/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockItem
 *  net.minecraft.BlockPlaceContext
 *  net.minecraft.BlockState
 *  org.jetbrains.annotations.NotNull
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.aevum.asm.mixins;

import dev.aevum.Aevum;
import dev.aevum.api.events.impl.PlaceBlockEvent;
import dev.aevum.mod.modules.Module;
import net.minecraft.BlockItem;
import net.minecraft.BlockPlaceContext;
import net.minecraft.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={BlockItem.class})
public class MixinBlockItem {
    @Inject(method={"place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z"}, at={@At(value="RETURN")})
    private void onPlace(@NotNull BlockPlaceContext context, BlockState state, CallbackInfoReturnable<Boolean> info) {
        if (Module.nullCheck()) {
            return;
        }
        if (context.getLevel().isClientSide) {
            Aevum.EVENT_BUS.post(PlaceBlockEvent.get(context.getClickedPos(), state.method_26204()));
        }
    }
}

