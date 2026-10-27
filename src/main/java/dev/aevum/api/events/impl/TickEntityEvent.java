/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 */
package dev.aevum.api.events.impl;

import dev.aevum.api.events.Event;
import net.minecraft.Entity;

public class TickEntityEvent
extends Event {
    private static final TickEntityEvent INSTANCE = new TickEntityEvent();
    private Entity entity;

    private TickEntityEvent() {
    }

    public static TickEntityEvent get(Entity entity) {
        TickEntityEvent.INSTANCE.entity = entity;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }

    public Entity getEntity() {
        return this.entity;
    }
}

