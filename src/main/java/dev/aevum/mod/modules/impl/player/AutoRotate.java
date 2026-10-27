/*
 * Decompiled with CFR 0.152.
 */
package dev.aevum.mod.modules.impl.player;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.SendMovementPacketsEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.SliderSetting;

public class AutoRotate
extends Module {
    public static AutoRotate INSTANCE;
    private final SliderSetting speed = this.add(new SliderSetting("Speed", 3.0, 1.0, 10.0, 0.1));
    private float rotateYaw;

    public AutoRotate() {
        super("AutoRotate", Module.Category.Player);
        this.setChinese("\u5e7b\u5f71\u65cb\u8f6c");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (AutoRotate.nullCheck()) {
            this.disable();
            return;
        }
        this.rotateYaw = AutoRotate.mc.player.getYRot();
    }

    @Override
    public void onDisable() {
        if (AutoRotate.nullCheck()) {
            return;
        }
        AutoRotate.mc.player.input.up = false;
        AutoRotate.mc.player.input.down = false;
        AutoRotate.mc.player.input.left = false;
        AutoRotate.mc.player.input.right = false;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        float rotationsPerSecond = this.speed.getValueFloat();
        float yawIncrement = rotationsPerSecond * 360.0f / 20.0f;
        this.rotateYaw += yawIncrement;
        Aevum.ROTATION.setRenderRotation(this.rotateYaw, AutoRotate.mc.player.getXRot(), true);
    }

    @EventListener
    public void onSendMovement(SendMovementPacketsEvent event) {
        event.setYaw(this.rotateYaw);
    }
}

