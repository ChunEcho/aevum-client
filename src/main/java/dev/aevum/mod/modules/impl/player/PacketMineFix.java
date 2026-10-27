/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockPos
 */
package dev.aevum.mod.modules.impl.player;

import net.minecraft.BlockPos;

public final class PacketMineFix {
    private static final BlockPos FALLBACK = new BlockPos(0, 0, 0);

    private PacketMineFix() {
    }

    public static BlockPos safePos(BlockPos BlockPos) {
        return BlockPos == null ? FALLBACK : BlockPos;
    }
}

