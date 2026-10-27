/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 */
package dev.aevum.api.events.impl;

import dev.aevum.api.events.Event;
import net.minecraft.Entity;

public class RenderEntityEvent
extends Event {
    private static final RenderEntityEvent INSTANCE = new RenderEntityEvent();
    private Entity entity;

    private RenderEntityEvent() {
    }

    public static RenderEntityEvent get(Entity entity) {
        RenderEntityEvent.INSTANCE.entity = entity;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }

    public Entity getEntity() {
        return this.entity;
    }
}

