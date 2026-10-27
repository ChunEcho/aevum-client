/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.Entity
 *  net.minecraft.Blocks
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.Vec3
 *  net.minecraft.ServerboundUseItemOnPacket
 *  net.minecraft.BlockHitResult
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.AntiCheat;
import dev.aevum.mod.modules.impl.movement.FastWeb;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.InteractionHand;
import net.minecraft.Entity;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.Vec3;
import net.minecraft.ServerboundUseItemOnPacket;
import net.minecraft.BlockHitResult;

public class AntiPiston
extends Module {
    public static AntiPiston INSTANCE;
    public final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", false));
    public final BooleanSetting swing = this.add(new BooleanSetting("Swing", true));
    public final BooleanSetting inventorySwap = this.add(new BooleanSetting("InventorySwap", true));
    public final SliderSetting placeDelay = this.add(new SliderSetting("PlaceDelay", 50.0, 0.0, 500.0, 1.0));
    private final Timer timer = new Timer();
    private boolean suppressedFastWeb = false;

    public AntiPiston() {
        super("AntiPiston", "Places a cobweb above you when your lower body is stuck in obsidian", Module.Category.Combat);
        this.setChinese("\u9632\u6d3b\u585e");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (FastWeb.INSTANCE != null && FastWeb.INSTANCE.isOn()) {
            FastWeb.INSTANCE.pauseForAntiPiston();
            this.suppressedFastWeb = true;
        }
    }

    @Override
    public void onDisable() {
        if (this.suppressedFastWeb && FastWeb.INSTANCE != null) {
            FastWeb.INSTANCE.resumeAfterAntiPiston();
        }
        this.suppressedFastWeb = false;
    }

    @EventListener
    public void onTick(ClientTickEvent event) {
        if (Module.nullCheck() || event.isPost()) {
            return;
        }
        if (!this.timer.passed(this.placeDelay.getValueInt())) {
            return;
        }
        BlockPos headWebPos = this.getHeadWebPos();
        if (headWebPos == null) {
            return;
        }
        if (this.getWebSlot() == -1) {
            return;
        }
        if (this.placeWeb(headWebPos)) {
            this.timer.reset();
        }
    }

    private BlockPos getHeadWebPos() {
        if (AntiPiston.mc.player == null || AntiPiston.mc.level == null) {
            return null;
        }
        BlockPos getBlockPos = AntiPiston.mc.player.method_24515();
        BlockPos isInRange = getBlockPos.method_10074();
        BlockPos lambda$static$4 = isInRange.method_10074();
        if (!this.isObsidian(isInRange) && !this.isObsidian(lambda$static$4)) {
            return null;
        }
        if (!BlockUtil.canCollide((Entity)AntiPiston.mc.player, AntiPiston.mc.player.method_5829())) {
            return null;
        }
        BlockPos isHolding = getBlockPos.method_10086(1);
        if (!BlockUtil.canReplace(isHolding)) {
            return null;
        }
        if (BlockUtil.getPlaceSide(isHolding, 5.0) == null && !BlockUtil.allowAirPlace()) {
            return null;
        }
        return isHolding;
    }

    private boolean isObsidian(BlockPos pos) {
        return BlockUtil.getBlock(pos) == Blocks.OBSIDIAN;
    }

    private boolean placeWeb(BlockPos pos) {
        Direction placeSide = BlockUtil.getPlaceSide(pos);
        BlockPos offset = pos;
        Direction Direction = Direction.DOWN;
        if (placeSide == null) {
            if (!BlockUtil.allowAirPlace()) {
                return false;
            }
        } else {
            offset = pos.method_10093(placeSide);
            Direction = placeSide.getOpposite();
        }
        Vec3 iterate = offset.method_46558().method_1031((double)Direction.getNormal().method_10263() * 0.5, (double)Direction.getNormal().method_10264() * 0.5, (double)Direction.getNormal().getZ() * 0.5);
        if (this.rotate.getValue()) {
            Aevum.ROTATION.lookAt(iterate);
        }
        int webSlot = this.getWebSlot();
        int selectedSlot = AntiPiston.mc.player.getInventory().selected;
        this.doSwap(webSlot);
        EntityUtil.swingHand(InteractionHand.MAIN_HAND, AntiCheat.INSTANCE.interactSwing.getValue());
        BlockHitResult bhr = new BlockHitResult(iterate, Direction, offset, false);
        Module.sendSequencedPacket(n -> new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, bhr, n));
        if (this.inventorySwap.getValue()) {
            this.doSwap(webSlot);
            EntityUtil.syncInventory();
        } else {
            this.doSwap(selectedSlot);
        }
        BlockUtil.placedPos.add(pos);
        return true;
    }

    private void doSwap(int slot) {
        if (this.inventorySwap.getValue()) {
            InventoryUtil.inventorySwap(slot, AntiPiston.mc.player.getInventory().selected);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private int getWebSlot() {
        if (this.inventorySwap.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.COBWEB);
        }
        return InventoryUtil.findBlock(Blocks.COBWEB);
    }
}

