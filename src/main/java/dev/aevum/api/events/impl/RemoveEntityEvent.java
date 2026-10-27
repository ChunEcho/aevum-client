/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Entity$RemovalReason
 */
package dev.aevum.api.events.impl;

import net.minecraft.Entity;

public class RemoveEntityEvent {
    public static final RemoveEntityEvent instance = new RemoveEntityEvent();
    private Entity entity;
    private Entity.Entity$RemovalReason removalReason;

    private RemoveEntityEvent() {
    }

    public static RemoveEntityEvent get(Entity entity, Entity.Entity$RemovalReason removalReason) {
        RemoveEntityEvent.instance.entity = entity;
        RemoveEntityEvent.instance.removalReason = removalReason;
        return instance;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public Entity.Entity$RemovalReason getRemovalReason() {
        return this.removalReason;
    }
}

