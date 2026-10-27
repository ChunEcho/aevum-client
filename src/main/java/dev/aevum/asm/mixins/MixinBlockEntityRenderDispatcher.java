/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockEntity
 *  net.minecraft.PoseStack
 *  net.minecraft.MultiBufferSource
 *  net.minecraft.BlockEntityRenderDispatcher
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.Aevum;
import dev.aevum.api.events.impl.RenderBlockEntityEvent;
import net.minecraft.BlockEntity;
import net.minecraft.PoseStack;
import net.minecraft.MultiBufferSource;
import net.minecraft.BlockEntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BlockEntityRenderDispatcher.class})
public abstract class MixinBlockEntityRenderDispatcher {
    @Inject(method={"render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private <E extends BlockEntity> void onRenderEntity(E blockEntity, float tickDelta, PoseStack matrix, MultiBufferSource vertexConsumerProvider, CallbackInfo info) {
        RenderBlockEntityEvent event = Aevum.EVENT_BUS.post(RenderBlockEntityEvent.get(blockEntity));
        if (event.isCancelled()) {
            info.cancel();
        }
    }
}

