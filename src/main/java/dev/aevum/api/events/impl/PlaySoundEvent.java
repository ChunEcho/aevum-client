/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.SoundInstance
 */
package dev.aevum.api.events.impl;

import dev.aevum.api.events.Event;
import net.minecraft.SoundInstance;

public class PlaySoundEvent
extends Event {
    private static final PlaySoundEvent INSTANCE = new PlaySoundEvent();
    public SoundInstance sound;

    public static PlaySoundEvent get(SoundInstance sound) {
        INSTANCE.setCancelled(false);
        PlaySoundEvent.INSTANCE.sound = sound;
        return INSTANCE;
    }
}

