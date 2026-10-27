/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Player
 */
package dev.aevum.api.events.impl;

import net.minecraft.Player;

public class TotemEvent {
    private static final TotemEvent INSTANCE = new TotemEvent();
    private Player player;

    private TotemEvent() {
    }

    public static TotemEvent get(Player player) {
        TotemEvent.INSTANCE.player = player;
        return INSTANCE;
    }

    public Player getPlayer() {
        return this.player;
    }
}

