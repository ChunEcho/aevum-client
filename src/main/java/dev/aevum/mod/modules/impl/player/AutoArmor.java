/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.EquipmentSlot
 *  net.minecraft.Player
 *  net.minecraft.ClickType
 *  net.minecraft.ArmorItem
 *  net.minecraft.ElytraItem
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.EnchantmentHelper
 *  net.minecraft.Enchantments
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundPickItemPacket
 *  net.minecraft.ServerboundUseItemPacket
 *  net.minecraft.Holder
 *  net.minecraft.ItemEnchantments
 */
package dev.aevum.mod.modules.impl.player;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.api.utils.player.MovementUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.movement.ElytraFly;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.InteractionHand;
import net.minecraft.EquipmentSlot;
import net.minecraft.Player;
import net.minecraft.ClickType;
import net.minecraft.ArmorItem;
import net.minecraft.ElytraItem;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.EnchantmentHelper;
import net.minecraft.Enchantments;
import net.minecraft.Packet;
import net.minecraft.ServerboundPickItemPacket;
import net.minecraft.ServerboundUseItemPacket;
import net.minecraft.Holder;
import net.minecraft.ItemEnchantments;

public class AutoArmor
extends Module {
    public static AutoArmor INSTANCE;
    private final EnumSetting<EnchantPriority> head = this.add(new EnumSetting<EnchantPriority>("Head", EnchantPriority.Protection));
    private final EnumSetting<EnchantPriority> body = this.add(new EnumSetting<EnchantPriority>("Body", EnchantPriority.Protection));
    private final EnumSetting<EnchantPriority> tights = this.add(new EnumSetting<EnchantPriority>("Tights", EnchantPriority.Protection));
    private final EnumSetting<EnchantPriority> feet = this.add(new EnumSetting<EnchantPriority>("Feet", EnchantPriority.Protection));
    private final BooleanSetting ignoreCurse = this.add(new BooleanSetting("IgnoreCurse", true));
    private final BooleanSetting noMove = this.add(new BooleanSetting("NoMove", false));
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 3.0, 0.0, 10.0, 1.0));
    public final BooleanSetting autoElytra = this.add(new BooleanSetting("AutoElytra", true));
    private final EnumSetting<HotbarSwapMode> hotbarSwap = this.add(new EnumSetting<HotbarSwapMode>("HotbarSwap", HotbarSwapMode.Swap));
    private final EnumSetting<InventorySwapMode> inventorySwap = this.add(new EnumSetting<InventorySwapMode>("InventorySwap", InventorySwapMode.ClickSlot));
    private int tickDelay = 0;

    public AutoArmor() {
        super("AutoArmor", Module.Category.Player);
        this.setChinese("\u81ea\u52a8\u7a7f\u7532");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (!EntityUtil.inInventory()) {
            return;
        }
        if (AutoArmor.mc.player.inventoryMenu != AutoArmor.mc.player.containerMenu) {
            return;
        }
        if (MovementUtil.isMoving() && this.noMove.getValue()) {
            return;
        }
        if (this.tickDelay > 0) {
            --this.tickDelay;
            return;
        }
        this.tickDelay = this.delay.getValueInt();
        HashMap<EquipmentSlot, int[]> armorMap = new HashMap<EquipmentSlot, int[]>(4);
        armorMap.put(EquipmentSlot.FEET, new int[]{36, this.getProtection(AutoArmor.mc.player.getInventory().method_5438(36)), -1, -1});
        armorMap.put(EquipmentSlot.LEGS, new int[]{37, this.getProtection(AutoArmor.mc.player.getInventory().method_5438(37)), -1, -1});
        armorMap.put(EquipmentSlot.CHEST, new int[]{38, this.getProtection(AutoArmor.mc.player.getInventory().method_5438(38)), -1, -1});
        armorMap.put(EquipmentSlot.HEAD, new int[]{39, this.getProtection(AutoArmor.mc.player.getInventory().method_5438(39)), -1, -1});
        for (int s = 0; s < 36; ++s) {
            if (!(AutoArmor.mc.player.getInventory().method_5438(s).method_7909() instanceof ArmorItem) && AutoArmor.mc.player.getInventory().method_5438(s).method_7909() != Items.ELYTRA || AutoArmor.mc.player.getInventory().method_5438(s).method_7909() == Items.ELYTRA && (ElytraFly.INSTANCE.isOff() && this.autoElytra.getValue() || ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.packet.getValue())) continue;
            int protection = this.getProtection(AutoArmor.mc.player.getInventory().method_5438(s));
            EquipmentSlot slot = AutoArmor.mc.player.getInventory().method_5438(s).method_7909() instanceof ElytraItem ? EquipmentSlot.CHEST : ((ArmorItem)AutoArmor.mc.player.getInventory().method_5438(s).method_7909()).method_7685();
            for (Map.Entry e : armorMap.entrySet()) {
                if (this.autoElytra.getValue() && ElytraFly.INSTANCE.isOn() && e.getKey() == EquipmentSlot.CHEST) {
                    if (!AutoArmor.mc.player.getInventory().method_5438(38).method_7960() && AutoArmor.mc.player.getInventory().method_5438(38).method_7909() instanceof ElytraItem && ElytraItem.isFlyEnabled((ItemStack)AutoArmor.mc.player.getInventory().method_5438(38)) || ((int[])e.getValue())[2] != -1 && !AutoArmor.mc.player.getInventory().method_5438(((int[])e.getValue())[2]).method_7960() && AutoArmor.mc.player.getInventory().method_5438(((int[])e.getValue())[2]).method_7909() instanceof ElytraItem && ElytraItem.isFlyEnabled((ItemStack)AutoArmor.mc.player.getInventory().method_5438(((int[])e.getValue())[2])) || AutoArmor.mc.player.getInventory().method_5438(s).method_7960() || !(AutoArmor.mc.player.getInventory().method_5438(s).method_7909() instanceof ElytraItem) || !ElytraItem.isFlyEnabled((ItemStack)AutoArmor.mc.player.getInventory().method_5438(s))) continue;
                    ((int[])e.getValue())[2] = s;
                    continue;
                }
                if (protection <= 0 || e.getKey() != slot || protection <= ((int[])e.getValue())[1] || protection <= ((int[])e.getValue())[3]) continue;
                ((int[])e.getValue())[2] = s;
                ((int[])e.getValue())[3] = protection;
            }
        }
        for (Map.Entry equipmentSlotEntry : armorMap.entrySet()) {
            if (((int[])equipmentSlotEntry.getValue())[2] == -1) continue;
            if (((int[])equipmentSlotEntry.getValue())[2] < 9) {
                switch (this.hotbarSwap.getValue().ordinal()) {
                    case 0: {
                        int armorSlot = 44 - ((int[])equipmentSlotEntry.getValue())[0];
                        AutoArmor.mc.gameMode.method_2906(AutoArmor.mc.player.containerMenu.containerId, armorSlot, ((int[])equipmentSlotEntry.getValue())[2], ClickType.SWAP, (Player)AutoArmor.mc.player);
                        EntityUtil.syncInventory();
                        break;
                    }
                    case 1: {
                        int old = AutoArmor.mc.player.getInventory().selected;
                        InventoryUtil.switchToSlot(((int[])equipmentSlotEntry.getValue())[2]);
                        AutoArmor.sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, id, Aevum.ROTATION.getLastYaw(), Aevum.ROTATION.getLastPitch()));
                        InventoryUtil.switchToSlot(old);
                    }
                }
            } else if (AutoArmor.mc.player.inventoryMenu == AutoArmor.mc.player.containerMenu) {
                int armorSlot = 44 - ((int[])equipmentSlotEntry.getValue())[0];
                int newArmorSlot = ((int[])equipmentSlotEntry.getValue())[2];
                switch (this.inventorySwap.getValue().ordinal()) {
                    case 1: {
                        mc.getConnection().method_52787((Packet)new ServerboundPickItemPacket(newArmorSlot));
                        AutoArmor.sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, id, Aevum.ROTATION.getLastYaw(), Aevum.ROTATION.getLastPitch()));
                        mc.getConnection().method_52787((Packet)new ServerboundPickItemPacket(newArmorSlot));
                        break;
                    }
                    case 0: {
                        AutoArmor.mc.gameMode.method_2906(AutoArmor.mc.player.containerMenu.containerId, newArmorSlot, 0, ClickType.PICKUP, (Player)AutoArmor.mc.player);
                        AutoArmor.mc.gameMode.method_2906(AutoArmor.mc.player.containerMenu.containerId, armorSlot, 0, ClickType.PICKUP, (Player)AutoArmor.mc.player);
                        if (((int[])equipmentSlotEntry.getValue())[1] == -1) break;
                        AutoArmor.mc.gameMode.method_2906(AutoArmor.mc.player.containerMenu.containerId, newArmorSlot, 0, ClickType.PICKUP, (Player)AutoArmor.mc.player);
                    }
                }
                EntityUtil.syncInventory();
            }
            return;
        }
    }

    private int getProtection(ItemStack is) {
        if (is.method_7909() instanceof ArmorItem || is.method_7909() == Items.ELYTRA) {
            EquipmentSlot slot;
            int prot = 0;
            Item item = is.method_7909();
            if (item instanceof ArmorItem) {
                ArmorItem ai = (ArmorItem)item;
                slot = ai.method_7685();
            } else {
                slot = EquipmentSlot.BODY;
            }
            if (is.method_7909() instanceof ElytraItem) {
                if (!ElytraItem.isFlyEnabled((ItemStack)is)) {
                    return 0;
                }
                prot = 1;
            }
            int blastMultiplier = 1;
            int protectionMultiplier = 1;
            switch (slot) {
                case HEAD: {
                    if (this.head.is(EnchantPriority.Protection)) {
                        protectionMultiplier *= 2;
                        break;
                    }
                    blastMultiplier *= 2;
                    break;
                }
                case BODY: {
                    if (this.body.is(EnchantPriority.Protection)) {
                        protectionMultiplier *= 2;
                        break;
                    }
                    blastMultiplier *= 2;
                    break;
                }
                case LEGS: {
                    if (this.tights.is(EnchantPriority.Protection)) {
                        protectionMultiplier *= 2;
                        break;
                    }
                    blastMultiplier *= 2;
                    break;
                }
                case FEET: {
                    if (this.feet.is(EnchantPriority.Protection)) {
                        protectionMultiplier *= 2;
                        break;
                    }
                    blastMultiplier *= 2;
                }
            }
            if (is.isEnchanted()) {
                ItemEnchantments enchants = EnchantmentHelper.method_57532((ItemStack)is);
                if (enchants.method_57534().contains(AutoArmor.mc.level.method_30349().method_30530(Enchantments.PROTECTION.method_58273()).method_40264(Enchantments.PROTECTION).get())) {
                    prot += enchants.method_57536((Holder)AutoArmor.mc.level.method_30349().method_30530(Enchantments.PROTECTION.method_58273()).method_40264(Enchantments.PROTECTION).get()) * protectionMultiplier;
                }
                if (enchants.method_57534().contains(AutoArmor.mc.level.method_30349().method_30530(Enchantments.BLAST_PROTECTION.method_58273()).method_40264(Enchantments.BLAST_PROTECTION).get())) {
                    prot += enchants.method_57536((Holder)AutoArmor.mc.level.method_30349().method_30530(Enchantments.BLAST_PROTECTION.method_58273()).method_40264(Enchantments.BLAST_PROTECTION).get()) * blastMultiplier;
                }
                if (enchants.method_57534().contains(AutoArmor.mc.level.method_30349().method_30530(Enchantments.BLAST_PROTECTION.method_58273()).method_40264(Enchantments.BINDING_CURSE).get()) && this.ignoreCurse.getValue()) {
                    prot = -999;
                }
            }
            return (is.method_7909() instanceof ArmorItem ? ((ArmorItem)is.method_7909()).getDefense() : 0) + prot;
        }
        if (!is.method_7960()) {
            return 0;
        }
        return -1;
    }

    private static enum EnchantPriority {
        Blast,
        Protection;

    }

    public static enum HotbarSwapMode {
        Swap,
        Switch;

    }

    public static enum InventorySwapMode {
        ClickSlot,
        Pick;

    }
}

