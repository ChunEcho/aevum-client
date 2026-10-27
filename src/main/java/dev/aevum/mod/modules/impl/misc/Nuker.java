/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Blocks
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.ServerboundPlayerActionPacket
 *  net.minecraft.ServerboundPlayerActionPacket$Action
 */
package dev.aevum.mod.modules.impl.misc;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.player.PacketMine;
import dev.aevum.mod.modules.impl.render.PlaceRender;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.ServerboundPlayerActionPacket;

public class Nuker
extends Module {
    private final SliderSetting range = this.add(new SliderSetting("Range", 4.0, 0.0, 8.0, 0.1));
    private final BooleanSetting down = this.add(new BooleanSetting("Down", false));
    private final BooleanSetting sand = this.add(new BooleanSetting("Sand", false));
    private final SliderSetting breaks = this.add(new SliderSetting("Breaks", 10, 0, 20, this.sand::getValue));

    public Nuker() {
        super("Nuker", Module.Category.Misc);
        this.setChinese("\u8303\u56f4\u6316\u6398");
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (PacketMine.getBreakPos() != null && !Nuker.mc.level.isEmptyBlock(PacketMine.getBreakPos())) {
            return;
        }
        if (this.sand.getValue()) {
            if (!Nuker.mc.player.onGround()) {
                return;
            }
            int elementCodec = 0;
            for (BlockPos sand : BlockUtil.getSphere(this.range.getValueFloat(), Nuker.mc.player.getEyePosition())) {
                Direction side;
                if (Blocks.SAND != Nuker.mc.level.method_8320(sand).method_26204() && Blocks.RED_SAND != Nuker.mc.level.method_8320(sand).method_26204() || (side = BlockUtil.getClickSideStrict(sand)) == null) continue;
                PlaceRender.INSTANCE.create(sand);
                Aevum.ROTATION.snapAt(sand.method_46558());
                Nuker.sendSequencedPacket(id -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action.STOP_DESTROY_BLOCK, sand, side, id));
                Nuker.sendSequencedPacket(id -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action.START_DESTROY_BLOCK, sand, side, id));
                Nuker.sendSequencedPacket(id -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action.STOP_DESTROY_BLOCK, sand, side, id));
                Aevum.ROTATION.snapBack();
                if (!((double)(++elementCodec) >= this.breaks.getValue())) continue;
                return;
            }
        } else {
            BlockPos pos = this.getBlock();
            if (pos != null) {
                PacketMine.INSTANCE.mine(pos);
            }
        }
    }

    private BlockPos getBlock() {
        BlockPos down = null;
        for (BlockPos pos : BlockUtil.getSphere(this.range.getValueFloat(), Nuker.mc.player.getEyePosition())) {
            if (Nuker.mc.level.isEmptyBlock(pos) || PacketMine.unbreakable(pos) || BlockUtil.getClickSideStrict(pos) == null) continue;
            if ((double)pos.method_10264() < Nuker.mc.player.getY()) {
                if (down != null || !this.down.getValue()) continue;
                down = pos;
                continue;
            }
            return pos;
        }
        return down;
    }
}

