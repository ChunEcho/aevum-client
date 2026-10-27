/*
 * Decompiled with CFR 0.152.
 */
package dev.aevum.mod.modules.impl.movement;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.KeyboardInputEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.MovementUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;

public class Fly
extends Module {
    public static Fly INSTANCE;
    private final SliderSetting speedConfig = this.add(new SliderSetting("Speed", 2.5, 0.1, 10.0));
    private final SliderSetting vspeedConfig = this.add(new SliderSetting("VerticalSpeed", 1.0, 0.1, 5.0));
    private final BooleanSetting antiKickConfig = this.add(new BooleanSetting("AntiKick", true).setParent());
    private final BooleanSetting up = this.add(new BooleanSetting("Up", true, this.antiKickConfig::isOpen));
    private final BooleanSetting allowSneak = this.add(new BooleanSetting("AllowSneak", false));
    private final Timer antiKickTimer = new Timer();
    private final Timer antiKick2Timer = new Timer();

    public Fly() {
        super("Fly", Module.Category.Movement);
        this.setChinese("\u98de\u884c");
        INSTANCE = this;
    }

    @Override
    public void onLogin() {
        this.antiKickTimer.reset();
        this.antiKick2Timer.reset();
    }

    @Override
    public void onEnable() {
        if (Fly.nullCheck()) {
            return;
        }
        this.antiKickTimer.reset();
        this.antiKick2Timer.reset();
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.antiKickTimer.passed(3900L) && this.antiKickConfig.getValue() && !Fly.mc.player.onGround()) {
            MovementUtil.setMotionY(-0.04);
            this.antiKickTimer.reset();
        } else if (this.antiKick2Timer.passed(4000L) && this.antiKickConfig.getValue() && !Fly.mc.player.onGround() && this.up.getValue()) {
            MovementUtil.setMotionY(0.04);
            this.antiKick2Timer.reset();
        } else {
            MovementUtil.setMotionY(0.0);
            if (Fly.mc.options.keyJump.isDown()) {
                MovementUtil.setMotionY(this.vspeedConfig.getValue());
            } else if (Fly.mc.options.keyShift.isDown()) {
                MovementUtil.setMotionY(-this.vspeedConfig.getValue());
            }
        }
        double[] move = MovementUtil.directionSpeed(this.speedConfig.getValueFloat());
        MovementUtil.setMotionX(move[0]);
        MovementUtil.setMotionZ(move[1]);
    }

    @EventListener(priority=-100)
    public void keyboard(KeyboardInputEvent event) {
        if (!this.allowSneak.getValue()) {
            Fly.mc.player.input.shiftKeyDown = false;
        }
    }
}

