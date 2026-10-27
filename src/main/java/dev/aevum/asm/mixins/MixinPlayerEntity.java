/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Player
 *  net.minecraft.Vec3
 *  net.minecraft.Pose
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.aevum.asm.mixins;

import dev.aevum.Aevum;
import dev.aevum.api.events.Event;
import dev.aevum.api.events.impl.JumpEvent;
import dev.aevum.api.events.impl.TravelEvent;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import dev.aevum.mod.modules.impl.player.InteractTweaks;
import net.minecraft.Player;
import net.minecraft.Vec3;
import net.minecraft.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Player.class})
public class MixinPlayerEntity
implements Wrapper {
    @Inject(method={"canChangeIntoPose"}, at={@At(value="RETURN")}, cancellable=true)
    private void poseNotCollide(Pose pose, CallbackInfoReturnable<Boolean> cir) {
        if (Player.class.cast(this) == MixinPlayerEntity.mc.player && !ClientSetting.INSTANCE.crawl.getValue() && pose == Pose.SWIMMING) {
            cir.setReturnValue((Object)false);
        }
    }

    @Inject(method={"getBlockInteractionRange"}, at={@At(value="HEAD")}, cancellable=true)
    public void getBlockInteractionRangeHook(CallbackInfoReturnable<Double> cir) {
        if (InteractTweaks.INSTANCE.reach()) {
            cir.setReturnValue((Object)InteractTweaks.INSTANCE.blockRange.getValue());
        }
    }

    @Inject(method={"getEntityInteractionRange"}, at={@At(value="HEAD")}, cancellable=true)
    public void getEntityInteractionRangeHook(CallbackInfoReturnable<Double> cir) {
        if (InteractTweaks.INSTANCE.reach()) {
            cir.setReturnValue((Object)InteractTweaks.INSTANCE.entityRange.getValue());
        }
    }

    @Inject(method={"jump"}, at={@At(value="HEAD")})
    private void onJumpPre(CallbackInfo ci) {
        Aevum.EVENT_BUS.post(JumpEvent.get(Event.Stage.Pre));
    }

    @Inject(method={"jump"}, at={@At(value="RETURN")})
    private void onJumpPost(CallbackInfo ci) {
        Aevum.EVENT_BUS.post(JumpEvent.get(Event.Stage.Post));
    }

    @Inject(method={"travel"}, at={@At(value="HEAD")}, cancellable=true)
    private void onTravelPre(Vec3 movementInput, CallbackInfo ci) {
        Player player = (Player)Player.class.cast(this);
        if (player != MixinPlayerEntity.mc.player) {
            return;
        }
        TravelEvent event = TravelEvent.get(Event.Stage.Pre, player);
        Aevum.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
            event = TravelEvent.get(Event.Stage.Post, player);
            Aevum.EVENT_BUS.post(event);
        }
    }

    @Inject(method={"travel"}, at={@At(value="RETURN")})
    private void onTravelPost(Vec3 movementInput, CallbackInfo ci) {
        Player player = (Player)Player.class.cast(this);
        if (player != MixinPlayerEntity.mc.player) {
            return;
        }
        TravelEvent event = TravelEvent.get(Event.Stage.Post, player);
        Aevum.EVENT_BUS.post(event);
    }
}

