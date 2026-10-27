/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Screen
 */
package dev.aevum.api.events.impl;

import dev.aevum.api.events.Event;
import net.minecraft.Screen;

public class OpenScreenEvent
extends Event {
    private static final OpenScreenEvent INSTANCE = new OpenScreenEvent();
    public Screen screen;

    private OpenScreenEvent() {
    }

    public static OpenScreenEvent get(Screen screen) {
        OpenScreenEvent.INSTANCE.screen = screen;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}

