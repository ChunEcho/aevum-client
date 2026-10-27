/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 */
package dev.aevum.api.events.impl;

import dev.aevum.api.events.Event;
import net.minecraft.InteractionHand;

public class InteractBlockEvent
extends Event {
    private static final InteractBlockEvent INSTANCE = new InteractBlockEvent();
    public InteractionHand hand;

    private InteractBlockEvent() {
    }

    public static InteractBlockEvent getPre(InteractionHand hand) {
        InteractBlockEvent.INSTANCE.hand = hand;
        InteractBlockEvent.INSTANCE.stage = Event.Stage.Pre;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }

    public static InteractBlockEvent getPost(InteractionHand hand) {
        InteractBlockEvent.INSTANCE.hand = hand;
        InteractBlockEvent.INSTANCE.stage = Event.Stage.Post;
        return INSTANCE;
    }
}

