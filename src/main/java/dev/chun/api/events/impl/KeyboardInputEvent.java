/*
 * Decompiled with CFR 0.152.
 */
package dev.chun.api.events.impl;

import dev.chun.api.events.Event;

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

