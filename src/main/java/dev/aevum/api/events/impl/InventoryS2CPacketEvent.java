/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ClientboundContainerSetContentPacket
 */
package dev.aevum.api.events.impl;

import dev.aevum.api.events.Event;
import net.minecraft.ClientboundContainerSetContentPacket;

public class InventoryS2CPacketEvent
extends Event {
    private static final InventoryS2CPacketEvent INSTANCE = new InventoryS2CPacketEvent();
    public ClientboundContainerSetContentPacket packet;

    private InventoryS2CPacketEvent() {
    }

    public static InventoryS2CPacketEvent get(ClientboundContainerSetContentPacket hand) {
        InventoryS2CPacketEvent.INSTANCE.packet = hand;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}

