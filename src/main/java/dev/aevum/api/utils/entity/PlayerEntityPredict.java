/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Player
 */
package dev.aevum.api.utils.entity;

import dev.aevum.api.utils.entity.CopyPlayerEntity;
import net.minecraft.Player;

public class PlayerEntityPredict {
    public final Player player;
    public final Player predict;

    public PlayerEntityPredict(Player player, double maxMotionY, int ticks, int simulation, boolean step, boolean doubleStep, boolean jump, boolean inBlockPause) {
        this.player = player;
        this.predict = ticks > 0 ? new CopyPlayerEntity(player, true, maxMotionY, ticks, simulation, step, doubleStep, jump, inBlockPause) : player;
    }
}

