/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.Entity
 *  net.minecraft.ExperienceOrb
 *  net.minecraft.ItemFrame
 *  net.minecraft.ItemEntity
 *  net.minecraft.Arrow
 *  net.minecraft.ThrownExperienceBottle
 *  net.minecraft.Items
 *  net.minecraft.Blocks
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.AABB
 *  net.minecraft.Vec3
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundInteractPacket
 *  net.minecraft.ScaffoldingBlock
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.combat.CombatUtil;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.api.utils.world.BlockPosX;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.AntiCheat;
import dev.aevum.mod.modules.impl.exploit.Blink;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.InteractionHand;
import net.minecraft.Entity;
import net.minecraft.ExperienceOrb;
import net.minecraft.ItemFrame;
import net.minecraft.ItemEntity;
import net.minecraft.Arrow;
import net.minecraft.ThrownExperienceBottle;
import net.minecraft.Items;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.AABB;
import net.minecraft.Vec3;
import net.minecraft.Packet;
import net.minecraft.ServerboundInteractPacket;
import net.minecraft.ScaffoldingBlock;

public class AntiPhase
extends Module {
    private final SliderSetting placeRange = this.add(new SliderSetting("PlaceRange", 4, 0, 8));
    private final BooleanSetting ladder = this.add(new BooleanSetting("Ladder", true).setParent());
    private final BooleanSetting onlyHard = this.add(new BooleanSetting("OnlyHard", true, this.ladder::isOpen));
    private final BooleanSetting itemFrame = this.add(new BooleanSetting("ItemFrame", true).setParent());
    private final BooleanSetting fill = this.add(new BooleanSetting("Fill", false, this.itemFrame::isOpen));
    private final BooleanSetting scaffolding = this.add(new BooleanSetting("Scaffolding", true));
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
    private final BooleanSetting eatingPause = this.add(new BooleanSetting("EatingPause", true));
    private final BooleanSetting collideSkip = this.add(new BooleanSetting("CollideSkip", true));
    private final BooleanSetting crawlingSkip = this.add(new BooleanSetting("CrawlingSkip", true));
    private final BooleanSetting onlyGround = this.add(new BooleanSetting("InAirSkip", false));
    private final SliderSetting targetRange = this.add(new SliderSetting("TargetRange", 5.0, 0.0, 7.0, 0.1));
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 100.0, 0.0, 2000.0, 1.0));
    private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
    private final Timer timer = new Timer();

    public AntiPhase() {
        super("AntiPhase", Module.Category.Combat);
        this.setChinese("\u53cd\u7a7f\u5899");
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.eatingPause.getValue() && AntiPhase.mc.player.isUsingItem()) {
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        for (Entity class_12972 : CombatUtil.getEnemies(this.targetRange.getValue())) {
            Direction facing;
            BlockPos bp;
            int block;
            if (this.crawlingSkip.getValue() && class_12972.isVisuallyCrawling() || this.onlyGround.getValue() && !class_12972.onGround() || this.collideSkip.getValue() && BlockUtil.canCollide(class_12972, class_12972.method_5829()) || !this.timer.passed(this.delay.getValueInt())) continue;
            if (this.scaffolding.getValue() && BlockUtil.canReplace(class_12972.method_24515()) && (block = this.getScaffolding()) != -1) {
                bp = class_12972.method_24515();
                Direction downSide = null;
                Direction placeSide = BlockUtil.getPlaceSide(bp, 6.0);
                if (placeSide != null || (downSide = this.getSideIgnore(bp.method_10074())) != null && BlockUtil.getBlock(bp.method_10074()) instanceof ScaffoldingBlock && !AntiPhase.mc.player.isShiftKeyDown() || AntiPhase.mc.player.isShiftKeyDown() && (downSide = this.getSideOnly(bp.method_10074())) != null && BlockUtil.getBlock(bp.method_10074()) instanceof ScaffoldingBlock) {
                    Vec3 targetPos = placeSide != null ? bp.method_10093(placeSide).method_46558().method_1031((double)placeSide.getOpposite().getNormal().method_10263() * 0.5, (double)placeSide.getOpposite().getNormal().method_10264() * 0.5, (double)placeSide.getOpposite().getNormal().getZ() * 0.5) : bp.method_10074().method_46558().method_1031((double)downSide.getNormal().method_10263() * 0.5, (double)downSide.getNormal().method_10264() * 0.5, (double)downSide.getNormal().getZ() * 0.5);
                    double getDistance = AntiPhase.mc.player.getEyePosition().method_1022(targetPos);
                    if (getDistance <= this.placeRange.getValue()) {
                        int old = AntiPhase.mc.player.getInventory().selected;
                        this.doSwap(block);
                        if (BlockUtil.getBlock(bp.method_10074()) instanceof ScaffoldingBlock && downSide != null) {
                            BlockUtil.clickBlock(bp.method_10074(), downSide, this.rotate.getValue());
                        } else {
                            BlockUtil.placeBlock(bp, this.rotate.getValue());
                        }
                        this.timer.reset();
                        if (this.inventory.getValue()) {
                            this.doSwap(block);
                            EntityUtil.syncInventory();
                        } else {
                            this.doSwap(old);
                        }
                    }
                }
            }
            if (this.itemFrame.getValue() && AntiPhase.mc.level.isEmptyBlock(class_12972.method_24515())) {
                int block2;
                ItemFrame itemFrameEntity = this.hasItemFrame(new AABB(class_12972.method_24515()));
                if (itemFrameEntity == null && (block2 = this.getItemFrame()) != -1) {
                    BlockPos bp2 = class_12972.method_24515().method_10074();
                    double getDistance = AntiPhase.mc.player.getEyePosition().method_1022(bp2.method_61082().method_1031(0.0, 1.0, 0.0));
                    if (getDistance <= this.placeRange.getValue() && BlockUtil.isStrictDirection(bp2, Direction.UP) && !BlockUtil.canReplace(bp2) && BlockUtil.canClick(bp2)) {
                        int old = AntiPhase.mc.player.getInventory().selected;
                        this.doSwap(block2);
                        BlockUtil.clickBlock(bp2, Direction.UP, this.rotate.getValue());
                        this.timer.reset();
                        if (this.inventory.getValue()) {
                            this.doSwap(block2);
                            EntityUtil.syncInventory();
                        } else {
                            this.doSwap(old);
                        }
                    }
                }
                if (this.fill.getValue() && itemFrameEntity != null && itemFrameEntity.getItem().method_7960()) {
                    int block3;
                    Vec3 hitVec = MathUtil.getClosestPointToBox(AntiPhase.mc.player.getEyePosition(), itemFrameEntity.method_5829());
                    if (AntiPhase.mc.player.getEyePosition().method_1022(hitVec) <= AntiCheat.INSTANCE.ieRange.getValue() && (block3 = this.getObsidian()) != -1) {
                        int old = AntiPhase.mc.player.getInventory().selected;
                        this.doSwap(block3);
                        if (this.rotate.getValue()) {
                            Aevum.ROTATION.snapAt(hitVec);
                        }
                        AntiPhase.mc.player.connection.method_52787((Packet)ServerboundInteractPacket.method_34207((Entity)itemFrameEntity, (boolean)AntiPhase.mc.player.isShiftKeyDown(), (InteractionHand)InteractionHand.MAIN_HAND));
                        this.timer.reset();
                        if (this.inventory.getValue()) {
                            this.doSwap(block3);
                            EntityUtil.syncInventory();
                        } else {
                            this.doSwap(old);
                        }
                        if (this.rotate.getValue()) {
                            Aevum.ROTATION.snapBack();
                        }
                    }
                }
            }
            if (!this.ladder.getValue() || (block = this.getLadder()) == -1 || !BlockUtil.canReplace(class_12972.method_24515()) || (facing = this.targetFacing(class_12972.position())) == null) continue;
            bp = class_12972.method_24515().method_10093(facing);
            double getDistance = AntiPhase.mc.player.getEyePosition().method_1022(bp.method_46558().method_1031((double)facing.getOpposite().getNormal().method_10263() * 0.5, (double)facing.getOpposite().getNormal().method_10264() * 0.5, (double)facing.getOpposite().getNormal().getZ() * 0.5));
            if (!(getDistance <= this.placeRange.getValue())) continue;
            BlockUtil.placedPos.add(class_12972.method_24515());
            int old = AntiPhase.mc.player.getInventory().selected;
            this.doSwap(block);
            BlockUtil.clickBlock(bp, facing.getOpposite(), this.rotate.getValue());
            this.timer.reset();
            if (this.inventory.getValue()) {
                this.doSwap(block);
                EntityUtil.syncInventory();
                continue;
            }
            this.doSwap(old);
        }
    }

    private Direction getSideOnly(BlockPos pos) {
        if (BlockUtil.isStrictDirection(pos, Direction.UP)) {
            return Direction.UP;
        }
        return null;
    }

    private Direction getSideIgnore(BlockPos pos) {
        for (Direction i : Direction.values()) {
            if (i == Direction.UP || !BlockUtil.isStrictDirection(pos, i)) continue;
            return i;
        }
        return null;
    }

    private ItemFrame hasItemFrame(AABB box) {
        for (Entity entity : BlockUtil.getEntities(box)) {
            if (!(entity instanceof ItemFrame)) continue;
            ItemFrame itemFrameEntity = (ItemFrame)entity;
            if (entity.getNearestViewDirection() != Direction.UP) continue;
            return itemFrameEntity;
        }
        return null;
    }

    private static AABB getBox(Direction facing, BlockPos bp) {
        AABB box = null;
        double wide = 0.1875;
        double x = (double)facing.getStepX() * 0.5 + (double)bp.method_10263() + 0.5;
        double y = bp.method_10264();
        double z = (double)facing.getStepZ() * 0.5 + (double)bp.getZ() + 0.5;
        switch (facing) {
            case WEST: {
                box = new AABB(x, y, z, x + wide, y + 1.0, z + 1.0);
                break;
            }
            case EAST: {
                box = new AABB(x, y, z, x - wide, y + 1.0, z + 1.0);
                break;
            }
            case NORTH: {
                box = new AABB(x, y, z, x + 1.0, y + 1.0, z + wide);
                break;
            }
            case SOUTH: {
                box = new AABB(x, y, z, x + 1.0, y + 1.0, z - wide);
            }
        }
        return box;
    }

    private void doSwap(int slot) {
        if (this.inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, AntiPhase.mc.player.getInventory().selected);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private int getFlintAndSteel() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findItemInventorySlot(Items.FLINT_AND_STEEL);
        }
        return InventoryUtil.findItem(Items.FLINT_AND_STEEL);
    }

    private int getObsidian() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findItemInventorySlot(Items.OBSIDIAN);
        }
        return InventoryUtil.findItem(Items.OBSIDIAN);
    }

    private int getItemFrame() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findItemInventorySlot(Items.ITEM_FRAME);
        }
        return InventoryUtil.findItem(Items.ITEM_FRAME);
    }

    private int getLadder() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.LADDER);
        }
        return InventoryUtil.findBlock(Blocks.LADDER);
    }

    private int getScaffolding() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.SCAFFOLDING);
        }
        return InventoryUtil.findBlock(Blocks.SCAFFOLDING);
    }

    private Direction targetFacing(Vec3 vec3d) {
        BlockPosX blockPos = new BlockPosX(vec3d);
        Vec3 centerPos = blockPos.method_61082();
        float factorValue = 0.4f;
        double minDistance = Double.MAX_VALUE;
        Direction facing = null;
        for (Direction direction : Direction.values()) {
            Vec3 tempPos;
            double getDistance;
            BlockPos bp;
            if (direction == Direction.UP || direction == Direction.DOWN || !BlockUtil.isStrictDirection(bp = blockPos.method_10093(direction), direction.getOpposite()) || (this.onlyHard.getValue() ? !Aevum.HOLE.isHard(bp) : BlockUtil.canReplace(bp) || !BlockUtil.canClick(bp))) continue;
            AABB box = AntiPhase.getBox(direction, blockPos);
            if (box == null || AntiPhase.hasEntity(box) || !((getDistance = (tempPos = centerPos.method_1031((double)((float)direction.getStepX() * factorValue), 0.0, (double)((float)direction.getStepZ() * factorValue))).method_1022(vec3d)) < minDistance)) continue;
            minDistance = getDistance;
            facing = direction;
        }
        return facing;
    }

    public static boolean hasEntity(AABB box) {
        for (Entity entity : BlockUtil.getEntities(box)) {
            if (!entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrb || entity instanceof ThrownExperienceBottle || entity instanceof Arrow || entity instanceof ItemFrame) continue;
            return true;
        }
        return false;
    }
}

