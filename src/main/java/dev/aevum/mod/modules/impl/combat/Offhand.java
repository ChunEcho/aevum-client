/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.EndCrystal
 *  net.minecraft.Player
 *  net.minecraft.ClickType
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.PickaxeItem
 *  net.minecraft.SwordItem
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.Vec3
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundPickItemPacket
 *  net.minecraft.ServerboundPlayerActionPacket
 *  net.minecraft.ServerboundPlayerActionPacket$Action
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.api.events.impl.TotemEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.combat.AutoCrystal;
import dev.aevum.mod.modules.settings.enums.Timing;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.Entity;
import net.minecraft.EndCrystal;
import net.minecraft.Player;
import net.minecraft.ClickType;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.PickaxeItem;
import net.minecraft.SwordItem;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.Vec3;
import net.minecraft.Packet;
import net.minecraft.ServerboundPickItemPacket;
import net.minecraft.ServerboundPlayerActionPacket;

public class Offhand
extends Module {
    private final EnumSetting<OffhandItem> item = this.add(new EnumSetting<OffhandItem>("Item", OffhandItem.Totem));
    private final BooleanSetting safe = this.add(new BooleanSetting("Safe", true).setParent());
    private final SliderSetting safeHealth = this.add(new SliderSetting("Health", 16.0, 0.0, 36.0, 0.1, this.safe::isOpen));
    private final BooleanSetting lethalCrystal = this.add(new BooleanSetting("LethalCrystal", true, this.safe::isOpen));
    private final BooleanSetting gapSwitch = this.add(new BooleanSetting("GapSwitch", true).setParent());
    private final BooleanSetting always = this.add(new BooleanSetting("Always", false, this.gapSwitch::isOpen));
    private final BooleanSetting gapOnTotem = this.add(new BooleanSetting("Gap-Totem", false, this.gapSwitch::isOpen));
    private final BooleanSetting gapOnSword = this.add(new BooleanSetting("Gap-Sword", true, this.gapSwitch::isOpen));
    private final BooleanSetting gapOnPick = this.add(new BooleanSetting("Gap-Pickaxe", false, this.gapSwitch::isOpen));
    private final BooleanSetting mainHandTotem = this.add(new BooleanSetting("MainHandTotem", false).setParent());
    private final SliderSetting slot = this.add(new SliderSetting("Slot", 1.0, 1.0, 9.0, 1.0, this.mainHandTotem::isOpen));
    private final BooleanSetting forceUpdate = this.add(new BooleanSetting("ForceUpdate", false, this.mainHandTotem::isOpen));
    private final BooleanSetting withOffhand = this.add(new BooleanSetting("WithOffhand", false, this.mainHandTotem::isOpen));
    private final EnumSetting<SwapMode> swapMode = this.add(new EnumSetting<SwapMode>("SwapMode", SwapMode.OffhandSwap));
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 50.0, 0.0, 500.0, 1.0));
    private final EnumSetting<Timing> timing = this.add(new EnumSetting<Timing>("Timing", Timing.All));
    private final Timer timer = new Timer();

    public Offhand() {
        super("Offhand", Module.Category.Combat);
        this.setChinese("\u526f\u624b\u7269\u54c1");
    }

    @EventListener
    public void totem(TotemEvent event) {
        if (event.getPlayer() == Offhand.mc.player) {
            if (Offhand.mc.player.getMainHandItem().method_7909() == Items.TOTEM_OF_UNDYING) {
                Offhand.mc.player.getInventory().method_5441(0);
            } else if (Offhand.mc.player.getOffhandItem().method_7909() == Items.TOTEM_OF_UNDYING) {
                Offhand.mc.player.getInventory().offhand.set(0, (Object)ItemStack.EMPTY);
            }
        }
    }

    private boolean lethalCrystal() {
        if (!this.lethalCrystal.getValue()) {
            return false;
        }
        for (Entity entity : Aevum.THREAD.getEntities()) {
            Vec3 vec3d;
            if (!(entity instanceof EndCrystal) || !(Offhand.mc.player.method_5739(entity) <= 12.0f) || !(AutoCrystal.INSTANCE.calculateDamage(vec3d = new Vec3(entity.getX(), entity.getY(), entity.getZ()), (Player)Offhand.mc.player, (Player)Offhand.mc.player) >= EntityUtil.getHealth((Entity)Offhand.mc.player))) continue;
            return true;
        }
        return false;
    }

    /*
     * Enabled aggressive block sorting
     */
    @EventListener
    public void onTick(ClientTickEvent event) {
        block22: {
            int hotBarSlot;
            boolean switchMainHandTotem;
            block23: {
                boolean unsafe;
                block21: {
                    int totemSlot;
                    if (Offhand.nullCheck()) {
                        return;
                    }
                    if (this.timing.is(Timing.Pre) && event.isPost() || this.timing.is(Timing.Post) && event.isPre()) {
                        return;
                    }
                    if (!this.timer.passed(this.delay.getValueInt())) {
                        return;
                    }
                    if (!EntityUtil.inInventory()) {
                        return;
                    }
                    switchMainHandTotem = Offhand.mc.player.getOffhandItem().method_7909() != Items.TOTEM_OF_UNDYING || this.withOffhand.getValue();
                    unsafe = (double)EntityUtil.getHealth((Entity)Offhand.mc.player) < this.safeHealth.getValue() || this.lethalCrystal();
                    boolean bl = unsafe;
                    if (this.mainHandTotem.getValue() && (totemSlot = InventoryUtil.findItemInventorySlot(Items.TOTEM_OF_UNDYING)) != -1 && Offhand.mc.player.getInventory().method_5438(this.slot.getValueInt() - 1).method_7909() != Items.TOTEM_OF_UNDYING) {
                        switch (this.swapMode.getValue().ordinal()) {
                            case 0: {
                                Offhand.mc.gameMode.method_2906(Offhand.mc.player.containerMenu.containerId, totemSlot, 0, ClickType.PICKUP, (Player)Offhand.mc.player);
                                Offhand.mc.gameMode.method_2906(Offhand.mc.player.containerMenu.containerId, this.slot.getValueInt() - 1 + 36, 0, ClickType.PICKUP, (Player)Offhand.mc.player);
                                Offhand.mc.gameMode.method_2906(Offhand.mc.player.containerMenu.containerId, totemSlot, 0, ClickType.PICKUP, (Player)Offhand.mc.player);
                                EntityUtil.syncInventory();
                                break;
                            }
                            case 1: {
                                Offhand.mc.gameMode.method_2906(Offhand.mc.player.containerMenu.containerId, totemSlot, this.slot.getValueInt() - 1, ClickType.SWAP, (Player)Offhand.mc.player);
                                EntityUtil.syncInventory();
                                break;
                            }
                            case 2: {
                                int old = Offhand.mc.player.getInventory().selected;
                                InventoryUtil.switchToSlot(this.slot.getValueInt() - 1);
                                mc.getConnection().method_52787((Packet)new ServerboundPickItemPacket(totemSlot));
                                InventoryUtil.switchToSlot(old);
                                break;
                            }
                        }
                        if (switchMainHandTotem && (!this.safe.getValue() || unsafe) && (this.slot.getValueInt() - 1 != Offhand.mc.player.getInventory().selected || this.forceUpdate.getValue())) {
                            InventoryUtil.switchToSlot(this.slot.getValueInt() - 1);
                        }
                        this.timer.reset();
                        return;
                    }
                    if (this.safe.getValue()) break block21;
                    if (!this.mainHandTotem.getValue()) break block22;
                    break block23;
                }
                if (!unsafe) break block22;
                if (this.mainHandTotem.getValue() && switchMainHandTotem) {
                    hotBarSlot = InventoryUtil.findItem(Items.TOTEM_OF_UNDYING);
                    if (hotBarSlot != -1 && (hotBarSlot != Offhand.mc.player.getInventory().selected || this.forceUpdate.getValue())) {
                        InventoryUtil.switchToSlot(hotBarSlot);
                    }
                    break block22;
                } else {
                    this.swap(Items.TOTEM_OF_UNDYING);
                    this.timer.reset();
                    return;
                }
            }
            if (switchMainHandTotem && (hotBarSlot = InventoryUtil.findItem(Items.TOTEM_OF_UNDYING)) != -1 && (hotBarSlot != Offhand.mc.player.getInventory().selected || this.forceUpdate.getValue())) {
                InventoryUtil.switchToSlot(hotBarSlot);
            }
        }
        if ((this.gapOnSword.getValue() && Offhand.mc.player.getMainHandItem().method_7909() instanceof SwordItem || this.always.getValue() && Offhand.mc.player.getMainHandItem().method_7909() != Items.GOLDEN_APPLE && Offhand.mc.player.getMainHandItem().method_7909() != Items.ENCHANTED_GOLDEN_APPLE || this.gapOnPick.getValue() && Offhand.mc.player.getMainHandItem().method_7909() instanceof PickaxeItem || this.gapOnTotem.getValue() && Offhand.mc.player.getMainHandItem().method_7909() == Items.TOTEM_OF_UNDYING) && Offhand.mc.options.keyUse.isDown() && this.gapSwitch.getValue()) {
            this.swap(Items.GOLDEN_APPLE);
            this.timer.reset();
            return;
        }
        EnumSetting<OffhandItem> item = this.item;
        OffhandItem i = item.getValue();
        if (i == OffhandItem.Shield) {
            this.swap(Items.SHIELD);
            this.timer.reset();
            return;
        }
        if (i == OffhandItem.Chorus) {
            this.swap(Items.CHORUS_FRUIT);
            this.timer.reset();
            return;
        }
        if (i == OffhandItem.Crystal) {
            this.swap(Items.END_CRYSTAL);
            this.timer.reset();
            return;
        }
        if (i == OffhandItem.Totem) {
            this.swap(Items.TOTEM_OF_UNDYING);
            this.timer.reset();
            return;
        }
        if (i == OffhandItem.Gapple) {
            this.swap(Items.GOLDEN_APPLE);
            this.timer.reset();
        }
    }

    private void swap(Item item) {
        int itemSlot = item == Items.GOLDEN_APPLE ? this.getGAppleSlot() : this.findItemInventorySlot(item);
        int n = itemSlot;
        if (itemSlot == -1) {
            return;
        }
        switch (this.swapMode.getValue().ordinal()) {
            case 1: {
                Offhand.mc.gameMode.method_2906(Offhand.mc.player.containerMenu.containerId, itemSlot, 40, ClickType.SWAP, (Player)Offhand.mc.player);
                EntityUtil.syncInventory();
                break;
            }
            case 2: {
                mc.getConnection().method_52787((Packet)new ServerboundPickItemPacket(itemSlot));
                mc.getConnection().method_52787((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN, 0));
                mc.getConnection().method_52787((Packet)new ServerboundPickItemPacket(itemSlot));
                break;
            }
            case 0: {
                Offhand.mc.gameMode.method_2906(Offhand.mc.player.containerMenu.containerId, itemSlot, 0, ClickType.PICKUP, (Player)Offhand.mc.player);
                Offhand.mc.gameMode.method_2906(Offhand.mc.player.containerMenu.containerId, 45, 0, ClickType.PICKUP, (Player)Offhand.mc.player);
                Offhand.mc.gameMode.method_2906(Offhand.mc.player.containerMenu.containerId, itemSlot, 0, ClickType.PICKUP, (Player)Offhand.mc.player);
                EntityUtil.syncInventory();
            }
        }
    }

    private int getGAppleSlot() {
        return this.findItemInventorySlot(Items.ENCHANTED_GOLDEN_APPLE) != -1 ? this.findItemInventorySlot(Items.ENCHANTED_GOLDEN_APPLE) : this.findItemInventorySlot(Items.GOLDEN_APPLE);
    }

    @Override
    public String getInfo() {
        return this.item.getValue().name();
    }

    public int findItemInventorySlot(Item item) {
        if (Offhand.mc.player.getOffhandItem().method_7909() == Items.GOLDEN_APPLE && item == Items.GOLDEN_APPLE) {
            return -1;
        }
        if (Offhand.mc.player.getOffhandItem().method_7909() == Items.ENCHANTED_GOLDEN_APPLE && (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE)) {
            return -1;
        }
        if (item == Offhand.mc.player.getOffhandItem().method_7909()) {
            return -1;
        }
        switch (this.swapMode.getValue().ordinal()) {
            case 2: {
                ItemStack s;
                int i;
                for (i = 9; i < Offhand.mc.player.getInventory().method_5439() + 1; ++i) {
                    s = Offhand.mc.player.getInventory().method_5438(i);
                    if (s.method_7909() != item) continue;
                    return i;
                }
                for (i = 0; i < 9; ++i) {
                    s = Offhand.mc.player.getInventory().method_5438(i);
                    if (s.method_7909() != item) continue;
                    return i;
                }
                break;
            }
            case 0: 
            case 1: {
                for (int i = 44; i >= 0; --i) {
                    ItemStack stack = Offhand.mc.player.getInventory().method_5438(i);
                    if (stack.method_7909() != item) continue;
                    return i < 9 ? i + 36 : i;
                }
                break;
            }
        }
        return -1;
    }

    public static enum OffhandItem {
        None,
        Totem,
        Crystal,
        Gapple,
        Shield,
        Chorus;

    }

    public static enum SwapMode {
        ClickSlot,
        OffhandSwap,
        Pick;

    }
}

