/*
 * Decompiled with CFR 0.152.
 */
package dev.chun.mod.modules.impl.movement;

import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.UpdateEvent;
import dev.chun.asm.accessors.ILivingEntity;
import dev.chun.mod.modules.Module;

public class NoJumpDelay
extends Module {
    public static NoJumpDelay INSTANCE;

    public NoJumpDelay() {
        super("NoJumpDelay", Module.Category.Movement);
        this.setChinese("\u65e0\u8df3\u8dc3\u51b7\u5374");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        ((ILivingEntity)NoJumpDelay.mc.player).setLastJumpCooldown(0);
    }
}

