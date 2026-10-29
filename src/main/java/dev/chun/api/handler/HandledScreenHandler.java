package dev.chun.api.handler;

import dev.chun.Aevum;
import dev.chun.mod.modules.impl.combat.AutoRegear;
import dev.chun.mod.modules.impl.misc.ShulkerViewer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

public final class HandledScreenHandler {
   private HandledScreenHandler() {
   }

   public static boolean onMouseClicked(Slot focusedSlot, ItemStack[] items, MinecraftClient mc) {
      if (focusedSlot == null) {
         return false;
      } else {
         ItemStack stack = focusedSlot.getStack();
         if (stack.isEmpty()) {
            return false;
         } else if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            return false;
         } else if (!ShulkerViewer.INSTANCE.isOn()) {
            return false;
         } else {
            return !ShulkerViewer.hasItems(stack) && (stack.getItem() != Items.ENDER_CHEST || !Aevum.PLAYER.known)
               ? false
               : ShulkerViewer.openContainer(stack, items, false);
         }
      }
   }

   public static boolean onRenderHead(Object screen) {
      if (!(screen instanceof ShulkerBoxScreen)) {
         return false;
      } else {
         return AutoRegear.INSTANCE != null && AutoRegear.INSTANCE.isOn()
            ? AutoRegear.INSTANCE.silentDisplay.getValue() && AutoRegear.INSTANCE.silentScreenActive
            : false;
      }
   }

   public static void onRenderReturn(DrawContext context, int mouseX, int mouseY, Slot focusedSlot, MinecraftClient mc) {
      if (ShulkerViewer.INSTANCE != null && ShulkerViewer.INSTANCE.isOn()) {
         if (ShulkerViewer.INSTANCE.toolTips.getValue()) {
            if (focusedSlot != null && !focusedSlot.getStack().isEmpty()) {
               if (mc.player.playerScreenHandler.getCursorStack().isEmpty()) {
                  ItemStack stack = focusedSlot.getStack();
                  if (ShulkerViewer.hasItems(stack) || stack.getItem() == Items.ENDER_CHEST && Aevum.PLAYER.known) {
                     ShulkerViewer.renderShulkerToolTip(context, mouseX, mouseY, stack);
                  }
               }
            }
         }
      }
   }
}
