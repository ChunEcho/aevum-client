/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Player
 *  net.minecraft.ElytraItem
 *  net.minecraft.ItemStack
 *  net.minecraft.Blocks
 *  net.minecraft.ConcretePowderBlock
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.AABB
 *  net.minecraft.Vec3i
 *  net.minecraft.Vec3
 *  net.minecraft.Mth
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.combat.CombatUtil;
import dev.aevum.api.utils.math.PredictUtil;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.api.utils.world.BlockPosX;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.exploit.Blink;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.util.ArrayList;
import net.minecraft.Entity;
import net.minecraft.Player;
import net.minecraft.ElytraItem;
import net.minecraft.ItemStack;
import net.minecraft.Blocks;
import net.minecraft.ConcretePowderBlock;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.AABB;
import net.minecraft.Vec3i;
import net.minecraft.Vec3;
import net.minecraft.Mth;

public class AutoTrap
extends Module {
    public static AutoTrap INSTANCE;
    public final SliderSetting delay = this.add(new SliderSetting("Delay", 100, 0, 500).setSuffix("ms"));
    private final EnumSetting<TargetMode> targetMod = this.add(new EnumSetting<TargetMode>("TargetMode", TargetMode.Single));
    private final EnumSetting<Mode> headMode = this.add(new EnumSetting<Mode>("BlockForHead", Mode.Anchor));
    final ArrayList<BlockPos> trapList = new ArrayList();
    final ArrayList<BlockPos> placeList = new ArrayList();
    private final Timer timer = new Timer();
    private final SliderSetting placeRange = this.add(new SliderSetting("PlaceRange", 4.0, 1.0, 6.0).setSuffix("m"));
    private final SliderSetting blocksPer = this.add(new SliderSetting("BlocksPer", 1, 1, 8));
    public final SliderSetting predictTicks = this.add(new SliderSetting("PredictTicks", 2.0, 0.0, 50.0, 1.0));
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
    private final BooleanSetting autoDisable = this.add(new BooleanSetting("AutoDisable", true));
    private final SliderSetting range = this.add(new SliderSetting("Range", 5.0, 1.0, 8.0).setSuffix("m"));
    private final BooleanSetting checkMine = this.add(new BooleanSetting("DetectMining", false));
    private final BooleanSetting helper = this.add(new BooleanSetting("Helper", true));
    private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
    private final BooleanSetting onlyCrawling = this.add(new BooleanSetting("OnlyCrawling", false));
    private final BooleanSetting checkElytra = this.add(new BooleanSetting("CheckElytra", false));
    private final BooleanSetting extend = this.add(new BooleanSetting("Extend", true));
    private final BooleanSetting antiStep = this.add(new BooleanSetting("AntiStep", false));
    private final BooleanSetting onlyBreak = this.add(new BooleanSetting("OnlyBreak", false, this.antiStep::getValue));
    private final BooleanSetting head = this.add(new BooleanSetting("Head", true));
    private final BooleanSetting headExtend = this.add(new BooleanSetting("HeadExtend", true));
    private final BooleanSetting chestUp = this.add(new BooleanSetting("ChestUp", true));
    private final BooleanSetting onlyBreaking = this.add(new BooleanSetting("OnlyBreaking", false, this.chestUp::getValue));
    private final BooleanSetting chest = this.add(new BooleanSetting("Chest", true));
    private final BooleanSetting onlyGround = this.add(new BooleanSetting("OnlyGround", false, this.chest::getValue));
    private final BooleanSetting ignoreCrawling = this.add(new BooleanSetting("IgnoreCrawling", false, this.chest::getValue));
    private final BooleanSetting legs = this.add(new BooleanSetting("Legs", false));
    private final BooleanSetting legAnchor = this.add(new BooleanSetting("LegAnchor", true));
    private final BooleanSetting down = this.add(new BooleanSetting("Down", false));
    private final BooleanSetting onlyHole = this.add(new BooleanSetting("OnlyHole", false));
    private final BooleanSetting breakCrystal = this.add(new BooleanSetting("Break", true));
    private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", true));
    private final BooleanSetting selfGround = this.add(new BooleanSetting("SelfGround", true));
    public Player target;
    int progress = 0;

    public AutoTrap() {
        super("AutoTrap", Module.Category.Combat);
        this.setChinese("\u81ea\u52a8\u56f0\u4f4f");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        this.trapList.clear();
        this.placeList.clear();
        this.progress = 0;
        this.target = null;
        if (this.selfGround.getValue() && !AutoTrap.mc.player.onGround()) {
            return;
        }
        if (this.inventory.getValue() && !EntityUtil.inInventory()) {
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        if (this.usingPause.getValue() && AutoTrap.mc.player.isUsingItem()) {
            return;
        }
        if (!this.timer.passed((long)this.delay.getValue())) {
            return;
        }
        if (this.targetMod.getValue() == TargetMode.Single) {
            this.target = CombatUtil.getClosestEnemy(this.range.getValue());
            if (this.target == null) {
                if (this.autoDisable.getValue()) {
                    this.disable();
                }
                return;
            }
            this.trapTarget(this.target);
        } else if (this.targetMod.getValue() == TargetMode.Multi) {
            boolean found = false;
            for (Player player : CombatUtil.getEnemies(this.range.getValue())) {
                found = true;
                this.target = player;
                this.trapTarget(this.target);
            }
            if (!found) {
                if (this.autoDisable.getValue()) {
                    this.disable();
                }
                this.target = null;
            }
        }
    }

    private void trapTarget(Player target) {
        if (this.onlyHole.getValue() && !Aevum.HOLE.isHole(EntityUtil.getEntityPos((Entity)target))) {
            return;
        }
        if (this.onlyCrawling.getValue() && !target.isVisuallyCrawling() && (!this.checkElytra.getValue() || !(((ItemStack)target.getInventory().armor.get(2)).method_7909() instanceof ElytraItem) || AutoTrap.mc.player.getY() < target.getY() + 1.0 && !target.isFallFlying())) {
            return;
        }
        Vec3 playerPos = this.predictTicks.getValue() > 0.0 ? PredictUtil.getPos(target, this.predictTicks.getValueInt()) : target.position();
        this.doTrap(target, new BlockPosX(playerPos.method_10216(), playerPos.method_10214(), playerPos.method_10215()));
    }

    private void doTrap(Player player, BlockPos pos) {
        BlockPos offsetPos;
        int chestOffset;
        if (pos == null) {
            return;
        }
        if (this.trapList.contains(pos)) {
            return;
        }
        this.trapList.add(pos);
        int headOffset = player.isVisuallyCrawling() ? 1 : 2;
        int n = chestOffset = player.isVisuallyCrawling() ? 0 : 1;
        if (this.legs.getValue()) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos legPos = pos.method_10093(i);
                this.tryPlaceBlock(legPos, this.legAnchor.getValue(), false, false);
                if (BlockUtil.getPlaceSide(legPos) != null || !BlockUtil.clientCanPlace(legPos, this.breakCrystal.getValue()) || this.getHelper(legPos) == null) continue;
                this.tryPlaceObsidian(this.getHelper(legPos));
            }
        }
        if (this.headExtend.getValue()) {
            int[] xOffsets;
            int[] nArray = xOffsets = new int[]{1, 0, -1};
            int n2 = nArray.length;
            for (int i = 0; i < n2; ++i) {
                int x = nArray[i];
                int[] nArray2 = xOffsets;
                int n3 = nArray2.length;
                for (int j = 0; j < n3; ++j) {
                    int z = nArray2[j];
                    offsetPos = pos.method_10069(z, 0, x);
                    if (!this.checkEntity(new BlockPos((Vec3i)offsetPos))) continue;
                    this.tryPlaceBlock(offsetPos.method_10086(headOffset), this.headMode.getValue() == Mode.Anchor, this.headMode.getValue() == Mode.Concrete, this.headMode.getValue() == Mode.Web);
                }
            }
        }
        if (this.head.getValue() && BlockUtil.clientCanPlace(pos.method_10086(headOffset), this.breakCrystal.getValue())) {
            if (BlockUtil.getPlaceSide(pos.method_10086(headOffset)) == null) {
                boolean trapChest = this.helper.getValue();
                if (this.getHelper(pos.method_10086(headOffset)) != null) {
                    this.tryPlaceObsidian(this.getHelper(pos.method_10086(headOffset)));
                    trapChest = false;
                }
                if (trapChest) {
                    for (Direction i : Direction.values()) {
                        if (i == Direction.DOWN || i == Direction.UP) continue;
                        BlockPos offsetPos3 = pos.method_10093(i).method_10086(chestOffset);
                        if (!BlockUtil.isStrictDirection(pos.method_10093(i).method_10084(), i.getOpposite()) || !BlockUtil.clientCanPlace(offsetPos3.method_10086(chestOffset), this.breakCrystal.getValue()) || !BlockUtil.canPlace(offsetPos3, this.placeRange.getValue(), this.breakCrystal.getValue())) continue;
                        this.tryPlaceObsidian(offsetPos3);
                        trapChest = false;
                        break;
                    }
                    if (trapChest) {
                        for (Direction i : Direction.values()) {
                            if (i == Direction.DOWN || i == Direction.UP) continue;
                            BlockPos offsetPos4 = pos.method_10093(i).method_10086(chestOffset);
                            if (!BlockUtil.isStrictDirection(pos.method_10093(i).method_10084(), i.getOpposite()) || !BlockUtil.clientCanPlace(offsetPos4.method_10086(chestOffset), this.breakCrystal.getValue()) || BlockUtil.getPlaceSide(offsetPos4) != null || !BlockUtil.clientCanPlace(offsetPos4, this.breakCrystal.getValue()) || this.getHelper(offsetPos4) == null) continue;
                            this.tryPlaceObsidian(this.getHelper(offsetPos4));
                            trapChest = false;
                            break;
                        }
                        if (trapChest) {
                            for (Direction i : Direction.values()) {
                                if (i == Direction.DOWN || i == Direction.UP) continue;
                                BlockPos offsetPos5 = pos.method_10093(i).method_10086(chestOffset);
                                if (!BlockUtil.isStrictDirection(pos.method_10093(i).method_10084(), i.getOpposite()) || !BlockUtil.clientCanPlace(offsetPos5.method_10086(chestOffset), this.breakCrystal.getValue()) || BlockUtil.getPlaceSide(offsetPos5) != null || !BlockUtil.clientCanPlace(offsetPos5, this.breakCrystal.getValue()) || this.getHelper(offsetPos5) == null || BlockUtil.getPlaceSide(offsetPos5.method_10074()) != null || !BlockUtil.clientCanPlace(offsetPos5.method_10074(), this.breakCrystal.getValue()) || this.getHelper(offsetPos5.method_10074()) == null) continue;
                                this.tryPlaceObsidian(this.getHelper(offsetPos5.method_10074()));
                                break;
                            }
                        }
                    }
                }
            }
            this.tryPlaceBlock(pos.method_10086(headOffset), this.headMode.getValue() == Mode.Anchor, this.headMode.getValue() == Mode.Concrete, this.headMode.getValue() == Mode.Web);
        }
        if (this.antiStep.getValue() && (Aevum.BREAK.isMining(pos.method_10086(headOffset)) || !this.onlyBreak.getValue())) {
            if (BlockUtil.getPlaceSide(pos.method_10086(3)) == null && BlockUtil.clientCanPlace(pos.method_10086(3), this.breakCrystal.getValue()) && this.getHelper(pos.method_10086(3), Direction.DOWN) != null) {
                this.tryPlaceObsidian(this.getHelper(pos.method_10086(3)));
            }
            this.tryPlaceObsidian(pos.method_10086(3));
        }
        if (this.down.getValue()) {
            BlockPos offsetPos6 = pos.method_10074();
            this.tryPlaceObsidian(offsetPos6);
            if (BlockUtil.getPlaceSide(offsetPos6) == null && BlockUtil.clientCanPlace(offsetPos6, this.breakCrystal.getValue()) && this.getHelper(offsetPos6) != null) {
                this.tryPlaceObsidian(this.getHelper(offsetPos6));
            }
        }
        if (this.chestUp.getValue()) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos chestUpPos = pos.method_10093(i).method_10086(headOffset);
                if (this.onlyBreaking.getValue() && !Aevum.BREAK.isMining(pos.method_10086(headOffset))) continue;
                this.tryPlaceObsidian(chestUpPos);
                if (BlockUtil.getPlaceSide(chestUpPos) != null || !BlockUtil.clientCanPlace(chestUpPos, this.breakCrystal.getValue())) continue;
                if (this.getHelper(chestUpPos) != null) {
                    this.tryPlaceObsidian(this.getHelper(chestUpPos));
                    continue;
                }
                if (BlockUtil.getPlaceSide(chestUpPos.method_10074()) != null || !BlockUtil.clientCanPlace(chestUpPos.method_10074(), this.breakCrystal.getValue()) || this.getHelper(chestUpPos.method_10074()) == null) continue;
                this.tryPlaceObsidian(this.getHelper(chestUpPos.method_10074()));
            }
        }
        if (!(!this.chest.getValue() || this.onlyGround.getValue() && !this.target.onGround() || this.ignoreCrawling.getValue() && this.target.isVisuallyCrawling())) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos chestPos = pos.method_10093(i).method_10086(chestOffset);
                this.tryPlaceObsidian(chestPos);
                if (BlockUtil.getPlaceSide(chestPos) != null || !BlockUtil.clientCanPlace(chestPos, this.breakCrystal.getValue())) continue;
                if (this.getHelper(chestPos) != null) {
                    this.tryPlaceObsidian(this.getHelper(chestPos));
                    continue;
                }
                if (BlockUtil.getPlaceSide(chestPos.method_10074()) != null || !BlockUtil.clientCanPlace(chestPos.method_10074(), this.breakCrystal.getValue()) || this.getHelper(chestPos.method_10074()) == null) continue;
                this.tryPlaceObsidian(this.getHelper(chestPos.method_10074()));
            }
        }
        if (this.extend.getValue()) {
            for (int x : new int[]{1, 0, -1}) {
                for (int z : new int[]{1, 0, -1}) {
                    offsetPos = pos.method_10069(x, 0, z);
                    if (!this.checkEntity(new BlockPos((Vec3i)offsetPos))) continue;
                    this.doTrap(player, offsetPos);
                }
            }
        }
    }

    @Override
    public String getInfo() {
        if (this.target != null) {
            return this.target.method_5477().getString();
        }
        return null;
    }

    public BlockPos getHelper(BlockPos pos) {
        if (!this.helper.getValue()) {
            return null;
        }
        for (Direction i : Direction.values()) {
            if (this.checkMine.getValue() && Aevum.BREAK.isMining(pos.method_10093(i)) || !BlockUtil.isStrictDirection(pos.method_10093(i), i.getOpposite()) || !BlockUtil.canPlace(pos.method_10093(i), this.placeRange.getValue(), this.breakCrystal.getValue())) continue;
            return pos.method_10093(i);
        }
        return null;
    }

    public BlockPos getHelper(BlockPos pos, Direction ignore) {
        if (!this.helper.getValue()) {
            return null;
        }
        for (Direction i : Direction.values()) {
            if (i == ignore || this.checkMine.getValue() && Aevum.BREAK.isMining(pos.method_10093(i)) || !BlockUtil.isStrictDirection(pos.method_10093(i), i.getOpposite()) || !BlockUtil.canPlace(pos.method_10093(i), this.placeRange.getValue(), this.breakCrystal.getValue())) continue;
            return pos.method_10093(i);
        }
        return null;
    }

    private boolean checkEntity(BlockPos pos) {
        if (AutoTrap.mc.player.method_5829().method_994(new AABB(pos))) {
            return false;
        }
        for (Entity class_12972 : Aevum.THREAD.getPlayers()) {
            if (!class_12972.method_5829().method_994(new AABB(pos)) || !class_12972.isAlive()) continue;
            return true;
        }
        return false;
    }

    private void tryPlaceBlock(BlockPos pos, boolean anchor, boolean sand, boolean web) {
        int block;
        if (this.placeList.contains(pos)) {
            return;
        }
        if (Aevum.BREAK.isMining(pos)) {
            return;
        }
        if (!BlockUtil.canPlace(pos, 6.0, this.breakCrystal.getValue())) {
            return;
        }
        if (!((double)this.progress < this.blocksPer.getValue())) {
            return;
        }
        if ((double)Mth.method_15355((float)((float)AutoTrap.mc.player.getEyePosition().method_1025(pos.method_46558()))) > this.placeRange.getValue()) {
            return;
        }
        int old = AutoTrap.mc.player.getInventory().selected;
        if (sand) {
            block = this.getConcrete();
        } else if (web) {
            block = this.getWeb() != -1 ? this.getWeb() : this.getBlock();
        } else {
            int n = block = anchor && this.getAnchor() != -1 ? this.getAnchor() : this.getBlock();
        }
        if (block == -1) {
            return;
        }
        this.placeList.add(pos);
        CombatUtil.attackCrystal(pos, this.rotate.getValue(), this.usingPause.getValue());
        this.doSwap(block);
        BlockUtil.placeBlock(pos, this.rotate.getValue());
        if (this.inventory.getValue()) {
            this.doSwap(block);
            EntityUtil.syncInventory();
        } else {
            this.doSwap(old);
        }
        this.timer.reset();
        ++this.progress;
    }

    private void tryPlaceObsidian(BlockPos pos) {
        if (pos == null) {
            return;
        }
        if (this.placeList.contains(pos)) {
            return;
        }
        if (Aevum.BREAK.isMining(pos)) {
            return;
        }
        if (!BlockUtil.canPlace(pos, 6.0, this.breakCrystal.getValue())) {
            return;
        }
        if (!((double)this.progress < this.blocksPer.getValue())) {
            return;
        }
        if ((double)Mth.method_15355((float)((float)AutoTrap.mc.player.getEyePosition().method_1025(pos.method_46558()))) > this.placeRange.getValue()) {
            return;
        }
        int old = AutoTrap.mc.player.getInventory().selected;
        int block = this.getBlock();
        if (block == -1) {
            return;
        }
        BlockUtil.placedPos.add(pos);
        this.placeList.add(pos);
        CombatUtil.attackCrystal(pos, this.rotate.getValue(), this.usingPause.getValue());
        this.doSwap(block);
        BlockUtil.placeBlock(pos, this.rotate.getValue());
        if (this.inventory.getValue()) {
            this.doSwap(block);
            EntityUtil.syncInventory();
        } else {
            this.doSwap(old);
        }
        this.timer.reset();
        ++this.progress;
    }

    private void doSwap(int slot) {
        if (this.inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, AutoTrap.mc.player.getInventory().selected);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private int getBlock() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
        }
        return InventoryUtil.findBlock(Blocks.OBSIDIAN);
    }

    private int getConcrete() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findClassInventorySlot(ConcretePowderBlock.class);
        }
        return InventoryUtil.findClass(ConcretePowderBlock.class);
    }

    private int getWeb() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.COBWEB);
        }
        return InventoryUtil.findBlock(Blocks.COBWEB);
    }

    private int getAnchor() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.RESPAWN_ANCHOR);
        }
        return InventoryUtil.findBlock(Blocks.RESPAWN_ANCHOR);
    }

    public static enum TargetMode {
        Single,
        Multi;

    }

    private static enum Mode {
        Obsidian,
        Anchor,
        Web,
        Concrete;

    }
}

