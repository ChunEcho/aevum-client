/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Slot
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Minecraft
 *  net.minecraft.GuiGraphics
 *  net.minecraft.ShulkerBoxScreen
 */
package dev.aevum.api.handler;

import dev.aevum.Aevum;
import dev.aevum.mod.modules.impl.combat.AutoRegear;
import dev.aevum.mod.modules.impl.misc.ShulkerViewer;
import net.minecraft.Slot;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.Minecraft;
import net.minecraft.GuiGraphics;
import net.minecraft.ShulkerBoxScreen;

public final class HandledScreenHandler {
    private HandledScreenHandler() {
    }

    public static boolean onMouseClicked(Slot focusedSlot, ItemStack[] items, Minecraft mc) {
        if (focusedSlot == null) {
            return false;
        }
        ItemStack stack = focusedSlot.getItem();
        if (stack.method_7960()) {
            return false;
        }
        if (!mc.player.containerMenu.getCarried().method_7960()) {
            return false;
        }
        if (!ShulkerViewer.INSTANCE.isOn()) {
            return false;
        }
        if (ShulkerViewer.hasItems(stack) || stack.method_7909() == Items.ENDER_CHEST && Aevum.PLAYER.known) {
            return ShulkerViewer.openContainer(stack, items, false);
        }
        return false;
    }

    public static boolean onRenderHead(Object screen) {
        if (!(screen instanceof ShulkerBoxScreen)) {
            return false;
        }
        if (AutoRegear.INSTANCE == null || !AutoRegear.INSTANCE.isOn()) {
            return false;
        }
        return AutoRegear.INSTANCE.silentDisplay.getValue() && AutoRegear.INSTANCE.silentScreenActive;
    }

    public static void onRenderReturn(GuiGraphics context, int mouseX, int mouseY, Slot focusedSlot, Minecraft mc) {
        if (ShulkerViewer.INSTANCE == null || !ShulkerViewer.INSTANCE.isOn()) {
            return;
        }
        if (!ShulkerViewer.INSTANCE.toolTips.getValue()) {
            return;
        }
        if (focusedSlot == null || focusedSlot.getItem().method_7960()) {
            return;
        }
        if (!mc.player.inventoryMenu.getCarried().method_7960()) {
            return;
        }
        ItemStack stack = focusedSlot.getItem();
        if (ShulkerViewer.hasItems(stack) || stack.method_7909() == Items.ENDER_CHEST && Aevum.PLAYER.known) {
            ShulkerViewer.renderShulkerToolTip(context, mouseX, mouseY, stack);
        }
    }
}

