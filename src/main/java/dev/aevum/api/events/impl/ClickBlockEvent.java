/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 */
package dev.aevum.api.events.impl;

import dev.aevum.api.events.Event;
import net.minecraft.BlockPos;
import net.minecraft.Direction;

public class ClickBlockEvent
extends Event {
    private BlockPos pos;
    private Direction direction;
    private static final ClickBlockEvent INSTANCE = new ClickBlockEvent();

    private ClickBlockEvent() {
    }

    public Direction getDirection() {
        return this.direction;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public static ClickBlockEvent get(BlockPos pos, Direction direction) {
        ClickBlockEvent.INSTANCE.pos = pos;
        ClickBlockEvent.INSTANCE.direction = direction;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}

