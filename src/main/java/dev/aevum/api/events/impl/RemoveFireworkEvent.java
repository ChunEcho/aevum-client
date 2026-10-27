/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.FireworkRocketEntity
 */
package dev.aevum.api.events.impl;

import dev.aevum.api.events.Event;
import net.minecraft.FireworkRocketEntity;

public class RemoveFireworkEvent
extends Event {
    public static final RemoveFireworkEvent instance = new RemoveFireworkEvent();
    private FireworkRocketEntity entity;

    private RemoveFireworkEvent() {
    }

    public static RemoveFireworkEvent get(FireworkRocketEntity entity) {
        RemoveFireworkEvent.instance.entity = entity;
        instance.setCancelled(false);
        return instance;
    }

    public FireworkRocketEntity getRocketEntity() {
        return this.entity;
    }
}

