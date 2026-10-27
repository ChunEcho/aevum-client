/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.LivingEntity
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.BushBlock
 *  net.minecraft.BlockPos
 *  net.minecraft.Vec3
 *  net.minecraft.WallSignBlock
 */
package dev.aevum.api.utils.path;

import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.path.AStarCustomPathFinder;
import dev.aevum.api.utils.path.Vec3;
import dev.aevum.api.utils.world.BlockPosX;
import java.util.ArrayList;
import net.minecraft.LivingEntity;
import net.minecraft.Blocks;
import net.minecraft.Block;
import net.minecraft.BushBlock;
import net.minecraft.BlockPos;
import net.minecraft.Vec3;
import net.minecraft.WallSignBlock;

public class PathUtils
implements Wrapper {
    public static final double range = 9.0;

    private static boolean canPassThrough(BlockPos pos) {
        Block block = PathUtils.mc.level.method_8320(new BlockPos(pos.method_10263(), pos.method_10264(), pos.getZ())).method_26204();
        return block == Blocks.AIR || block instanceof BushBlock || block == Blocks.VINE || block == Blocks.LADDER || block == Blocks.WATER || block == Blocks.WATER_CAULDRON || block instanceof WallSignBlock;
    }

    public static ArrayList<Vec3> computePath(LivingEntity fromEntity, LivingEntity toEntity) {
        return PathUtils.computePath(new Vec3(fromEntity.getX(), fromEntity.getY(), fromEntity.getZ()), new Vec3(toEntity.getX(), toEntity.getY(), toEntity.getZ()));
    }

    public static ArrayList<Vec3> computePath(Vec3 vec3d) {
        return PathUtils.computePath(new Vec3(PathUtils.mc.player.getX(), PathUtils.mc.player.getY(), PathUtils.mc.player.getZ()), new Vec3(vec3d.x, vec3d.y, vec3d.z));
    }

    public static ArrayList<Vec3> computePath(Vec3 topFrom, Vec3 to) {
        if (!PathUtils.canPassThrough(new BlockPosX(topFrom.mc()))) {
            topFrom = topFrom.addVector(0.0, 1.0, 0.0);
        }
        AStarCustomPathFinder pathfinder = new AStarCustomPathFinder(topFrom, to);
        pathfinder.compute();
        int i = 0;
        Vec3 lastLoc = null;
        Vec3 lastDashLoc = null;
        ArrayList<Vec3> path = new ArrayList<Vec3>();
        ArrayList<Vec3> pathFinderPath = pathfinder.getPath();
        for (Vec3 pathElm : pathFinderPath) {
            if (i != 0 && i != pathFinderPath.size() - 1) {
                boolean canContinue = true;
                if (pathElm.squareDistanceTo(lastDashLoc) > 9.0) {
                    canContinue = false;
                } else {
                    double smallX = Math.min(lastDashLoc.x(), pathElm.x());
                    double smallY = Math.min(lastDashLoc.y(), pathElm.y());
                    double smallZ = Math.min(lastDashLoc.z(), pathElm.z());
                    double bigX = Math.max(lastDashLoc.x(), pathElm.x());
                    double bigY = Math.max(lastDashLoc.y(), pathElm.y());
                    double bigZ = Math.max(lastDashLoc.z(), pathElm.z());
                    int x = (int)smallX;
                    block1: while ((double)x <= bigX) {
                        int y = (int)smallY;
                        while ((double)y <= bigY) {
                            int z = (int)smallZ;
                            while ((double)z <= bigZ) {
                                if (!AStarCustomPathFinder.checkPositionValidity(x, y, z, false)) {
                                    canContinue = false;
                                    break block1;
                                }
                                ++z;
                            }
                            ++y;
                        }
                        ++x;
                    }
                }
                if (!canContinue) {
                    if (!path.contains(lastLoc.addVector(0.5, 0.0, 0.5))) {
                        path.add(lastLoc.addVector(0.5, 0.0, 0.5));
                    }
                    lastDashLoc = lastLoc;
                }
            } else {
                if (lastLoc != null && !path.contains(lastLoc.addVector(0.5, 0.0, 0.5))) {
                    path.add(lastLoc.addVector(0.5, 0.0, 0.5));
                }
                if (!path.contains(pathElm.addVector(0.5, 0.0, 0.5))) {
                    path.add(pathElm.addVector(0.5, 0.0, 0.5));
                }
                lastDashLoc = pathElm;
            }
            lastLoc = pathElm;
            ++i;
        }
        return path;
    }
}

