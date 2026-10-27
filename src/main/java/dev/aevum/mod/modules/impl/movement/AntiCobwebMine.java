/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Blocks
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 */
package dev.aevum.mod.modules.impl.movement;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.Direction;

public class AntiCobwebMine
extends Module {
    public static AntiCobwebMine INSTANCE;
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 100, 0, 500));
    private final BooleanSetting mineFeet = this.add(new BooleanSetting("MineFeet", true));
    private final BooleanSetting mineHead = this.add(new BooleanSetting("MineHead", false));
    private final Timer mineTimer = new Timer();

    public AntiCobwebMine() {
        super("AntiCobwebMine", "Auto-mine cobwebs at your feet/head", Module.Category.Movement);
        this.setChinese("\u9632\u86db\u7f51");
        INSTANCE = this;
    }

    @EventListener
    public void onTick(ClientTickEvent clientTickEvent) {
        if (Module.nullCheck() || clientTickEvent.isPost()) {
            return;
        }
        if (!this.mineTimer.passedMs(this.delay.getValue())) {
            return;
        }
        BlockPos playerPos = EntityUtil.getPlayerPos(true);
        BlockPos up = playerPos.method_10084();
        if (this.mineFeet.getValue() && this.isCobweb(playerPos)) {
            AntiCobwebMine.mc.gameMode.method_2910(playerPos, Direction.UP);
            this.mineTimer.reset();
        } else if (this.mineHead.getValue() && this.isCobweb(up)) {
            AntiCobwebMine.mc.gameMode.method_2910(up, Direction.UP);
            this.mineTimer.reset();
        }
    }

    private boolean isCobweb(BlockPos BlockPos) {
        return AntiCobwebMine.mc.level.method_8320(BlockPos).method_26204() == Blocks.COBWEB;
    }
}

