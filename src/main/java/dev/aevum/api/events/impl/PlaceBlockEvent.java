/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Block
 *  net.minecraft.BlockPos
 */
package dev.aevum.api.events.impl;

import net.minecraft.Block;
import net.minecraft.BlockPos;

public class PlaceBlockEvent {
    private static final PlaceBlockEvent INSTANCE = new PlaceBlockEvent();
    public BlockPos blockPos;
    public Block block;

    private PlaceBlockEvent() {
    }

    public static PlaceBlockEvent get(BlockPos blockPos, Block block) {
        PlaceBlockEvent.INSTANCE.blockPos = blockPos;
        PlaceBlockEvent.INSTANCE.block = block;
        return INSTANCE;
    }
}

