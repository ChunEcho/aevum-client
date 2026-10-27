/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Player
 *  net.minecraft.ClickType
 *  net.minecraft.Item
 *  net.minecraft.Items
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundPickItemPacket
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.combat.Offhand;
import dev.aevum.mod.modules.settings.enums.Timing;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.Player;
import net.minecraft.ClickType;
import net.minecraft.Item;
import net.minecraft.Items;
import net.minecraft.Packet;
import net.minecraft.ServerboundPickItemPacket;

public class MainHand
extends Module {
    private final EnumSetting<Offhand.OffhandItem> item = this.add(new EnumSetting<Offhand.OffhandItem>("Item", Offhand.OffhandItem.Totem));
    private final SliderSetting slot = this.add(new SliderSetting("Slot", 2.0, 1.0, 9.0, 1.0));
    private final EnumSetting<Offhand.SwapMode> swapMode = this.add(new EnumSetting<Offhand.SwapMode>("SwapMode", Offhand.SwapMode.OffhandSwap));
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 50.0, 0.0, 500.0, 1.0));
    private final EnumSetting<Timing> timing = this.add(new EnumSetting<Timing>("Timing", Timing.All));
    private final Timer timer = new Timer();

    public MainHand() {
        super("MainHand", Module.Category.Combat);
        this.setChinese("\u4e3b\u624b\u8f85\u52a9");
    }

    @Override
    public String getInfo() {
        Offhand.OffhandItem offhandItem = this.item.getValue();
        return offhandItem == Offhand.OffhandItem.None ? null : offhandItem.name();
    }

    @EventListener
    public void onTick(ClientTickEvent clientTickEvent) {
        if (MainHand.nullCheck()) {
            return;
        }
        if (this.timing.is(Timing.Pre) && clientTickEvent.isPost() || this.timing.is(Timing.Post) && clientTickEvent.isPre()) {
            return;
        }
        Offhand.OffhandItem offhandItem = this.item.getValue();
        if (offhandItem == Offhand.OffhandItem.None) {
            return;
        }
        if (!this.timer.passed(this.delay.getValueInt())) {
            return;
        }
        if (!EntityUtil.inInventory()) {
            return;
        }
        int n = this.slot.getValueInt() - 1;
        if (this.isFilled(MainHand.mc.player.getInventory().method_5438(n).method_7909(), offhandItem)) {
            return;
        }
        int itemInventorySlot = this.findItemInventorySlot(offhandItem, n);
        if (itemInventorySlot == -1) {
            return;
        }
        this.swap(itemInventorySlot, n);
        this.timer.reset();
    }

    private boolean isFilled(Item Item2, Offhand.OffhandItem offhandItem) {
        switch (offhandItem) {
            case Totem: {
                return Item2 == Items.TOTEM_OF_UNDYING;
            }
            case Crystal: {
                return Item2 == Items.END_CRYSTAL;
            }
            case Shield: {
                return Item2 == Items.SHIELD;
            }
            case Chorus: {
                return Item2 == Items.CHORUS_FRUIT;
            }
            case Gapple: {
                return Item2 == Items.GOLDEN_APPLE || Item2 == Items.ENCHANTED_GOLDEN_APPLE;
            }
        }
        return false;
    }

    private Item itemOf(Offhand.OffhandItem offhandItem) {
        switch (offhandItem) {
            case Totem: {
                return Items.TOTEM_OF_UNDYING;
            }
            case Crystal: {
                return Items.END_CRYSTAL;
            }
            case Shield: {
                return Items.SHIELD;
            }
            case Chorus: {
                return Items.CHORUS_FRUIT;
            }
        }
        return Items.GOLDEN_APPLE;
    }

    private int findItemInventorySlot(Offhand.OffhandItem offhandItem, int n) {
        if (offhandItem != Offhand.OffhandItem.Gapple) {
            return this.scanFor(this.itemOf(offhandItem), n);
        }
        int scan = this.scanFor(Items.ENCHANTED_GOLDEN_APPLE, n);
        if (scan != -1) {
            return scan;
        }
        return this.scanFor(Items.GOLDEN_APPLE, n);
    }

    private int scanFor(Item Item2, int n) {
        if (this.swapMode.getValue().ordinal() == 2) {
            for (int i = 9; i < MainHand.mc.player.getInventory().method_5439() + 1; ++i) {
                if (i == n || MainHand.mc.player.getInventory().method_5438(i).method_7909() != Item2) continue;
                return i;
            }
            for (int j = 0; j < 9; ++j) {
                if (j == n || MainHand.mc.player.getInventory().method_5438(j).method_7909() != Item2) continue;
                return j;
            }
            return -1;
        }
        for (int k = 44; k >= 0; --k) {
            if (k == n || MainHand.mc.player.getInventory().method_5438(k).method_7909() != Item2) continue;
            return k < 9 ? k + 36 : k;
        }
        return -1;
    }

    private void swap(int n, int n2) {
        switch (this.swapMode.getValue().ordinal()) {
            case 1: {
                MainHand.mc.gameMode.method_2906(MainHand.mc.player.containerMenu.containerId, n, n2, ClickType.SWAP, (Player)MainHand.mc.player);
                EntityUtil.syncInventory();
                break;
            }
            case 2: {
                int selectedSlot = MainHand.mc.player.getInventory().selected;
                InventoryUtil.switchToSlot(n2);
                mc.getConnection().method_52787((Packet)new ServerboundPickItemPacket(n));
                InventoryUtil.switchToSlot(selectedSlot);
                break;
            }
            case 0: {
                MainHand.mc.gameMode.method_2906(MainHand.mc.player.containerMenu.containerId, n, 0, ClickType.PICKUP, (Player)MainHand.mc.player);
                MainHand.mc.gameMode.method_2906(MainHand.mc.player.containerMenu.containerId, n2 + 36, 0, ClickType.PICKUP, (Player)MainHand.mc.player);
                MainHand.mc.gameMode.method_2906(MainHand.mc.player.containerMenu.containerId, n, 0, ClickType.PICKUP, (Player)MainHand.mc.player);
                EntityUtil.syncInventory();
            }
        }
    }
}

