/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Player
 */
package dev.aevum.api.events.impl;

import net.minecraft.Player;

public class DeathEvent {
    private static final DeathEvent INSTANCE = new DeathEvent();
    private Player player;

    private DeathEvent() {
    }

    public static DeathEvent get(Player player) {
        DeathEvent.INSTANCE.player = player;
        return INSTANCE;
    }

    public Player getPlayer() {
        return this.player;
    }
}

