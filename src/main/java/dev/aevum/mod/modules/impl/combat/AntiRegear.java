/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockPos
 *  net.minecraft.ShulkerBoxBlock
 *  net.minecraft.BlockEntity
 *  net.minecraft.ShulkerBoxBlockEntity
 *  net.minecraft.Mth
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.PlaceBlockEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.combat.AutoRegear;
import dev.aevum.mod.modules.impl.player.PacketMine;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.BlockPos;
import net.minecraft.ShulkerBoxBlock;
import net.minecraft.BlockEntity;
import net.minecraft.ShulkerBoxBlockEntity;
import net.minecraft.Mth;

public class AntiRegear
extends Module {
    public static AntiRegear INSTANCE;
    public final List<BlockPos> safe = new ArrayList<BlockPos>();
    private final SliderSetting safeRange = this.add(new SliderSetting("SafeRange", 2.0, 0.0, 8.0, 0.1));
    private final SliderSetting range = this.add(new SliderSetting("Range", 5.0, 0.0, 8.0, 0.1));
    private final BooleanSetting checkSelf = this.add(new BooleanSetting("CheckSelf", true));

    public AntiRegear() {
        super("AntiRegear", Module.Category.Combat);
        this.setChinese("\u53cd\u8865\u7ed9");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (PacketMine.getBreakPos() != null && AntiRegear.mc.level.method_8320(PacketMine.getBreakPos()).method_26204() instanceof ShulkerBoxBlock) {
            return;
        }
        this.safe.removeIf(pos -> !(AntiRegear.mc.level.method_8320(pos).method_26204() instanceof ShulkerBoxBlock));
        if (this.getBlock() != null) {
            PacketMine.INSTANCE.mine(this.getBlock().getBlockPos());
        }
    }

    @EventListener
    public void onPlace(PlaceBlockEvent event) {
        if (event.block instanceof ShulkerBoxBlock) {
            this.safe.add(event.blockPos);
        }
    }

    private ShulkerBoxBlockEntity getBlock() {
        for (BlockEntity entity : BlockUtil.getTileEntities()) {
            ShulkerBoxBlockEntity shulker;
            if (!(entity instanceof ShulkerBoxBlockEntity) || (double)Mth.method_15355((float)((float)AntiRegear.mc.player.method_5707((shulker = (ShulkerBoxBlockEntity)entity).getBlockPos().method_46558()))) <= this.safeRange.getValue() || this.checkSelf.getValue() && (this.safe.contains(shulker.getBlockPos()) || shulker.getBlockPos().equals((Object)AutoRegear.INSTANCE.placePos) && !AutoRegear.INSTANCE.timeoutTimer.passed(100L)) || !((double)Mth.method_15355((float)((float)AntiRegear.mc.player.method_5707(shulker.getBlockPos().method_46558()))) <= this.range.getValue())) continue;
            return shulker;
        }
        return null;
    }
}

