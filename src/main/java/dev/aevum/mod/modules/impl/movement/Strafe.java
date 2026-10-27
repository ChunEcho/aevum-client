/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.MobEffects
 */
package dev.aevum.mod.modules.impl.movement;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.MoveEvent;
import dev.aevum.api.utils.path.BaritoneUtil;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.MovementUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.movement.Fly;
import dev.aevum.mod.modules.impl.movement.HoleSnap;
import dev.aevum.mod.modules.impl.movement.Speed;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import java.util.Objects;
import net.minecraft.MobEffects;

public class Strafe
extends Module {
    public static Strafe INSTANCE;
    private final BooleanSetting airStop = this.add(new BooleanSetting("AirStop", true));
    private final BooleanSetting slowCheck = this.add(new BooleanSetting("SlowCheck", true));

    public Strafe() {
        super("Strafe", "Modifies sprinting", Module.Category.Movement);
        this.setChinese("\u7075\u6d3b\u79fb\u52a8");
        INSTANCE = this;
    }

    @EventListener
    public void onStrafe(MoveEvent event) {
        if (BaritoneUtil.isActive()) {
            return;
        }
        if (Strafe.mc.player.isShiftKeyDown() || Fly.INSTANCE.isOn() || HoleSnap.INSTANCE.isOn() || Speed.INSTANCE.isOn() || Strafe.mc.player.isFallFlying() || EntityUtil.isInsideBlock() || Strafe.mc.player.isInLava() || Strafe.mc.player.isInWater() || Strafe.mc.player.getAbilities().flying) {
            return;
        }
        if (!MovementUtil.isMoving()) {
            if (this.airStop.getValue()) {
                MovementUtil.setMotionX(0.0);
                MovementUtil.setMotionZ(0.0);
            }
            return;
        }
        double[] dir = MovementUtil.directionSpeed(this.getBaseMoveSpeed());
        event.setX(dir[0]);
        event.setZ(dir[1]);
    }

    public double getBaseMoveSpeed() {
        double n = 0.2873;
        if (!(!Strafe.mc.player.method_6059(MobEffects.MOVEMENT_SPEED) || this.slowCheck.getValue() && Strafe.mc.player.method_6059(MobEffects.MOVEMENT_SLOWDOWN))) {
            n *= 1.0 + 0.2 * (double)(Objects.requireNonNull(Strafe.mc.player.method_6112(MobEffects.MOVEMENT_SPEED)).method_5578() + 1);
        }
        return n;
    }
}

