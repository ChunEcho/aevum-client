/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Blocks
 *  net.minecraft.BlockPos
 *  net.minecraft.LavaFluid$Source
 */
package dev.aevum.mod.modules.impl.misc;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.LavaFluid;

public class LavaFiller
extends Module {
    public final SliderSetting placeDelay = this.add(new SliderSetting("PlaceDelay", 50, 0, 500).setSuffix("ms"));
    private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
    private final SliderSetting range = this.add(new SliderSetting("Range", 5.0, 0.0, 8.0, 0.1));
    private final SliderSetting blocksPer = this.add(new SliderSetting("BlocksPer", 1, 1, 8));
    private final BooleanSetting detectMining = this.add(new BooleanSetting("DetectMining", false));
    private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", true));
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
    private final BooleanSetting packetPlace = this.add(new BooleanSetting("PacketPlace", true));
    private final Timer timer = new Timer();
    int progress = 0;

    public LavaFiller() {
        super("LavaFiller", Module.Category.Misc);
        this.setChinese("\u81ea\u52a8\u586b\u5ca9\u6d46");
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (!this.timer.passed((long)this.placeDelay.getValue())) {
            return;
        }
        if (this.inventory.getValue() && !EntityUtil.inInventory()) {
            return;
        }
        this.progress = 0;
        if (this.getBlock() == -1) {
            return;
        }
        if (this.usingPause.getValue() && LavaFiller.mc.player.isUsingItem()) {
            return;
        }
        for (BlockPos pos : BlockUtil.getSphere(this.range.getValueFloat())) {
            if (LavaFiller.mc.level.method_8320(pos).method_26204() != Blocks.LAVA || !(LavaFiller.mc.level.method_8320(pos).getFluidState().method_15772() instanceof LavaFluid.LavaFluid$Source)) continue;
            this.tryPlaceBlock(pos);
        }
    }

    private void tryPlaceBlock(BlockPos pos) {
        if (pos == null) {
            return;
        }
        if (this.detectMining.getValue() && Aevum.BREAK.isMining(pos)) {
            return;
        }
        if (!((double)this.progress < this.blocksPer.getValue())) {
            return;
        }
        int block = this.getBlock();
        if (block == -1) {
            return;
        }
        if (!BlockUtil.canPlace(pos, this.range.getValue(), false)) {
            return;
        }
        int old = LavaFiller.mc.player.getInventory().selected;
        this.doSwap(block);
        BlockUtil.placeBlock(pos, this.rotate.getValue(), this.packetPlace.getValue());
        if (this.inventory.getValue()) {
            this.doSwap(block);
            EntityUtil.syncInventory();
        } else {
            this.doSwap(old);
        }
        ++this.progress;
        this.timer.reset();
    }

    private void doSwap(int slot) {
        if (this.inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, LavaFiller.mc.player.getInventory().selected);
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
}

