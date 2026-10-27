/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.InteractionResult
 *  net.minecraft.Player
 *  net.minecraft.ItemStack
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.BlockHitResult
 *  net.minecraft.MultiPlayerGameMode
 *  net.minecraft.LocalPlayer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Constant
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyConstant
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.aevum.asm.mixins;

import dev.aevum.Aevum;
import dev.aevum.api.events.impl.ClickBlockEvent;
import dev.aevum.api.events.impl.InteractBlockEvent;
import dev.aevum.api.events.impl.InteractItemEvent;
import dev.aevum.mod.modules.impl.player.InteractTweaks;
import net.minecraft.InteractionHand;
import net.minecraft.InteractionResult;
import net.minecraft.Player;
import net.minecraft.ItemStack;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.BlockHitResult;
import net.minecraft.MultiPlayerGameMode;
import net.minecraft.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={MultiPlayerGameMode.class})
public class MixinClientPlayerInteractionManager {
    @Shadow
    private ItemStack destroyingItem;

    @ModifyVariable(method={"isCurrentlyBreaking"}, at=@At(value="STORE"))
    private ItemStack stack(ItemStack stack) {
        return InteractTweaks.INSTANCE.noReset() ? this.destroyingItem : stack;
    }

    @ModifyConstant(method={"updateBlockBreakingProgress"}, constant={@Constant(intValue=5)})
    private int MiningCooldownFix(int value) {
        return InteractTweaks.INSTANCE.noDelay() ? 0 : value;
    }

    @Inject(method={"interactItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void hookInteractItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        InteractItemEvent event = InteractItemEvent.getPre(hand);
        Aevum.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            cir.setReturnValue((Object)InteractionResult.PASS);
        }
    }

    @Inject(method={"interactItem"}, at={@At(value="RETURN")})
    private void hookInteractItemReturn(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Aevum.EVENT_BUS.post(InteractItemEvent.getPost(hand));
    }

    @Inject(method={"interactBlock"}, at={@At(value="HEAD")}, cancellable=true)
    private void hookInteractBlock(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        InteractBlockEvent event = InteractBlockEvent.getPre(hand);
        Aevum.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            cir.setReturnValue((Object)InteractionResult.PASS);
        }
    }

    @Inject(method={"interactBlock"}, at={@At(value="RETURN")})
    private void hookInteractBlockReturn(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        Aevum.EVENT_BUS.post(InteractBlockEvent.getPost(hand));
    }

    @Inject(method={"cancelBlockBreaking"}, at={@At(value="HEAD")}, cancellable=true)
    private void hookCancelBlockBreaking(CallbackInfo callbackInfo) {
        if (InteractTweaks.INSTANCE.noAbort()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"attackBlock"}, at={@At(value="HEAD")}, cancellable=true)
    private void onAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        ClickBlockEvent event = ClickBlockEvent.get(pos, direction);
        Aevum.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            cir.setReturnValue((Object)false);
        }
    }
}

