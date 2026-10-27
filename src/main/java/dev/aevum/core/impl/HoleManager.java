/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.AABB
 *  net.minecraft.Mth
 */
package dev.aevum.core.impl;

import dev.aevum.Aevum;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.impl.combat.Breaker;
import net.minecraft.Blocks;
import net.minecraft.Block;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.AABB;
import net.minecraft.Mth;

public class HoleManager
implements Wrapper {
    public boolean isHole(BlockPos pos) {
        return this.isHole(pos, true, false, false);
    }

    public boolean isHole(BlockPos pos, boolean canStand, boolean checkTrap, boolean anyBlock) {
        int blockProgress = 0;
        for (Direction i : Direction.values()) {
            if (i == Direction.UP || i == Direction.DOWN || (!anyBlock || HoleManager.mc.level.isEmptyBlock(pos.method_10093(i))) && !Aevum.HOLE.isHard(pos.method_10093(i))) continue;
            ++blockProgress;
        }
        return !(checkTrap && (!HoleManager.mc.level.isEmptyBlock(pos) || !HoleManager.mc.level.isEmptyBlock(pos.method_10084()) || !HoleManager.mc.level.isEmptyBlock(pos.method_10086(1)) || !HoleManager.mc.level.isEmptyBlock(pos.method_10086(2)) || HoleManager.mc.player.getBlockY() - 1 > pos.method_10264() && !HoleManager.mc.level.isEmptyBlock(pos.method_10086(3)) || HoleManager.mc.player.getBlockY() - 2 > pos.method_10264() && !HoleManager.mc.level.isEmptyBlock(pos.method_10086(4))) || blockProgress <= 3 || canStand && !BlockUtil.canCollide(new AABB(pos.method_10069(0, -1, 0))));
    }

    public BlockPos getHole(float range, boolean doubleHole, boolean any, boolean up) {
        BlockPos bestPos = null;
        double bestDistance = range + 1.0f;
        for (BlockPos pos : BlockUtil.getSphere(range, HoleManager.mc.player.position())) {
            if ((pos.method_10263() != HoleManager.mc.player.getBlockX() || pos.getZ() != HoleManager.mc.player.getBlockZ()) && !up && (double)(pos.method_10264() + 1) > HoleManager.mc.player.getY() || !Aevum.HOLE.isHole(pos, true, true, any) && (!doubleHole || !this.isDoubleHole(pos)) || pos.method_10264() - HoleManager.mc.player.getBlockY() > 1) continue;
            double getDistance = Mth.method_15355((float)((float)HoleManager.mc.player.method_5649((double)pos.method_10263() + 0.5, (double)pos.method_10264() + 0.5, (double)pos.getZ() + 0.5)));
            if (bestPos != null && !(getDistance < bestDistance)) continue;
            bestPos = pos;
            bestDistance = getDistance;
        }
        return bestPos;
    }

    public boolean isDoubleHole(BlockPos pos) {
        Direction unHardFacing = this.is3Block(pos);
        if (unHardFacing != null) {
            return (unHardFacing = this.is3Block(pos = pos.method_10093(unHardFacing))) != null;
        }
        return false;
    }

    public Direction is3Block(BlockPos pos) {
        if (!this.isHard(pos.method_10074())) {
            return null;
        }
        if (!(HoleManager.mc.level.isEmptyBlock(pos) && HoleManager.mc.level.isEmptyBlock(pos.method_10084()) && HoleManager.mc.level.isEmptyBlock(pos.method_10086(2)))) {
            return null;
        }
        int progress = 0;
        Direction unHardFacing = null;
        for (Direction facing : Direction.values()) {
            if (facing == Direction.UP || facing == Direction.DOWN) continue;
            if (this.isHard(pos.method_10093(facing))) {
                ++progress;
                continue;
            }
            int progress2 = 0;
            for (Direction facing2 : Direction.values()) {
                if (facing2 == Direction.DOWN || facing2 == facing.getOpposite() || !this.isHard(pos.method_10093(facing).method_10093(facing2))) continue;
                ++progress2;
            }
            if (progress2 == 4) {
                ++progress;
                continue;
            }
            unHardFacing = facing;
        }
        if (progress == 3) {
            return unHardFacing;
        }
        return null;
    }

    public boolean isHard(BlockPos pos) {
        Block block = HoleManager.mc.level.method_8320(pos).method_26204();
        return this.isHard(block);
    }

    public boolean isHard(Block block) {
        return block == Blocks.BEDROCK || Breaker.hard.contains(block);
    }
}

