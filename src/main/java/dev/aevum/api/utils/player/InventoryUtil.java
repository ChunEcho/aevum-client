/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.MobEffect
 *  net.minecraft.MobEffectInstance
 *  net.minecraft.Player
 *  net.minecraft.ClickType
 *  net.minecraft.BlockItem
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.PotionContents
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundPickItemPacket
 *  net.minecraft.ServerboundSetCarriedItemPacket
 *  net.minecraft.DataComponents
 */
package dev.aevum.api.utils.player;

import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.impl.client.AntiCheat;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.MobEffect;
import net.minecraft.MobEffectInstance;
import net.minecraft.Player;
import net.minecraft.ClickType;
import net.minecraft.BlockItem;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.PotionContents;
import net.minecraft.Blocks;
import net.minecraft.Block;
import net.minecraft.Packet;
import net.minecraft.ServerboundPickItemPacket;
import net.minecraft.ServerboundSetCarriedItemPacket;
import net.minecraft.DataComponents;

public class InventoryUtil
implements Wrapper {
    static int lastSlot = -1;
    static int lastSelect = -1;

    public static void inventorySwap(int slot, int selectedSlot) {
        if (slot == lastSlot) {
            InventoryUtil.switchToSlot(lastSelect);
            lastSlot = -1;
            lastSelect = -1;
            return;
        }
        if (slot - 36 == selectedSlot) {
            return;
        }
        if (!EntityUtil.inInventory()) {
            return;
        }
        if (AntiCheat.INSTANCE.invSwapBypass.getValue()) {
            if (slot - 36 >= 0) {
                lastSlot = slot;
                lastSelect = selectedSlot;
                InventoryUtil.switchToSlot(slot - 36);
                return;
            }
            mc.getConnection().method_52787((Packet)new ServerboundPickItemPacket(slot));
        } else {
            InventoryUtil.mc.gameMode.method_2906(InventoryUtil.mc.player.containerMenu.containerId, slot, selectedSlot, ClickType.SWAP, (Player)InventoryUtil.mc.player);
            InventoryUtil.mc.player.getInventory().method_7381();
        }
    }

    public static void switchToSlot(int slot) {
        InventoryUtil.mc.player.getInventory().selected = slot;
        mc.getConnection().method_52787((Packet)new ServerboundSetCarriedItemPacket(slot));
    }

    public static int findItem(Item input) {
        for (int i = 0; i < 9; ++i) {
            Item item = InventoryUtil.mc.player.getInventory().method_5438(i).method_7909();
            if (Item.method_7880((Item)item) != Item.method_7880((Item)input)) continue;
            return i;
        }
        return -1;
    }

    public static int getFood() {
        for (int i = 0; i < 9; ++i) {
            if (!InventoryUtil.mc.player.getInventory().method_5438(i).method_57353().method_57832(DataComponents.FOOD)) continue;
            return i;
        }
        return -1;
    }

    public static int getPotionCount(MobEffect targetEffect) {
        int count = 0;
        for (int i = 35; i >= 0; --i) {
            ItemStack itemStack = InventoryUtil.mc.player.getInventory().method_5438(i);
            if (Item.method_7880((Item)itemStack.method_7909()) != Item.method_7880((Item)Items.SPLASH_POTION)) continue;
            PotionContents potionContentsComponent = (PotionContents)itemStack.method_57825(DataComponents.POTION_CONTENTS, (Object)PotionContents.EMPTY);
            for (MobEffectInstance effect : potionContentsComponent.method_57397()) {
                if (effect.method_5579().comp_349() != targetEffect) continue;
                count += itemStack.getCount();
            }
        }
        return count;
    }

    public static int getItemCount(Class<?> clazz) {
        int count = 0;
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            if (!(entry.getValue().method_7909() instanceof BlockItem) || !clazz.isInstance(((BlockItem)entry.getValue().method_7909()).getBlock())) continue;
            count += entry.getValue().getCount();
        }
        return count;
    }

    public static int getItemCount(Item item) {
        int count = 0;
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            if (entry.getValue().method_7909() != item) continue;
            count += entry.getValue().getCount();
        }
        if (InventoryUtil.mc.player.getOffhandItem().method_7909() == item) {
            count += InventoryUtil.mc.player.getOffhandItem().getCount();
        }
        return count;
    }

    public static int findClass(Class<?> clazz) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = InventoryUtil.mc.player.getInventory().method_5438(i);
            if (stack == ItemStack.EMPTY) continue;
            if (clazz.isInstance(stack.method_7909())) {
                return i;
            }
            if (!(stack.method_7909() instanceof BlockItem) || !clazz.isInstance(((BlockItem)stack.method_7909()).getBlock())) continue;
            return i;
        }
        return -1;
    }

    public static int findClassInventorySlot(Class<?> clazz) {
        if (AntiCheat.INSTANCE.priorHotbar.getValue()) {
            for (int i = 0; i < 36; ++i) {
                ItemStack stack = InventoryUtil.mc.player.getInventory().method_5438(i);
                if (stack == ItemStack.EMPTY) continue;
                if (clazz.isInstance(stack.method_7909())) {
                    return i < 9 ? i + 36 : i;
                }
                if (!(stack.method_7909() instanceof BlockItem) || !clazz.isInstance(((BlockItem)stack.method_7909()).getBlock())) continue;
                return i < 9 ? i + 36 : i;
            }
        } else {
            for (int i = 35; i >= 0; --i) {
                ItemStack stack = InventoryUtil.mc.player.getInventory().method_5438(i);
                if (stack == ItemStack.EMPTY) continue;
                if (clazz.isInstance(stack.method_7909())) {
                    return i < 9 ? i + 36 : i;
                }
                if (!(stack.method_7909() instanceof BlockItem) || !clazz.isInstance(((BlockItem)stack.method_7909()).getBlock())) continue;
                return i < 9 ? i + 36 : i;
            }
        }
        return -1;
    }

    public static int findBlock(Block blockIn) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = InventoryUtil.mc.player.getInventory().method_5438(i);
            if (stack == ItemStack.EMPTY || !(stack.method_7909() instanceof BlockItem) || ((BlockItem)stack.method_7909()).getBlock() != blockIn) continue;
            return i;
        }
        return -1;
    }

    public static int findUnBlock() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = InventoryUtil.mc.player.getInventory().method_5438(i);
            if (stack.method_7909() instanceof BlockItem) continue;
            return i;
        }
        return -1;
    }

    public static int findBlock() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = InventoryUtil.mc.player.getInventory().method_5438(i);
            if (!(stack.method_7909() instanceof BlockItem) || BlockUtil.isClickable(Block.method_9503((Item)stack.method_7909())) || ((BlockItem)stack.method_7909()).getBlock() == Blocks.COBWEB) continue;
            return i;
        }
        return -1;
    }

    public static int findBlockInventorySlot(Block block) {
        return InventoryUtil.findItemInventorySlot(block.method_8389());
    }

    public static int findItemInventorySlot(Item item) {
        if (AntiCheat.INSTANCE.priorHotbar.getValue()) {
            return InventoryUtil.findItemInventorySlotFromZero(item.method_8389());
        }
        for (int i = 35; i >= 0; --i) {
            ItemStack stack = InventoryUtil.mc.player.getInventory().method_5438(i);
            if (stack.method_7909() != item) continue;
            return i < 9 ? i + 36 : i;
        }
        return -1;
    }

    public static int findItemInventorySlotFromZero(Item item) {
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = InventoryUtil.mc.player.getInventory().method_5438(i);
            if (stack.method_7909() != item) continue;
            return i < 9 ? i + 36 : i;
        }
        return -1;
    }

    public static Map<Integer, ItemStack> getInventoryAndHotbarSlots() {
        HashMap<Integer, ItemStack> fullInventorySlots = new HashMap<Integer, ItemStack>();
        for (int current = 0; current <= 35; ++current) {
            fullInventorySlots.put(current, InventoryUtil.mc.player.getInventory().method_5438(current));
        }
        return fullInventorySlots;
    }
}

