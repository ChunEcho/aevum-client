/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Player
 */
package dev.aevum.api.events.impl;

import dev.aevum.api.events.Event;
import net.minecraft.Player;

public class TravelEvent
extends Event {
    private static final TravelEvent INSTANCE = new TravelEvent();
    private Player entity;

    private TravelEvent() {
    }

    public static TravelEvent get(Event.Stage stage, Player entity) {
        TravelEvent.INSTANCE.entity = entity;
        TravelEvent.INSTANCE.stage = stage;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }

    public Player getEntity() {
        return this.entity;
    }
}

