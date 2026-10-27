/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.Entity
 *  net.minecraft.EndCrystal
 *  net.minecraft.Items
 *  net.minecraft.Blocks
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.AABB
 *  net.minecraft.Vec3i
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundInteractPacket
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.api.events.impl.EntitySpawnedEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.enums.SwingSide;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.InteractionHand;
import net.minecraft.Entity;
import net.minecraft.EndCrystal;
import net.minecraft.Items;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.AABB;
import net.minecraft.Vec3i;
import net.minecraft.Packet;
import net.minecraft.ServerboundInteractPacket;

public class AntiCobweb
extends Module {
    public static AntiCobweb INSTANCE;
    private BlockPos targetCrystalPos;
    private final Timer breakTimer = new Timer();
    private final Timer placeTimer = new Timer();
    private final SliderSetting safeHealth = this.add(new SliderSetting("SafeHealth", 5.0, 0.0, 20.0, 0.5).setSuffix("hp"));

    public AntiCobweb() {
        super("AntiCobweb", "Uses crystal to break cobwebs around you", Module.Category.Combat);
        this.setChinese("\u81ea\u52a8\u7834\u7f51");
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        this.targetCrystalPos = null;
    }

    @EventListener
    public void onTick(ClientTickEvent event) {
        BlockPos crystalPos;
        if (AntiCobweb.nullCheck()) {
            return;
        }
        if (event.isPost()) {
            return;
        }
        BlockPos playerPos = AntiCobweb.mc.player.method_24515();
        BlockPos cobwebPos = null;
        if (AntiCobweb.mc.level.method_8320(playerPos).method_26204() == Blocks.COBWEB) {
            cobwebPos = playerPos;
        } else if (AntiCobweb.mc.level.method_8320(playerPos.method_10084()).method_26204() == Blocks.COBWEB) {
            cobwebPos = playerPos.method_10084();
        }
        if (cobwebPos == null) {
            this.targetCrystalPos = null;
            return;
        }
        if ((double)(AntiCobweb.mc.player.getHealth() + AntiCobweb.mc.player.getAbsorptionAmount()) <= this.safeHealth.getValue()) {
            return;
        }
        if (this.targetCrystalPos != null && this.breakTimer.passedMs(100.0)) {
            this.breakCrystalAt(this.targetCrystalPos);
            this.targetCrystalPos = null;
            return;
        }
        if (this.targetCrystalPos == null && this.placeTimer.passedMs(300.0) && (crystalPos = this.findBestCrystalPos(cobwebPos)) != null) {
            this.placeCrystalAt(crystalPos);
        }
    }

    @EventListener
    public void onEntitySpawn(EntitySpawnedEvent event) {
        EndCrystal crystal;
        Entity entity = event.getEntity();
        if (this.targetCrystalPos != null && entity instanceof EndCrystal && (crystal = (EndCrystal)entity).method_24515().equals((Object)this.targetCrystalPos)) {
            this.doBreak(crystal);
            this.targetCrystalPos = null;
        }
    }

    private BlockPos findBestCrystalPos(BlockPos cobwebPos) {
        BlockPos bestPos = null;
        double bestDist = Double.MAX_VALUE;
        for (int x = -3; x <= 3; ++x) {
            for (int z = -3; z <= 3; ++z) {
                double dist;
                BlockPos crystalPos;
                if (x == 0 && z == 0 || !this.isValidCrystalPos(crystalPos = new BlockPos(cobwebPos.method_10263() + x, cobwebPos.method_10264(), cobwebPos.getZ() + z)) || !((dist = crystalPos.method_10262((Vec3i)cobwebPos)) < bestDist)) continue;
                bestDist = dist;
                bestPos = crystalPos;
            }
        }
        return bestPos;
    }

    private boolean isValidCrystalPos(BlockPos pos) {
        BlockPos below = pos.method_10074();
        return (BlockUtil.getBlock(below) == Blocks.BEDROCK || BlockUtil.getBlock(below) == Blocks.OBSIDIAN) && BlockUtil.getClickSideStrict(below) != null && AntiCobweb.mc.level.isEmptyBlock(pos) && AntiCobweb.mc.level.isEmptyBlock(pos.method_10084());
    }

    private void placeCrystalAt(BlockPos pos) {
        int crystalSlot = InventoryUtil.findItem(Items.END_CRYSTAL);
        if (crystalSlot == -1) {
            return;
        }
        boolean offhand = AntiCobweb.mc.player.getOffhandItem().method_7909() == Items.END_CRYSTAL;
        int oldSlot = AntiCobweb.mc.player.getInventory().selected;
        if (!offhand) {
            InventoryUtil.switchToSlot(crystalSlot);
        }
        BlockPos obsPos = pos.method_10074();
        Direction facing = BlockUtil.getClickSide(obsPos);
        BlockUtil.clickBlock(obsPos, facing, false, offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, SwingSide.All);
        if (!offhand) {
            InventoryUtil.switchToSlot(oldSlot);
        }
        this.targetCrystalPos = pos;
        this.placeTimer.reset();
        this.breakTimer.reset();
    }

    private void breakCrystalAt(BlockPos pos) {
        for (EndCrystal crystal : BlockUtil.getEndCrystals(new AABB((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.getZ(), (double)(pos.method_10263() + 1), (double)(pos.method_10264() + 2), (double)(pos.getZ() + 1)))) {
            if (!crystal.isAlive()) continue;
            this.doBreak(crystal);
        }
    }

    private void doBreak(EndCrystal crystal) {
        mc.getConnection().method_52787((Packet)ServerboundInteractPacket.method_34206((Entity)crystal, (boolean)AntiCobweb.mc.player.isShiftKeyDown()));
        AntiCobweb.mc.player.resetAttackStrengthTicker();
        EntityUtil.swingHand(InteractionHand.MAIN_HAND, SwingSide.All);
    }
}

