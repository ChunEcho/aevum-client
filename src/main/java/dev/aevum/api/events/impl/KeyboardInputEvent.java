/*
 * Decompiled with CFR 0.152.
 */
package dev.aevum.api.events.impl;

import dev.aevum.api.events.Event;

public class KeyboardInputEvent
extends Event {
    private static final KeyboardInputEvent INSTANCE = new KeyboardInputEvent();

    private KeyboardInputEvent() {
    }

    public static KeyboardInputEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}

