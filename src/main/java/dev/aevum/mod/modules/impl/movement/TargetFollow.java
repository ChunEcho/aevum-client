/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Player
 *  net.minecraft.Vec3
 */
package dev.aevum.mod.modules.impl.movement;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateRotateEvent;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.core.impl.RotationManager;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.movement.ElytraFly;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.Entity;
import net.minecraft.Player;
import net.minecraft.Vec3;

public class TargetFollow
extends Module {
    private final SliderSetting range = this.add(new SliderSetting("Range", 50.0, 1.0, 100.0, 1.0));

    public TargetFollow() {
        super("TargetFollow", Module.Category.Movement);
        this.setChinese("\u8ddf\u968f\u76ee\u6807");
    }

    @Override
    public void onDisable() {
        if (TargetFollow.nullCheck()) {
            return;
        }
        TargetFollow.mc.options.keyUp.method_23481(false);
    }

    @EventListener(priority=-9999)
    public void onRotation(UpdateRotateEvent event) {
        if (TargetFollow.nullCheck()) {
            return;
        }
        Player target = this.getClosestPlayer(this.range.getValue());
        if (target != null && ElytraFly.INSTANCE.isFallFlying()) {
            Vec3 pos = MathUtil.getClosestPointToBox(TargetFollow.mc.player.getEyePosition(), target.method_5829());
            float[] rotations = RotationManager.getRotation(pos);
            event.setYaw(rotations[0]);
            event.setPitch(rotations[1]);
            TargetFollow.mc.options.keyUp.method_23481(true);
        }
    }

    private Player getClosestPlayer(double range) {
        Player target = null;
        double distance = range;
        for (Entity entity : Aevum.THREAD.threadSafeEntityList) {
            if (!(entity instanceof Player)) continue;
            Player player = (Player)entity;
            if (entity == TargetFollow.mc.player || Aevum.FRIEND.isFriend(player.method_5477().getString()) || (double)TargetFollow.mc.player.method_5739(entity) > range) continue;
            if (target == null) {
                target = player;
                distance = TargetFollow.mc.player.method_5739(entity);
                continue;
            }
            if ((double)TargetFollow.mc.player.method_5739(entity) >= distance) continue;
            target = player;
            distance = TargetFollow.mc.player.method_5739(entity);
        }
        return target;
    }
}

