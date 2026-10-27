/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockEntity
 */
package dev.aevum.api.events.impl;

import dev.aevum.api.events.Event;
import net.minecraft.BlockEntity;

public class RenderBlockEntityEvent
extends Event {
    private static final RenderBlockEntityEvent INSTANCE = new RenderBlockEntityEvent();
    public BlockEntity blockEntity;

    public static RenderBlockEntityEvent get(BlockEntity blockEntity) {
        INSTANCE.setCancelled(false);
        RenderBlockEntityEvent.INSTANCE.blockEntity = blockEntity;
        return INSTANCE;
    }
}

