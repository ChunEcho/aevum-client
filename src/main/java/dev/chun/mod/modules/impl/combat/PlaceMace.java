package dev.chun.mod.modules.impl.combat;

import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.ClientTickEvent;
import dev.chun.api.utils.math.Timer;
import dev.chun.api.utils.player.InventoryUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class PlaceMace extends Module {
   public static PlaceMace INSTANCE;
   private final Timer timer1 = new Timer();
   private final SliderSetting Qztj = this.add(new SliderSetting("UpwardVelocity", 1.5, 0.1, 4.0, 0.1));
   private final SliderSetting delay = this.add(new SliderSetting("Delay", 1.0, 0.1, 5.0, 0.1));

   public PlaceMace() {
      super("PlaceMace", Module.Category.Mace);
      this.setChinese("\u539f\u5730\u91cd\u9524");
      INSTANCE = this;
   }

   @EventListener
   public void onTick(ClientTickEvent var1) {
      if (!nullCheck()) {
         if (this.getTimer12().passedS(this.getDelay().getValue())) {
            int var2 = InventoryUtil.findItem(Items.WIND_CHARGE);
            if (var2 != -1) {
               int var3 = mc.player.getInventory().selectedSlot;
               InventoryUtil.switchToSlot(var2);
               sendSequencedPacket(var0 -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, var0, mc.player.getYaw(), mc.player.getPitch()));
               mc.player.addVelocity(0.0, this.QztjAccess().getValue(), 0.0);
               InventoryUtil.switchToSlot(var3);
               this.getTimer12().reset();
            }
         }
      }
   }

   @Override
   public String getInfo() {
      if (mc.player != null && mc.world != null) {
         boolean var1 = false;
         boolean var2 = false;
         boolean var3 = false;
         int var4 = 0;

         for (int var5 = 0; var5 < 9; var5++) {
            if (mc.player.getInventory().getStack(var5).getItem() == Items.WIND_CHARGE) {
               var4 += mc.player.getInventory().getStack(var5).getCount();
            }
         }

         return var4 > 0 ? String.valueOf(var4) : null;
      } else {
         return "";
      }
   }

   private Timer getTimer12() {
      return this.timer1;
   }

   private SliderSetting QztjAccess() {
      return this.Qztj;
   }

   private SliderSetting getDelay() {
      return this.delay;
   }
}
