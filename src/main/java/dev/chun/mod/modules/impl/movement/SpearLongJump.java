package dev.chun.mod.modules.impl.movement;

import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.ClientTickEvent;
import dev.chun.api.utils.math.Timer;
import dev.chun.api.utils.player.InventoryUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.impl.render.SpearFix;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import dev.chun.mod.modules.settings.impl.StringSetting;
import java.util.Locale;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class SpearLongJump extends Module {
   public static SpearLongJump INSTANCE;
   public final SliderSetting delay = this.add(new SliderSetting("Delay", 1.0, 0.1, 10.0, 0.1));
   public final SliderSetting thrustCount = this.add(new SliderSetting("ThrustCount", 1.0, 1.0, 20.0, 1.0));
   public final StringSetting spearName = this.add(new StringSetting("SpearName", "spear"));
   public final StringSetting hotbarSlot = this.add(new StringSetting("HotbarSlot", "Auto"));
   public final BooleanSetting jumpOnSend = this.add(new BooleanSetting("JumpOnSend", true));
   public final BooleanSetting boostForward = this.add(new BooleanSetting("BoostForward", true));
   public final SliderSetting boostPower = this.add(new SliderSetting("BoostPower", 0.45, 0.1, 2.0, 0.05));
   private final Timer timer = new Timer();
   private int targetSlot = -1;
   private int prevSlot = -1;

   public SpearLongJump() {
      super("SpearLongJump", Module.Category.Spear);
      this.setChinese("\u957f\u77db\u8fdc\u8df3");
      INSTANCE = this;
   }

   @Override
   public void onEnable() {
      this.timer.reset();
      this.targetSlot = -1;
      this.prevSlot = mc.player == null ? -1 : mc.player.getInventory().selectedSlot;
      this.findSlot();
   }

   @Override
   public void onDisable() {
      if (mc.player != null && this.prevSlot >= 0 && this.prevSlot < 9) {
         int var1 = mc.player.getInventory().selectedSlot;
         if (var1 != this.prevSlot) {
            InventoryUtil.switchToSlot(this.prevSlot);
         }
      }

      this.targetSlot = -1;
      this.prevSlot = -1;
   }

   @EventListener
   public void onTick(ClientTickEvent var1) {
      if (!nullCheck()) {
         if (!var1.isPost()) {
            this.findSlot();
            if (this.targetSlot != -1) {
               if (mc.player.getInventory().selectedSlot != this.targetSlot) {
                  InventoryUtil.switchToSlot(this.targetSlot);
               }

               if (mc.currentScreen == null) {
                  if (this.timer.passed((long)(this.delay.getValue() * 1000.0))) {
                     this.trigger();
                     this.timer.reset();
                  }
               }
            }
         }
      }
   }

   private void trigger() {
      try {
         int var1 = (int)this.thrustCount.getValue();

         for (int var2 = 0; var2 < var1; var2++) {
            if (this.jumpOnSend.getValue() && mc.player.isOnGround()) {
               mc.player.jump();
            }

            if (this.boostForward.getValue()) {
               double var3 = this.boostPower.getValue();
               float var5 = mc.player.getYaw();
               double var6 = Math.toRadians((double)var5);
               double var8 = -Math.sin(var6) * var3;
               double var10 = Math.cos(var6) * var3;
               mc.player.setVelocity(new Vec3d(var8, mc.player.getVelocity().y, var10));
            }

            mc.player.swingHand(Hand.MAIN_HAND);
         }

         if (SpearFix.INSTANCE != null) {
            SpearFix.INSTANCE.playAttackSound(mc.player.getMainHandStack(), false);
         }
      } catch (Throwable var12) {
      }
   }

   private void findSlot() {
      if (this.targetSlot == -1) {
         if (mc.player != null) {
            int var1 = this.configuredSlot();
            if (var1 != -1) {
               this.targetSlot = var1;
            } else if (this.isSpear(mc.player.getMainHandStack())) {
               this.targetSlot = mc.player.getInventory().selectedSlot;
            } else {
               for (int var2 = 0; var2 < 9; var2++) {
                  if (this.isSpear(mc.player.getInventory().getStack(var2))) {
                     this.targetSlot = var2;
                     return;
                  }
               }
            }
         }
      }
   }

   private int configuredSlot() {
      String var1 = this.hotbarSlot.getValue();
      if (var1 != null && !var1.isBlank() && !var1.trim().equalsIgnoreCase("Auto")) {
         try {
            int var2 = Integer.parseInt(var1.trim());
            return var2 >= 1 && var2 <= 9 ? var2 - 1 : -1;
         } catch (NumberFormatException var3) {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private boolean isSpear(ItemStack var1) {
      if (var1 != null && !var1.isEmpty()) {
         try {
            if (SpearFix.INSTANCE != null && SpearFix.INSTANCE.shouldAnimate(var1)) {
               return true;
            }
         } catch (Throwable var4) {
         }

         String var2 = normalize(this.spearName.getValue());
         if (var2.isEmpty()) {
            return false;
         } else {
            String var3 = normalize(String.valueOf(var1.getName()));
            return var3.equals(var2) || var3.contains(var2);
         }
      } else {
         return false;
      }
   }

   private static String normalize(String var0) {
      return var0 == null ? "" : var0.replaceAll("(?i)\u00a7[0-9A-FK-OR]", "").trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
   }

   @Override
   public String getInfo() {
      return String.format("%.1fs", this.delay.getValue());
   }
}
