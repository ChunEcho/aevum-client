/*
 * Decompiled with CFR 0.152.
 */
package dev.aevum.mod.modules.impl.movement;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.asm.accessors.ILivingEntity;
import dev.aevum.mod.modules.Module;

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

