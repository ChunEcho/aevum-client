/*
 * Decompiled with CFR 0.152.
 */
package dev.chun.api.events.impl;

import dev.chun.api.events.Event;

public class ClientTickEvent
extends Event {
    private static final ClientTickEvent instance = new ClientTickEvent();

    private ClientTickEvent() {
    }

    public static ClientTickEvent get(Event.Stage stage) {
        ClientTickEvent.instance.stage = stage;
        return instance;
    }
}

