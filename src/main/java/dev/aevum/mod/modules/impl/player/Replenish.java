/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Player
 *  net.minecraft.ClickType
 *  net.minecraft.ItemStack
 */
package dev.aevum.mod.modules.impl.player;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.player.Sorter;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.Player;
import net.minecraft.ClickType;
import net.minecraft.ItemStack;

public class Replenish
extends Module {
    private final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.QuickMove));
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 2.0, 0.0, 5.0, 0.01).setSuffix("s"));
    private final SliderSetting min = this.add(new SliderSetting("Min", 50, 1, 100)).setSuffix("%");
    private final SliderSetting forceDelay = this.add(new SliderSetting("ForceDelay", 0.2, 0.0, 4.0, 0.01).setSuffix("s"));
    private final SliderSetting forceMin = this.add(new SliderSetting("ForceMin", 16, 1, 100)).setSuffix("%");
    private final Timer timer = new Timer();

    public Replenish() {
        super("Replenish", Module.Category.Player);
        this.setChinese("\u7269\u54c1\u680f\u8865\u5145");
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        for (int i = 0; i < 9; ++i) {
            if (!this.replenish(i)) continue;
            this.timer.reset();
            return;
        }
    }

    private boolean replenish(int slot) {
        ItemStack stack = Replenish.mc.player.getInventory().method_5438(slot);
        if (stack.method_7960()) {
            return false;
        }
        if (!stack.isStackable()) {
            return false;
        }
        int percent = (int)((double)stack.getCount() / (double)stack.getMaxStackSize() * 100.0);
        if ((double)percent > this.min.getValue()) {
            return false;
        }
        for (int i = 9; i < 36; ++i) {
            ItemStack item = Replenish.mc.player.getInventory().method_5438(i);
            if (item.method_7960() || !Sorter.canMerge(stack, item)) continue;
            if ((float)percent > this.forceMin.getValueFloat() ? !this.timer.passedS(this.delay.getValue()) : !this.timer.passedS(this.forceDelay.getValue())) {
                return false;
            }
            switch (this.mode.getValue().ordinal()) {
                case 0: {
                    Replenish.mc.gameMode.method_2906(Replenish.mc.player.inventoryMenu.containerId, i, 0, ClickType.QUICK_MOVE, (Player)Replenish.mc.player);
                    break;
                }
                case 1: {
                    Replenish.mc.gameMode.method_2906(Replenish.mc.player.inventoryMenu.containerId, i, 0, ClickType.PICKUP, (Player)Replenish.mc.player);
                    Replenish.mc.gameMode.method_2906(Replenish.mc.player.inventoryMenu.containerId, slot + 36, 0, ClickType.PICKUP, (Player)Replenish.mc.player);
                    Replenish.mc.gameMode.method_2906(Replenish.mc.player.inventoryMenu.containerId, i, 0, ClickType.PICKUP, (Player)Replenish.mc.player);
                }
            }
            return true;
        }
        return false;
    }

    public static enum Mode {
        QuickMove,
        ClickSlot;

    }
}

