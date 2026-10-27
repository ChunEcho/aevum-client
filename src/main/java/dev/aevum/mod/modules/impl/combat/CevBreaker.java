/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Player
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.combat.CombatUtil;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import dev.aevum.mod.modules.impl.player.PacketMine;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.Entity;
import net.minecraft.Player;
import net.minecraft.Blocks;
import net.minecraft.Block;
import net.minecraft.BlockPos;
import net.minecraft.Direction;

public class CevBreaker
extends Module {
    public static CevBreaker INSTANCE;
    private final SliderSetting targetRange = this.add(new SliderSetting("TargetRange", 5.0, 0.0, 8.0, 0.1));
    private final SliderSetting breakRange = this.add(new SliderSetting("BreakRange", 5.0, 0.0, 8.0, 0.1));
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 100, 0, 500).setSuffix("ms"));
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
    private final BooleanSetting ground = this.add(new BooleanSetting("Ground", true));
    private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
    private final BooleanSetting top = this.add(new BooleanSetting("Top", false));
    private final BooleanSetting bevel = this.add(new BooleanSetting("Bevel", true));
    private final Timer timer = new Timer();
    private Player target = null;

    public CevBreaker() {
        super("CevBreaker", Module.Category.Combat);
        this.setChinese("\u81ea\u52a8\u70b8\u5934");
        INSTANCE = this;
    }

    public static boolean canPlaceCrystal(BlockPos pos) {
        return CevBreaker.mc.level.isEmptyBlock(pos) && BlockUtil.noEntityBlockCrystal(pos, false) && BlockUtil.noEntityBlockCrystal(pos.method_10084(), false) && (!ClientSetting.INSTANCE.lowVersion.getValue() || CevBreaker.mc.level.isEmptyBlock(pos.method_10084()));
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.inventory.getValue() && !EntityUtil.inInventory()) {
            return;
        }
        if (this.ground.getValue() && !CevBreaker.mc.player.onGround()) {
            return;
        }
        PacketMine.INSTANCE.crystal.setValue(true);
        this.target = CombatUtil.getClosestEnemy(this.targetRange.getValue());
        if (this.target != null) {
            BlockPos pos;
            BlockPos targetPos = EntityUtil.getEntityPos((Entity)this.target);
            if (PacketMine.getBreakPos() != null) {
                for (Direction facing : Direction.values()) {
                    if (facing == Direction.DOWN || (facing == Direction.UP ? !this.top.getValue() : !this.bevel.getValue())) continue;
                    pos = targetPos.method_10086(1).method_10093(facing);
                    if (pos.method_10084().method_46558().method_1022(CevBreaker.mc.player.position()) > this.breakRange.getValue() || !PacketMine.getBreakPos().equals((Object)targetPos.method_10086(1).method_10093(facing))) continue;
                    if (CevBreaker.canPlaceCrystal(targetPos.method_10086(2).method_10093(facing))) {
                        if (CevBreaker.mc.level.isEmptyBlock(pos)) {
                            if (!BlockUtil.canPlace(pos)) continue;
                            if (!this.timer.passedMs(this.delay.getValue())) {
                                return;
                            }
                            this.placeBlock(pos);
                            this.timer.reset();
                            return;
                        }
                        if (this.getBlock(pos) != Blocks.OBSIDIAN) continue;
                        PacketMine.INSTANCE.mine(pos);
                        this.timer.reset();
                        return;
                    }
                    if (!BlockUtil.hasCrystal(targetPos.method_10086(2).method_10093(facing))) continue;
                    if (CevBreaker.mc.level.isEmptyBlock(pos)) {
                        return;
                    }
                    if (this.getBlock(pos) != Blocks.OBSIDIAN) continue;
                    PacketMine.INSTANCE.mine(pos);
                    this.timer.reset();
                    return;
                }
            }
            for (Direction facing : Direction.values()) {
                BlockPos class_23382;
                if (facing == Direction.DOWN || (facing == Direction.UP ? !this.top.getValue() : !this.bevel.getValue())) continue;
                pos = targetPos.method_10086(1).method_10093(facing);
                if (class_23382.method_10084().method_46558().method_1022(CevBreaker.mc.player.position()) > this.breakRange.getValue()) continue;
                if (CevBreaker.canPlaceCrystal(targetPos.method_10086(2).method_10093(facing))) {
                    if (CevBreaker.mc.level.isEmptyBlock(pos)) {
                        if (!BlockUtil.canPlace(pos)) continue;
                        if (!this.timer.passedMs(this.delay.getValue())) {
                            return;
                        }
                        this.placeBlock(pos);
                        this.timer.reset();
                        break;
                    }
                    if (this.getBlock(pos) != Blocks.OBSIDIAN) continue;
                    PacketMine.INSTANCE.mine(pos);
                    this.timer.reset();
                    break;
                }
                if (!BlockUtil.hasCrystal(targetPos.method_10086(2).method_10093(facing))) continue;
                if (CevBreaker.mc.level.isEmptyBlock(pos)) break;
                if (this.getBlock(pos) != Blocks.OBSIDIAN) continue;
                PacketMine.INSTANCE.mine(pos);
                this.timer.reset();
                break;
            }
        }
    }

    private void doSwap(int slot) {
        if (this.inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, CevBreaker.mc.player.getInventory().selected);
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

    private void placeBlock(BlockPos pos) {
        int block = this.getBlock();
        if (block == -1) {
            return;
        }
        int oldSlot = CevBreaker.mc.player.getInventory().selected;
        if (BlockUtil.canPlace(pos)) {
            if (BlockUtil.allowAirPlace()) {
                this.doSwap(block);
                BlockUtil.placedPos.add(pos);
                BlockUtil.airPlace(pos, this.rotate.getValue());
                if (this.inventory.getValue()) {
                    this.doSwap(block);
                    EntityUtil.syncInventory();
                } else {
                    this.doSwap(oldSlot);
                }
                return;
            }
            Direction side = BlockUtil.getPlaceSide(pos);
            if (side == null) {
                return;
            }
            this.doSwap(block);
            BlockUtil.placedPos.add(pos);
            BlockUtil.clickBlock(pos.method_10093(side), side.getOpposite(), this.rotate.getValue());
            if (this.inventory.getValue()) {
                this.doSwap(block);
                EntityUtil.syncInventory();
            } else {
                this.doSwap(oldSlot);
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

    private Block getBlock(BlockPos pos) {
        return CevBreaker.mc.level.method_8320(pos).method_26204();
    }
}

