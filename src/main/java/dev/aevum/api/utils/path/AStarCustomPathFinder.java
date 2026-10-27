/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.CactusBlock
 *  net.minecraft.ChestBlock
 *  net.minecraft.EnderChestBlock
 *  net.minecraft.BlockPos
 *  net.minecraft.FenceBlock
 *  net.minecraft.IronBarsBlock
 *  net.minecraft.SlabBlock
 *  net.minecraft.SkullBlock
 *  net.minecraft.StainedGlassBlock
 *  net.minecraft.StairBlock
 *  net.minecraft.TrapDoorBlock
 *  net.minecraft.WallBlock
 *  net.minecraft.PistonBaseBlock
 *  net.minecraft.MovingPistonBlock
 *  net.minecraft.PistonHeadBlock
 */
package dev.aevum.api.utils.path;

import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.path.Vec3;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.CactusBlock;
import net.minecraft.ChestBlock;
import net.minecraft.EnderChestBlock;
import net.minecraft.BlockPos;
import net.minecraft.FenceBlock;
import net.minecraft.IronBarsBlock;
import net.minecraft.SlabBlock;
import net.minecraft.SkullBlock;
import net.minecraft.StainedGlassBlock;
import net.minecraft.StairBlock;
import net.minecraft.TrapDoorBlock;
import net.minecraft.WallBlock;
import net.minecraft.PistonBaseBlock;
import net.minecraft.MovingPistonBlock;
import net.minecraft.PistonHeadBlock;

public class AStarCustomPathFinder
implements Wrapper {
    private static final Vec3[] flatCardinalDirections = new Vec3[]{new Vec3(1.0, 0.0, 0.0), new Vec3(-1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), new Vec3(0.0, 0.0, -1.0)};
    private final Vec3 startVec3;
    private final Vec3 endVec3;
    private final ArrayList<Hub> hubs = new ArrayList();
    private final ArrayList<Hub> hubsToWork = new ArrayList();
    private ArrayList<Vec3> path = new ArrayList();

    public AStarCustomPathFinder(Vec3 startVec3, Vec3 endVec3) {
        this.startVec3 = startVec3.addVector(0.0, 0.0, 0.0).floor();
        this.endVec3 = endVec3.addVector(0.0, 0.0, 0.0).floor();
    }

    public static boolean checkPositionValidity(Vec3 loc, boolean checkGround) {
        return AStarCustomPathFinder.checkPositionValidity((int)loc.x(), (int)loc.y(), (int)loc.z(), checkGround);
    }

    public static boolean checkPositionValidity(int x, int y, int z, boolean checkGround) {
        BlockPos block1 = new BlockPos(x, y, z);
        BlockPos block2 = new BlockPos(x, y + 1, z);
        BlockPos block3 = new BlockPos(x, y - 1, z);
        return !AStarCustomPathFinder.isBlockSolid(block1) && !AStarCustomPathFinder.isBlockSolid(block2) && (AStarCustomPathFinder.isBlockSolid(block3) || !checkGround) && AStarCustomPathFinder.isSafeToWalkOn(block3);
    }

    private static boolean isBlockSolid(BlockPos block) {
        return AStarCustomPathFinder.mc.level.method_8320((BlockPos)block).cache != null && AStarCustomPathFinder.mc.level.method_8320((BlockPos)block).cache.isCollisionShapeFullBlock || AStarCustomPathFinder.mc.level.method_8320(block).method_26204() instanceof SlabBlock || AStarCustomPathFinder.mc.level.method_8320(block).method_26204() instanceof StairBlock || AStarCustomPathFinder.mc.level.method_8320(block).method_26204() instanceof CactusBlock || AStarCustomPathFinder.mc.level.method_8320(block).method_26204() instanceof ChestBlock || AStarCustomPathFinder.mc.level.method_8320(block).method_26204() instanceof EnderChestBlock || AStarCustomPathFinder.mc.level.method_8320(block).method_26204() instanceof SkullBlock || AStarCustomPathFinder.mc.level.method_8320(block).method_26204() instanceof IronBarsBlock || AStarCustomPathFinder.mc.level.method_8320(block).method_26204() instanceof FenceBlock || AStarCustomPathFinder.mc.level.method_8320(block).method_26204() instanceof WallBlock || AStarCustomPathFinder.mc.level.method_8320(block).method_26204() instanceof StainedGlassBlock || AStarCustomPathFinder.mc.level.method_8320(block).method_26204() instanceof PistonBaseBlock || AStarCustomPathFinder.mc.level.method_8320(block).method_26204() instanceof MovingPistonBlock || AStarCustomPathFinder.mc.level.method_8320(block).method_26204() instanceof PistonHeadBlock || AStarCustomPathFinder.mc.level.method_8320(block).method_26204() instanceof StainedGlassBlock || AStarCustomPathFinder.mc.level.method_8320(block).method_26204() instanceof TrapDoorBlock;
    }

    private static boolean isSafeToWalkOn(BlockPos block) {
        return !(AStarCustomPathFinder.mc.level.method_8320(block).method_26204() instanceof FenceBlock) && !(AStarCustomPathFinder.mc.level.method_8320(block).method_26204() instanceof WallBlock);
    }

    public ArrayList<Vec3> getPath() {
        return this.path;
    }

    public void compute() {
        this.compute(1000, 4);
    }

    public void compute(int loops, int depth) {
        this.path.clear();
        this.hubsToWork.clear();
        ArrayList<Vec3> initPath = new ArrayList<Vec3>();
        initPath.add(this.startVec3);
        this.hubsToWork.add(new Hub(this.startVec3, null, initPath, this.startVec3.squareDistanceTo(this.endVec3), 0.0, 0.0));
        block0: for (int i = 0; i < loops; ++i) {
            this.hubsToWork.sort(new CompareHub());
            int j = 0;
            if (this.hubsToWork.isEmpty()) break;
            for (Hub o : new ArrayList<Hub>(this.hubsToWork)) {
                Vec3 loc2;
                if (++j > depth) continue;
                this.hubsToWork.remove(o);
                this.hubs.add(o);
                for (Vec3 direction : flatCardinalDirections) {
                    Vec3 loc = o.getLoc().add(direction).floor();
                    if (AStarCustomPathFinder.checkPositionValidity(loc, false) && this.addHub(o, loc, 0.0)) break block0;
                }
                Vec3 loc1 = o.getLoc().addVector(0.0, 1.0, 0.0).floor();
                if (AStarCustomPathFinder.checkPositionValidity(loc1, false) && this.addHub(o, loc1, 0.0) || AStarCustomPathFinder.checkPositionValidity(loc2 = o.getLoc().addVector(0.0, -1.0, 0.0).floor(), false) && this.addHub(o, loc2, 0.0)) break block0;
            }
        }
        this.hubs.sort(new CompareHub());
        this.path = this.hubs.getFirst().getPath();
    }

    public Hub isHubExisting(Vec3 loc) {
        for (Hub hub : this.hubs) {
            if (hub.getLoc().x() != loc.x() || hub.getLoc().y() != loc.y() || hub.getLoc().z() != loc.z()) continue;
            return hub;
        }
        for (Hub hub : this.hubsToWork) {
            if (hub.getLoc().x() != loc.x() || hub.getLoc().y() != loc.y() || hub.getLoc().z() != loc.z()) continue;
            return hub;
        }
        return null;
    }

    public boolean addHub(Hub parent, Vec3 loc, double cost) {
        Hub existingHub = this.isHubExisting(loc);
        double totalCost = cost;
        if (parent != null) {
            totalCost = cost + parent.getTotalCost();
        }
        if (existingHub == null) {
            double minDistanceSquared = 9.0;
            if (loc.x() == this.endVec3.x() && loc.y() == this.endVec3.y() && loc.z() == this.endVec3.z() || loc.squareDistanceTo(this.endVec3) <= minDistanceSquared) {
                this.path.clear();
                this.path = parent.getPath();
                this.path.add(loc);
                return true;
            }
            ArrayList<Vec3> path = new ArrayList<Vec3>(parent.getPath());
            path.add(loc);
            this.hubsToWork.add(new Hub(loc, parent, path, loc.squareDistanceTo(this.endVec3), cost, totalCost));
        } else if (existingHub.getCost() > cost) {
            ArrayList<Vec3> path = new ArrayList<Vec3>(parent.getPath());
            path.add(loc);
            existingHub.setLoc(loc);
            existingHub.setParent(parent);
            existingHub.setPath(path);
            existingHub.setSquareDistanceToFromTarget(loc.squareDistanceTo(this.endVec3));
            existingHub.setCost(cost);
            existingHub.setTotalCost(totalCost);
        }
        return false;
    }

    public static class Hub {
        private Vec3 loc;
        private Hub parent;
        private ArrayList<Vec3> path;
        private double squareDistanceToFromTarget;
        private double cost;
        private double totalCost;

        public Hub(Vec3 loc, Hub parent, ArrayList<Vec3> path, double squareDistanceToFromTarget, double cost, double totalCost) {
            this.loc = loc;
            this.parent = parent;
            this.path = path;
            this.squareDistanceToFromTarget = squareDistanceToFromTarget;
            this.cost = cost;
            this.totalCost = totalCost;
        }

        public Vec3 getLoc() {
            return this.loc;
        }

        public void setLoc(Vec3 loc) {
            this.loc = loc;
        }

        public Hub getParent() {
            return this.parent;
        }

        public void setParent(Hub parent) {
            this.parent = parent;
        }

        public ArrayList<Vec3> getPath() {
            return this.path;
        }

        public void setPath(ArrayList<Vec3> path) {
            this.path = path;
        }

        public double getSquareDistanceToFromTarget() {
            return this.squareDistanceToFromTarget;
        }

        public void setSquareDistanceToFromTarget(double squareDistanceToFromTarget) {
            this.squareDistanceToFromTarget = squareDistanceToFromTarget;
        }

        public double getCost() {
            return this.cost;
        }

        public void setCost(double cost) {
            this.cost = cost;
        }

        public double getTotalCost() {
            return this.totalCost;
        }

        public void setTotalCost(double totalCost) {
            this.totalCost = totalCost;
        }
    }

    public static class CompareHub
    implements Comparator<Hub> {
        @Override
        public int compare(Hub o1, Hub o2) {
            return (int)(o1.getSquareDistanceToFromTarget() + o1.getTotalCost() - (o2.getSquareDistanceToFromTarget() + o2.getTotalCost()));
        }
    }
}

