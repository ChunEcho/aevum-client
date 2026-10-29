package dev.chun.mod.modules.impl.combat;

import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.ClientTickEvent;
import dev.chun.api.utils.math.Timer;
import dev.chun.api.utils.player.EntityUtil;
import dev.chun.api.utils.player.InventoryUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.enums.Timing;
import dev.chun.mod.modules.settings.impl.EnumSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public class MainHand extends Module {
   private final EnumSetting<Offhand.OffhandItem> item = this.add(new EnumSetting<>("Item", Offhand.OffhandItem.Totem));
   private final SliderSetting slot = this.add(new SliderSetting("Slot", 2.0, 1.0, 9.0, 1.0));
   private final EnumSetting<Offhand.SwapMode> swapMode = this.add(new EnumSetting<>("SwapMode", Offhand.SwapMode.OffhandSwap));
   private final SliderSetting delay = this.add(new SliderSetting("Delay", 50.0, 0.0, 500.0, 1.0));
   private final EnumSetting<Timing> timing = this.add(new EnumSetting<>("Timing", Timing.All));
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
      if (!nullCheck()) {
         if ((!this.timing.is(Timing.Pre) || !clientTickEvent.isPost()) && (!this.timing.is(Timing.Post) || !clientTickEvent.isPre())) {
            Offhand.OffhandItem offhandItem = this.item.getValue();
            if (offhandItem != Offhand.OffhandItem.None) {
               if (this.timer.passed((long)this.delay.getValueInt())) {
                  if (EntityUtil.inInventory()) {
                     int n = this.slot.getValueInt() - 1;
                     if (!this.isFilled(mc.player.getInventory().getStack(n).getItem(), offhandItem)) {
                        int itemInventorySlot = this.findItemInventorySlot(offhandItem, n);
                        if (itemInventorySlot != -1) {
                           this.swap(itemInventorySlot, n);
                           this.timer.reset();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isFilled(Item Item, Offhand.OffhandItem offhandItem) {
      switch (offhandItem) {
         case Totem:
            return Item == Items.TOTEM_OF_UNDYING;
         case Crystal:
            return Item == Items.END_CRYSTAL;
         case Shield:
            return Item == Items.SHIELD;
         case Chorus:
            return Item == Items.CHORUS_FRUIT;
         case Gapple:
            return Item == Items.GOLDEN_APPLE || Item == Items.ENCHANTED_GOLDEN_APPLE;
         default:
            return false;
      }
   }

   private Item itemOf(Offhand.OffhandItem offhandItem) {
      switch (offhandItem) {
         case Totem:
            return Items.TOTEM_OF_UNDYING;
         case Crystal:
            return Items.END_CRYSTAL;
         case Shield:
            return Items.SHIELD;
         case Chorus:
            return Items.CHORUS_FRUIT;
         default:
            return Items.GOLDEN_APPLE;
      }
   }

   private int findItemInventorySlot(Offhand.OffhandItem offhandItem, int n) {
      if (offhandItem != Offhand.OffhandItem.Gapple) {
         return this.scanFor(this.itemOf(offhandItem), n);
      } else {
         int scan = this.scanFor(Items.ENCHANTED_GOLDEN_APPLE, n);
         return scan != -1 ? scan : this.scanFor(Items.GOLDEN_APPLE, n);
      }
   }

   private int scanFor(Item Item, int n) {
      if (this.swapMode.getValue().ordinal() == 2) {
         for (int i = 9; i < mc.player.getInventory().size() + 1; i++) {
            if (i != n && mc.player.getInventory().getStack(i).getItem() == Item) {
               return i;
            }
         }

         for (int j = 0; j < 9; j++) {
            if (j != n && mc.player.getInventory().getStack(j).getItem() == Item) {
               return j;
            }
         }

         return -1;
      } else {
         for (int k = 44; k >= 0; k--) {
            if (k != n && mc.player.getInventory().getStack(k).getItem() == Item) {
               return k < 9 ? k + 36 : k;
            }
         }

         return -1;
      }
   }

   private void swap(int n, int n2) {
      switch ((Offhand.SwapMode)this.swapMode.getValue()) {
         case ClickSlot:
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, n, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, n2 + 36, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, n, 0, SlotActionType.PICKUP, mc.player);
            EntityUtil.syncInventory();
            break;
         case OffhandSwap:
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, n, n2, SlotActionType.SWAP, mc.player);
            EntityUtil.syncInventory();
            break;
         case Pick:
            int selectedSlot = mc.player.getInventory().selectedSlot;
            InventoryUtil.switchToSlot(n2);
            mc.getNetworkHandler().sendPacket(new PickFromInventoryC2SPacket(n));
            InventoryUtil.switchToSlot(selectedSlot);
      }
   }
}
