/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockGetter
 *  net.minecraft.BlockPos
 *  net.minecraft.Shapes
 *  net.minecraft.VoxelShape
 *  net.minecraft.BlockState
 *  net.minecraft.CollisionContext
 *  net.minecraft.BlockBehaviour
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.aevum.asm.mixins;

import dev.aevum.Aevum;
import dev.aevum.api.events.impl.AmbientOcclusionEvent;
import dev.aevum.mod.modules.impl.player.PacketMine;
import net.minecraft.BlockGetter;
import net.minecraft.BlockPos;
import net.minecraft.Shapes;
import net.minecraft.VoxelShape;
import net.minecraft.BlockState;
import net.minecraft.CollisionContext;
import net.minecraft.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={BlockBehaviour.class})
public abstract class MixinAbstractBlock {
    @Inject(method={"getAmbientOcclusionLightLevel"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetAmbientOcclusionLightLevel(BlockState state, BlockGetter world, BlockPos pos, CallbackInfoReturnable<Float> info) {
        AmbientOcclusionEvent event = Aevum.EVENT_BUS.post(AmbientOcclusionEvent.get());
        if (event.lightLevel != -1.0f) {
            info.setReturnValue((Object)Float.valueOf(event.lightLevel));
        }
    }

    @Inject(method={"getCollisionShape"}, at={@At(value="HEAD")}, cancellable=true)
    private void onComputeNextCollisionBox(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (PacketMine.INSTANCE != null && pos.equals((Object)PacketMine.getBreakPos()) && PacketMine.INSTANCE.noCollide.getValue() && PacketMine.ghost) {
            cir.setReturnValue((Object)Shapes.method_1073());
        }
    }
}

