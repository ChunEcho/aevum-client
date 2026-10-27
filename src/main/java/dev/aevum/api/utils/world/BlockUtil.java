/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.Entity
 *  net.minecraft.ExperienceOrb
 *  net.minecraft.EndCrystal
 *  net.minecraft.ArmorStand
 *  net.minecraft.ItemEntity
 *  net.minecraft.Arrow
 *  net.minecraft.ThrownExperienceBottle
 *  net.minecraft.Items
 *  net.minecraft.BlockGetter
 *  net.minecraft.ChunkPos
 *  net.minecraft.Level
 *  net.minecraft.CollisionGetter
 *  net.minecraft.AnvilBlock
 *  net.minecraft.BasePressurePlateBlock
 *  net.minecraft.BaseEntityBlock
 *  net.minecraft.BedBlock
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.ButtonBlock
 *  net.minecraft.CraftingTableBlock
 *  net.minecraft.DoorBlock
 *  net.minecraft.BlockPos
 *  net.minecraft.FenceGateBlock
 *  net.minecraft.Direction
 *  net.minecraft.AABB
 *  net.minecraft.LoomBlock
 *  net.minecraft.NoteBlock
 *  net.minecraft.Vec3
 *  net.minecraft.TrapDoorBlock
 *  net.minecraft.BlockEntity
 *  net.minecraft.Packet
 *  net.minecraft.VoxelShape
 *  net.minecraft.BlockState
 *  net.minecraft.LevelChunk
 *  net.minecraft.ServerboundPlayerActionPacket
 *  net.minecraft.ServerboundPlayerActionPacket$Action
 *  net.minecraft.ServerboundUseItemOnPacket
 *  net.minecraft.CartographyTableBlock
 *  net.minecraft.GrindstoneBlock
 *  net.minecraft.StonecutterBlock
 *  net.minecraft.BlockHitResult
 *  net.minecraft.BlockCollisions
 *  net.minecraft.ClientLevel
 *  net.minecraft.LocalPlayer
 *  org.jetbrains.annotations.Nullable
 */
package dev.aevum.api.utils.world;

import dev.aevum.Aevum;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.world.BlockPosX;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.AntiCheat;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import dev.aevum.mod.modules.impl.combat.AutoCrystal;
import dev.aevum.mod.modules.impl.combat.AutoWeb;
import dev.aevum.mod.modules.impl.player.AirPlace;
import dev.aevum.mod.modules.settings.enums.SwingSide;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.InteractionHand;
import net.minecraft.Entity;
import net.minecraft.ExperienceOrb;
import net.minecraft.EndCrystal;
import net.minecraft.ArmorStand;
import net.minecraft.ItemEntity;
import net.minecraft.Arrow;
import net.minecraft.ThrownExperienceBottle;
import net.minecraft.Items;
import net.minecraft.BlockGetter;
import net.minecraft.ChunkPos;
import net.minecraft.Level;
import net.minecraft.CollisionGetter;
import net.minecraft.AnvilBlock;
import net.minecraft.BasePressurePlateBlock;
import net.minecraft.BaseEntityBlock;
import net.minecraft.BedBlock;
import net.minecraft.Blocks;
import net.minecraft.Block;
import net.minecraft.ButtonBlock;
import net.minecraft.CraftingTableBlock;
import net.minecraft.DoorBlock;
import net.minecraft.BlockPos;
import net.minecraft.FenceGateBlock;
import net.minecraft.Direction;
import net.minecraft.AABB;
import net.minecraft.LoomBlock;
import net.minecraft.NoteBlock;
import net.minecraft.Vec3;
import net.minecraft.TrapDoorBlock;
import net.minecraft.BlockEntity;
import net.minecraft.Packet;
import net.minecraft.VoxelShape;
import net.minecraft.BlockState;
import net.minecraft.LevelChunk;
import net.minecraft.ServerboundPlayerActionPacket;
import net.minecraft.ServerboundUseItemOnPacket;
import net.minecraft.CartographyTableBlock;
import net.minecraft.GrindstoneBlock;
import net.minecraft.StonecutterBlock;
import net.minecraft.BlockHitResult;
import net.minecraft.BlockCollisions;
import net.minecraft.ClientLevel;
import net.minecraft.LocalPlayer;
import org.jetbrains.annotations.Nullable;

public class BlockUtil
implements Wrapper {
    public static final List<BlockPos> placedPos = new ArrayList<BlockPos>();
    private static final double MIN_EYE_HEIGHT = 0.4;
    private static final double MAX_EYE_HEIGHT = 1.62;
    private static final double MOVEMENT_THRESHOLD = 2.0E-4;

    public static boolean canPlace(BlockPos pos) {
        return BlockUtil.canPlace(pos, 1000.0);
    }

    public static boolean canPlace(BlockPos pos, double getDistance) {
        if (BlockUtil.getPlaceSide(pos, getDistance) == null) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        return !BlockUtil.hasEntity(pos, false);
    }

    public static boolean canPlace(BlockPos pos, double getDistance, boolean ignoreCrystal) {
        if (BlockUtil.getPlaceSide(pos, getDistance) == null) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        return !BlockUtil.hasEntity(pos, ignoreCrystal);
    }

    public static boolean clientCanPlace(BlockPos pos, boolean ignoreCrystal) {
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        return !BlockUtil.hasEntity(pos, ignoreCrystal);
    }

    public static List<Entity> getEntities(AABB box) {
        ArrayList<Entity> list = new ArrayList<Entity>();
        for (Entity entity : Aevum.THREAD.getEntities()) {
            if (entity == null || entity instanceof ArmorStand && AntiCheat.INSTANCE.ignoreArmorStand.getValue() || !entity.method_5829().method_994(box)) continue;
            list.add(entity);
        }
        return list;
    }

    public static List<EndCrystal> getEndCrystals(AABB box) {
        ArrayList<EndCrystal> list = new ArrayList<EndCrystal>();
        for (Entity entity : Aevum.THREAD.getEntities()) {
            EndCrystal crystal;
            if (!(entity instanceof EndCrystal) || !(crystal = (EndCrystal)entity).method_5829().method_994(box)) continue;
            list.add(crystal);
        }
        return list;
    }

    public static boolean hasEntity(BlockPos pos, boolean ignoreCrystal) {
        return BlockUtil.hasEntity(new AABB(pos), ignoreCrystal);
    }

    public static boolean hasEntity(AABB box, boolean ignoreCrystal) {
        for (Entity entity : BlockUtil.getEntities(box)) {
            if (!entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrb || entity instanceof ThrownExperienceBottle || entity instanceof Arrow || ignoreCrystal && entity instanceof EndCrystal && BlockUtil.mc.player.getEyePosition().method_1022(MathUtil.getClosestPoint(entity)) <= AntiCheat.INSTANCE.ieRange.getValue()) continue;
            return true;
        }
        return false;
    }

    public static boolean hasCrystal(BlockPos pos) {
        for (Entity class_12972 : BlockUtil.getEndCrystals(new AABB(pos))) {
            if (!class_12972.isAlive() || !(class_12972 instanceof EndCrystal)) continue;
            return true;
        }
        return false;
    }

    public static boolean noEntityBlockCrystal(BlockPos pos, boolean ignoreCrystal) {
        return BlockUtil.noEntityBlockCrystal(pos, ignoreCrystal, false);
    }

    public static boolean noEntityBlockCrystal(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
        for (Entity entity : BlockUtil.getEntities(new AABB(pos))) {
            if (!entity.isAlive() || ignoreItem && entity instanceof ItemEntity || ignoreCrystal && entity instanceof EndCrystal && BlockUtil.mc.player.getEyePosition().method_1022(MathUtil.getClosestPoint(entity)) <= AntiCheat.INSTANCE.ieRange.getValue()) continue;
            return false;
        }
        return true;
    }

    public static boolean canPlaceCrystal(BlockPos pos) {
        BlockPos obsPos = pos.method_10074();
        BlockPos boost = obsPos.method_10084();
        return !(BlockUtil.getBlock(obsPos) != Blocks.BEDROCK && BlockUtil.getBlock(obsPos) != Blocks.OBSIDIAN || BlockUtil.getClickSideStrict(obsPos) == null || !BlockUtil.mc.level.isEmptyBlock(boost) || !BlockUtil.noEntityBlockCrystal(boost, false) || !BlockUtil.noEntityBlockCrystal(boost.method_10084(), false) || ClientSetting.INSTANCE.lowVersion.getValue() && !BlockUtil.mc.level.isEmptyBlock(boost.method_10084()));
    }

    public static void placeCrystal(BlockPos pos, boolean rotate) {
        boolean offhand = BlockUtil.mc.player.getOffhandItem().method_7909() == Items.END_CRYSTAL;
        BlockPos obsPos = pos.method_10074();
        Direction facing = BlockUtil.getClickSide(obsPos);
        Vec3 vec = obsPos.method_46558().method_1031((double)facing.getNormal().method_10263() * 0.5, (double)facing.getNormal().method_10264() * 0.5, (double)facing.getNormal().getZ() * 0.5);
        if (rotate) {
            Aevum.ROTATION.lookAt(vec);
        }
        BlockUtil.clickBlock(obsPos, facing, false, offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
    }

    public static void placeBlock(BlockPos pos, boolean rotate) {
        BlockUtil.placeBlock(pos, rotate, AntiCheat.INSTANCE.packetPlace.getValue());
    }

    public static void placeBlock(BlockPos pos, boolean rotate, boolean packet) {
        if (BlockUtil.allowAirPlace()) {
            placedPos.add(pos);
            BlockUtil.airPlace(pos, rotate, InteractionHand.MAIN_HAND, packet);
            return;
        }
        Direction side = BlockUtil.getPlaceSide(pos);
        if (side == null) {
            return;
        }
        placedPos.add(pos);
        BlockUtil.clickBlock(pos.method_10093(side), side.getOpposite(), rotate, InteractionHand.MAIN_HAND, packet);
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate) {
        BlockUtil.clickBlock(pos, side, rotate, InteractionHand.MAIN_HAND);
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate, InteractionHand hand) {
        BlockUtil.clickBlock(pos, side, rotate, hand, AntiCheat.INSTANCE.packetPlace.getValue());
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate, boolean packet) {
        BlockUtil.clickBlock(pos, side, rotate, InteractionHand.MAIN_HAND, packet);
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate, InteractionHand hand, boolean packet) {
        Vec3 directionVec = new Vec3((double)pos.method_10263() + 0.5 + (double)side.getNormal().method_10263() * 0.5, (double)pos.method_10264() + 0.5 + (double)side.getNormal().method_10264() * 0.5, (double)pos.getZ() + 0.5 + (double)side.getNormal().getZ() * 0.5);
        if (rotate) {
            Aevum.ROTATION.lookAt(directionVec);
        }
        EntityUtil.swingHand(hand, AntiCheat.INSTANCE.interactSwing.getValue());
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        if (packet) {
            Module.sendSequencedPacket(id -> new ServerboundUseItemOnPacket(hand, result, id));
        } else {
            BlockUtil.mc.gameMode.method_2896(BlockUtil.mc.player, hand, result);
        }
        BlockUtil.mc.rightClickDelay = 4;
        if (rotate) {
            Aevum.ROTATION.snapBack();
        }
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate, InteractionHand hand, SwingSide swingSide) {
        Vec3 directionVec = new Vec3((double)pos.method_10263() + 0.5 + (double)side.getNormal().method_10263() * 0.5, (double)pos.method_10264() + 0.5 + (double)side.getNormal().method_10264() * 0.5, (double)pos.getZ() + 0.5 + (double)side.getNormal().getZ() * 0.5);
        if (rotate) {
            Aevum.ROTATION.lookAt(directionVec);
        }
        EntityUtil.swingHand(hand, swingSide);
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        Module.sendSequencedPacket(id -> new ServerboundUseItemOnPacket(hand, result, id));
        BlockUtil.mc.rightClickDelay = 4;
        if (rotate) {
            Aevum.ROTATION.snapBack();
        }
    }

    public static void airPlace(BlockPos pos, boolean rotate) {
        BlockUtil.airPlace(pos, rotate, InteractionHand.MAIN_HAND, AntiCheat.INSTANCE.packetPlace.getValue());
    }

    public static void airPlace(BlockPos pos, boolean rotate, InteractionHand hand, boolean packet) {
        boolean bypass = hand == InteractionHand.MAIN_HAND && AirPlace.INSTANCE.grimBypass.getValue();
        boolean bl = bypass;
        if (bypass) {
            mc.getConnection().method_52787((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
            hand = InteractionHand.OFF_HAND;
        }
        Direction side = BlockUtil.getClickSide(pos);
        Vec3 directionVec = new Vec3((double)pos.method_10263() + 0.5 + (double)side.getNormal().method_10263() * 0.5, (double)pos.method_10264() + 0.5 + (double)side.getNormal().method_10264() * 0.5, (double)pos.getZ() + 0.5 + (double)side.getNormal().getZ() * 0.5);
        if (rotate) {
            Aevum.ROTATION.lookAt(directionVec);
        }
        EntityUtil.swingHand(hand, AntiCheat.INSTANCE.interactSwing.getValue());
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        if (packet) {
            InteractionHand finalHand = hand;
            Module.sendSequencedPacket(id -> new ServerboundUseItemOnPacket(finalHand, result, id));
        } else {
            BlockUtil.mc.gameMode.method_2896(BlockUtil.mc.player, hand, result);
        }
        BlockUtil.mc.rightClickDelay = 4;
        if (rotate) {
            Aevum.ROTATION.snapBack();
        }
        if (bypass) {
            mc.getConnection().method_52787((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
        }
    }

    public static double distanceToXZ(double x, double z, double x2, double z2) {
        double dx = x2 - x;
        double dz = z2 - z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static double distanceToXZ(double x, double z) {
        return BlockUtil.distanceToXZ(x, z, BlockUtil.mc.player.getX(), BlockUtil.mc.player.getZ());
    }

    public static Direction getPlaceSide(BlockPos pos) {
        if (BlockUtil.allowAirPlace()) {
            return BlockUtil.getClickSide(pos);
        }
        double minDistance = Double.MAX_VALUE;
        Direction side = null;
        for (Direction i : Direction.values()) {
            double d;
            if (!BlockUtil.canClick(pos.method_10093(i)) || BlockUtil.canReplace(pos.method_10093(i)) || !BlockUtil.isStrictDirection(pos.method_10093(i), i.getOpposite())) continue;
            double vecDis = BlockUtil.mc.player.getEyePosition().method_1025(pos.method_46558().method_1031((double)i.getNormal().method_10263() * 0.5, (double)i.getNormal().method_10264() * 0.5, (double)i.getNormal().getZ() * 0.5));
            if (d > minDistance) continue;
            side = i;
            minDistance = vecDis;
        }
        return side;
    }

    public static Direction getBestNeighboring(BlockPos pos, Direction facing) {
        Direction bestFacing = null;
        double getDistance = 0.0;
        for (Direction i : Direction.values()) {
            if (facing != null && pos.method_10093(i).equals((Object)pos.method_10079(facing, -1)) || i == Direction.DOWN || BlockUtil.getPlaceSide(pos) == null || bestFacing != null && !(BlockUtil.mc.player.getEyePosition().method_1025(pos.method_10093(i).method_46558()) < getDistance)) continue;
            bestFacing = i;
            getDistance = BlockUtil.mc.player.getEyePosition().method_1025(pos.method_10093(i).method_46558());
        }
        return bestFacing;
    }

    public static Direction getPlaceSide(BlockPos pos, double reachDistance) {
        if (BlockUtil.allowAirPlace()) {
            Direction i = BlockUtil.getClickSide(pos);
            double vecDis = BlockUtil.mc.player.getEyePosition().method_1025(pos.method_46558().method_1031((double)i.getNormal().method_10263() * 0.5, (double)i.getNormal().method_10264() * 0.5, (double)i.getNormal().getZ() * 0.5));
            if (Math.sqrt(vecDis) > reachDistance) {
                return null;
            }
            return Direction.DOWN;
        }
        double minDistance = Double.MAX_VALUE;
        Direction side = null;
        for (Direction i : Direction.values()) {
            double vecDis;
            if (!BlockUtil.canClick(pos.method_10093(i)) || BlockUtil.canReplace(pos.method_10093(i)) || !BlockUtil.isStrictDirection(pos.method_10093(i), i.getOpposite()) || Math.sqrt(vecDis = BlockUtil.mc.player.getEyePosition().method_1025(pos.method_46558().method_1031((double)i.getNormal().method_10263() * 0.5, (double)i.getNormal().method_10264() * 0.5, (double)i.getNormal().getZ() * 0.5))) > reachDistance || vecDis > minDistance) continue;
            side = i;
            minDistance = vecDis;
        }
        return side;
    }

    public static Direction getClickSide(BlockPos pos) {
        Direction side = Direction.UP;
        double minDistance = Double.MAX_VALUE;
        for (Direction i : Direction.values()) {
            double d;
            if (!BlockUtil.isStrictDirection(pos, i)) continue;
            double disSq = BlockUtil.mc.player.getEyePosition().method_1025(pos.method_10093(i).method_46558());
            if (d > minDistance) continue;
            side = i;
            minDistance = disSq;
        }
        return side;
    }

    public static Direction getClickSideStrict(BlockPos pos) {
        Direction side = null;
        double minDistance = Double.MAX_VALUE;
        for (Direction i : Direction.values()) {
            double d;
            if (!BlockUtil.isStrictDirection(pos, i)) continue;
            double disSq = BlockUtil.mc.player.getEyePosition().method_1025(pos.method_10093(i).method_46558());
            if (d > minDistance) continue;
            side = i;
            minDistance = disSq;
        }
        return side;
    }

    public static boolean isStrictDirection(BlockPos pos, Direction side, double reachDistance) {
        double vecDis = BlockUtil.mc.player.getEyePosition().method_1025(pos.method_46558().method_1031((double)side.getNormal().method_10263() * 0.5, (double)side.getNormal().method_10264() * 0.5, (double)side.getNormal().getZ() * 0.5));
        if (Math.sqrt(vecDis) > reachDistance) {
            return false;
        }
        return BlockUtil.isStrictDirection(pos, side);
    }

    public static boolean isStrictDirection(BlockPos pos, Direction side) {
        switch (AntiCheat.INSTANCE.placement.getValue()) {
            case Vanilla: {
                return true;
            }
            case Legit: {
                return EntityUtil.canSee(pos, side);
            }
            case Grim: {
                return BlockUtil.grimStrictDirectionCheck(pos, side, BlockUtil.mc.level, BlockUtil.mc.player);
            }
            case NCP: {
                if (BlockUtil.mc.level.method_8320(pos.method_10093(side)).method_26234((BlockGetter)BlockUtil.mc.level, pos.method_10093(side))) {
                    return false;
                }
                Vec3 eyePos = BlockUtil.mc.player.getEyePosition();
                Vec3 blockCenter = pos.method_46558();
                ArrayList<Direction> validAxis = new ArrayList<Direction>();
                validAxis.addAll(BlockUtil.checkAxis(eyePos.x - blockCenter.x, Direction.WEST, Direction.EAST, false));
                validAxis.addAll(BlockUtil.checkAxis(eyePos.y - blockCenter.y, Direction.DOWN, Direction.UP, true));
                validAxis.addAll(BlockUtil.checkAxis(eyePos.z - blockCenter.z, Direction.NORTH, Direction.SOUTH, false));
                return validAxis.contains(side);
            }
        }
        return true;
    }

    public static boolean grimStrictDirectionCheck(BlockPos pos, Direction direction, ClientLevel level, LocalPlayer player) {
        boolean bl;
        block15: {
            block14: {
                AABB combined = BlockUtil.getCombinedBox(pos, (Level)level);
                AABB eyePositions = new AABB(player.getX(), player.getY() + 0.4, player.getZ(), player.getX(), player.getY() + 1.62, player.getZ()).method_1014(2.0E-4);
                if (BlockUtil.isIntersected(eyePositions, combined)) {
                    return true;
                }
                switch (direction) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case NORTH: {
                        if (!(eyePositions.minZ > combined.minZ)) {
                            break;
                        }
                        break block14;
                    }
                    case SOUTH: {
                        if (!(eyePositions.maxZ < combined.maxZ)) {
                            break;
                        }
                        break block14;
                    }
                    case EAST: {
                        if (!(eyePositions.maxX < combined.maxX)) {
                            break;
                        }
                        break block14;
                    }
                    case WEST: {
                        if (!(eyePositions.minX > combined.minX)) {
                            break;
                        }
                        break block14;
                    }
                    case UP: {
                        if (!(eyePositions.maxY < combined.maxY)) {
                            break;
                        }
                        break block14;
                    }
                    case DOWN: {
                        if (eyePositions.minY > combined.minY) break block14;
                    }
                }
                bl = true;
                break block15;
            }
            bl = false;
        }
        return bl;
    }

    private static AABB getCombinedBox(BlockPos pos, Level level) {
        VoxelShape shape = level.method_8320(pos).method_26220((BlockGetter)level, pos).method_1096((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.getZ());
        AABB combined = new AABB(pos);
        for (AABB box : shape.toAabbs()) {
            double minX = Math.max(box.minX, combined.minX);
            double minY = Math.max(box.minY, combined.minY);
            double minZ = Math.max(box.minZ, combined.minZ);
            double maxX = Math.min(box.maxX, combined.maxX);
            double maxY = Math.min(box.maxY, combined.maxY);
            double maxZ = Math.min(box.maxZ, combined.maxZ);
            combined = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        }
        return combined;
    }

    private static boolean isIntersected(AABB bb, AABB other) {
        return other.maxX - 1.0E-7 > bb.minX && other.minX + 1.0E-7 < bb.maxX && other.maxY - 1.0E-7 > bb.minY && other.minY + 1.0E-7 < bb.maxY && other.maxZ - 1.0E-7 > bb.minZ && other.minZ + 1.0E-7 < bb.maxZ;
    }

    public static ArrayList<Direction> checkAxis(double diff, Direction negativeSide, Direction positiveSide, boolean vertical) {
        ArrayList<Direction> valid = new ArrayList<Direction>();
        if (vertical) {
            if (diff < -0.5) {
                valid.add(negativeSide);
            }
            if (AntiCheat.INSTANCE.upDirectionLimit.getValue()) {
                if (diff > 0.5) {
                    valid.add(positiveSide);
                }
            } else if (diff > -0.5) {
                valid.add(positiveSide);
            }
        } else {
            if (diff < -0.5) {
                valid.add(negativeSide);
            }
            if (diff > 0.5) {
                valid.add(positiveSide);
            }
        }
        return valid;
    }

    public static ArrayList<BlockEntity> getTileEntities() {
        return BlockUtil.getLoadedChunks().flatMap(chunk -> chunk.getBlockEntities().values().stream()).collect(Collectors.toCollection(ArrayList::new));
    }

    public static Stream<LevelChunk> getLoadedChunks() {
        int radius = Math.max(2, BlockUtil.mc.options.getEffectiveRenderDistance()) + 3;
        int diameter = radius * 2 + 1;
        ChunkPos center = BlockUtil.mc.player.chunkPosition();
        ChunkPos min = new ChunkPos(center.x - radius, center.z - radius);
        ChunkPos max = new ChunkPos(center.x + radius, center.z + radius);
        return Stream.iterate(min, pos -> {
            int x = pos.x;
            int z = pos.z;
            if (++x > max.x) {
                x = min.x;
                ++z;
            }
            return new ChunkPos(x, z);
        }).limit((long)diameter * (long)diameter).filter(c -> BlockUtil.mc.level.method_8393(c.x, c.z)).map(c -> BlockUtil.mc.level.method_8497(c.x, c.z)).filter(Objects::nonNull);
    }

    public static ArrayList<BlockPos> getSphere(float range) {
        return BlockUtil.getSphere(range, BlockUtil.mc.player.getEyePosition());
    }

    public static BlockPos getBlock(Block block, float range) {
        for (BlockPos pos : BlockUtil.getSphere(range)) {
            if (BlockUtil.mc.level.method_8320(pos).method_26204() != block) continue;
            return pos;
        }
        return null;
    }

    public static BlockPos getBlock(Class<?> block, float range) {
        for (BlockPos pos : BlockUtil.getSphere(range)) {
            if (!block.isInstance(BlockUtil.mc.level.method_8320(pos).method_26204())) continue;
            return pos;
        }
        return null;
    }

    public static ArrayList<BlockPos> getSphere(float range, Vec3 pos) {
        ArrayList<BlockPos> list = new ArrayList<BlockPos>();
        for (double y = pos.method_10214() + (double)range; y > pos.method_10214() - (double)range; y -= 1.0) {
            if (y < -64.0) continue;
            for (double x = pos.method_10216() - (double)range; x < pos.method_10216() + (double)range; x += 1.0) {
                for (double z = pos.method_10215() - (double)range; z < pos.method_10215() + (double)range; z += 1.0) {
                    BlockPosX curPos = new BlockPosX(x, y, z);
                    if (curPos.method_46558().method_1022(pos) > (double)range) continue;
                    list.add(curPos);
                }
            }
        }
        return list;
    }

    public static Block getBlock(BlockPos pos) {
        return BlockUtil.mc.level.method_8320(pos).method_26204();
    }

    public static boolean canReplace(BlockPos pos) {
        if (pos.method_10264() >= 320) {
            return false;
        }
        if (AntiCheat.INSTANCE.multiPlace.getValue() && placedPos.contains(pos)) {
            return false;
        }
        BlockState state = BlockUtil.mc.level.method_8320(pos);
        if (state.method_26204() == Blocks.COBWEB && AutoWeb.ignore && AutoCrystal.INSTANCE.replace.getValue()) {
            return true;
        }
        return state.method_45474();
    }

    public static boolean canClick(BlockPos pos) {
        if (AntiCheat.INSTANCE.multiPlace.getValue() && placedPos.contains(pos)) {
            return true;
        }
        BlockState state = BlockUtil.mc.level.method_8320(pos);
        Block block = state.method_26204();
        if (block == Blocks.COBWEB && AutoWeb.ignore) {
            return AutoCrystal.INSTANCE.airPlace.getValue();
        }
        return BlockUtil.mc.player.isShiftKeyDown() || !BlockUtil.isClickable(block);
    }

    public static boolean isClickable(Block block) {
        return block instanceof CraftingTableBlock || block instanceof AnvilBlock || block instanceof LoomBlock || block instanceof CartographyTableBlock || block instanceof GrindstoneBlock || block instanceof StonecutterBlock || block instanceof ButtonBlock || block instanceof BasePressurePlateBlock || block instanceof BaseEntityBlock || block instanceof BedBlock || block instanceof FenceGateBlock || block instanceof DoorBlock || block instanceof NoteBlock || block instanceof TrapDoorBlock;
    }

    public static boolean canCollide(AABB box) {
        return BlockUtil.canCollide((Entity)BlockUtil.mc.player, box);
    }

    public static boolean canCollide(@Nullable Entity entity, AABB box) {
        BlockCollisions blockCollisionSpliterator = new BlockCollisions((CollisionGetter)BlockUtil.mc.level, entity, box, false, (pos, voxelShape) -> voxelShape);
        do {
            if (blockCollisionSpliterator.hasNext()) continue;
            return false;
        } while (((VoxelShape)blockCollisionSpliterator.next()).method_1110());
        return true;
    }

    public static boolean allowAirPlace() {
        return AirPlace.INSTANCE.isOn() && AirPlace.INSTANCE.module.getValue();
    }
}

