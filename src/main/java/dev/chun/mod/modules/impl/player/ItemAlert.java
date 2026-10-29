package dev.chun.mod.modules.impl.player;

import dev.chun.Aevum;
import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.UpdateEvent;
import dev.chun.api.utils.player.EntityUtil;
import dev.chun.api.utils.player.InventoryUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;

public class ItemAlert extends Module {
   public static ItemAlert INSTANCE;
   private final BooleanSetting totemAlert = this.add(new BooleanSetting("TotemAlert", true));
   private final SliderSetting totemThreshold;
   private final BooleanSetting potionAlert;
   private final SliderSetting potionThreshold;
   private final BooleanSetting armorAlert;
   private final SliderSetting armorThreshold;
   private final BooleanSetting armorSmart;
   private final SliderSetting armorRange;
   private final SliderSetting volume;
   private final SliderSetting cooldown;
   private long lastTotemAlertTime;
   private long lastPotionAlertTime;
   private long lastArmorAlertTime;
   private boolean wasTotemLow;
   private boolean wasPotionLow;
   private boolean wasArmorLow;

   public ItemAlert() {
      super("ItemAlert", "Alerts when totems/potions/armor are low", Module.Category.Player);
      String name = "TotemThreshold";
      double value = 2.0;
      double min = 1.0;
      double max = 10.0;
      double increment = 1.0;
      BooleanSetting totemAlert = this.totemAlert;
      this.totemThreshold = this.add(new SliderSetting("TotemThreshold", 2.0, 1.0, 10.0, 1.0, totemAlert::getValue));
      this.potionAlert = this.add(new BooleanSetting("PotionAlert", true));
      String name2 = "PotionThreshold";
      double value2 = 4.0;
      double min2 = 1.0;
      double max2 = 20.0;
      double increment2 = 1.0;
      BooleanSetting potionAlert = this.potionAlert;
      this.potionThreshold = this.add(new SliderSetting("PotionThreshold", 4.0, 1.0, 20.0, 1.0, potionAlert::getValue));
      this.armorAlert = this.add(new BooleanSetting("ArmorAlert", false));
      String name3 = "ArmorThreshold";
      double value3 = 30.0;
      double min3 = 1.0;
      double max3 = 100.0;
      double increment3 = 1.0;
      BooleanSetting armorAlert = this.armorAlert;
      this.armorThreshold = this.add(new SliderSetting("ArmorThreshold", 30.0, 1.0, 100.0, 1.0, armorAlert::getValue));
      String name4 = "ArmorSmart";
      boolean defaultValue = true;
      BooleanSetting armorAlert2 = this.armorAlert;
      this.armorSmart = this.add(new BooleanSetting("ArmorSmart", true, armorAlert2::getValue));
      this.armorRange = this.add(new SliderSetting("ArmorRange", 8.0, 1.0, 32.0, 1.0, () -> this.armorAlert.getValue() && this.armorSmart.getValue()));
      this.volume = this.add(new SliderSetting("Volume", 1.0, 0.1, 2.0, 0.1));
      this.cooldown = this.add(new SliderSetting("Cooldown", 5000, 0, 30000));
      this.setChinese("\u7269\u54c1\u9884\u8b66");
      INSTANCE = this;
   }

   @EventListener
   public void onUpdate(UpdateEvent updateEvent) {
      if (!nullCheck()) {
         long currentTimeMillis = System.currentTimeMillis();
         long n = (long)this.cooldown.getValueInt();
         if (this.totemAlert.getValue()) {
            int itemCount = InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING);
            boolean wasTotemLow = itemCount > 0 && itemCount <= this.totemThreshold.getValueInt();
            if (wasTotemLow && !this.wasTotemLow && currentTimeMillis - this.lastTotemAlertTime > n) {
               this.playAlert("Low Totems: " + itemCount);
               this.lastTotemAlertTime = currentTimeMillis;
            }

            this.wasTotemLow = wasTotemLow;
         }

         if (this.potionAlert.getValue()) {
            int n2 = InventoryUtil.getItemCount(Items.SPLASH_POTION)
               + InventoryUtil.getItemCount(Items.LINGERING_POTION)
               + InventoryUtil.getItemCount(Items.EXPERIENCE_BOTTLE);
            boolean wasPotionLow = n2 > 0 && n2 <= this.potionThreshold.getValueInt();
            if (wasPotionLow && !this.wasPotionLow && currentTimeMillis - this.lastPotionAlertTime > n) {
               this.playAlert("Low Potions: " + n2);
               this.lastPotionAlertTime = currentTimeMillis;
            }

            this.wasPotionLow = wasPotionLow;
         }

         if (this.armorAlert.getValue() && (!this.armorSmart.getValue() || this.hasNearbyEnemy())) {
            int n3 = 100;

            for (ItemStack stack : mc.player.getArmorItems()) {
               if (stack != null && !stack.isEmpty() && stack.getMaxDamage() > 0) {
                  int damagePercent = EntityUtil.getDamagePercent(stack);
                  if (damagePercent < n3) {
                     n3 = damagePercent;
                  }
               }
            }

            boolean wasArmorLow = n3 <= this.armorThreshold.getValueInt();
            if (wasArmorLow && !this.wasArmorLow && currentTimeMillis - this.lastArmorAlertTime > n) {
               this.playAlert("Low Armor: " + n3);
               this.lastArmorAlertTime = currentTimeMillis;
            }

            this.wasArmorLow = wasArmorLow;
         }
      }
   }

   private boolean hasNearbyEnemy() {
      double n = this.armorRange.getValue() * this.armorRange.getValue();

      for (PlayerEntity entity : mc.world.getPlayers()) {
         if (entity != mc.player && entity.isAlive() && !Aevum.FRIEND.isFriend(entity) && mc.player.squaredDistanceTo(entity) <= n) {
            return true;
         }
      }

      return false;
   }

   private void playAlert(String s) {
      this.sendMessage(s);
      mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_BELL, this.volume.getValueFloat()));
   }

   @Override
   public void onDisable() {
      this.wasTotemLow = false;
      this.wasPotionLow = false;
      this.wasArmorLow = false;
   }
}
