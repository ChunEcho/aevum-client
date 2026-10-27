/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 */
package dev.aevum.api.events.impl;

import dev.aevum.api.events.Event;
import net.minecraft.InteractionHand;

public class InteractItemEvent
extends Event {
    private static final InteractItemEvent INSTANCE = new InteractItemEvent();
    public InteractionHand hand;

    private InteractItemEvent() {
    }

    public static InteractItemEvent getPre(InteractionHand hand) {
        InteractItemEvent.INSTANCE.hand = hand;
        InteractItemEvent.INSTANCE.stage = Event.Stage.Pre;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }

    public static InteractItemEvent getPost(InteractionHand hand) {
        InteractItemEvent.INSTANCE.hand = hand;
        InteractItemEvent.INSTANCE.stage = Event.Stage.Post;
        return INSTANCE;
    }
}

