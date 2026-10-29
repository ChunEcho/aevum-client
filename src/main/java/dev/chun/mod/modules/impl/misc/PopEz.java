package dev.chun.mod.modules.impl.misc;

import dev.chun.Aevum;
import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.TotemEvent;
import dev.chun.api.events.impl.UpdateEvent;
import dev.chun.api.utils.math.Timer;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import dev.chun.mod.modules.settings.impl.StringSetting;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class PopEz extends Module {
   public static PopEz INSTANCE;
   private final SliderSetting randomChars;
   public final BooleanSetting slowSend;
   private final StringSetting messageFormat = this.add(new StringSetting("Message", "%player% has popped %count% totems."));
   Random random;
   Timer timer;
   private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
   private Map<Integer, Integer> popQueue;

   public PopEz() {
      super("PopEz", Module.Category.Misc);
      this.randomChars = this.add(new SliderSetting("RandomChars", 3.0, 0.0, 20.0, 1.0));
      this.slowSend = this.add(new BooleanSetting("SlowSend", false));
      this.random = new Random();
      this.timer = new Timer();
      this.popQueue = new HashMap<>();
      this.setChinese("POP\u5632\u8bbd");
      INSTANCE = this;
   }

   @EventListener
   public void onTotem(TotemEvent totemEvent) {
      PlayerEntity player = totemEvent.getPlayer();
      if (player != null && !player.equals(this.getMinecraft().player) && !Aevum.FRIEND.isFriend(player)) {
         int intValue = 1;
         if (Aevum.POP.popContainer.containsKey(player.getName().getString())) {
            intValue = Aevum.POP.popContainer.get(player.getName().getString());
         }

         if (this.slowSend.getValue()) {
            this.popQueue.put(player.getId(), intValue);
            return;
         }

         this.sendMessage(this.buildMessage(player.getName().getString(), intValue), player.getId());
      }
   }

   @EventListener
   public void onUpdate(UpdateEvent updateEvent) {
      if (this.slowSend.getValue() && !this.popQueue.isEmpty() && this.timer.passedS(3.2)) {
         this.timer.reset();
         Entry entry = this.popQueue.entrySet().iterator().next();
         int intValue = (Integer)entry.getKey();
         int intValue2 = (Integer)entry.getValue();
         PlayerEntity PlayerEntity = null;

         for (Entity class_1658 : this.getMinecraft().world.getEntities()) {
            PlayerEntity ChatVisibility;
            if (class_1658 instanceof PlayerEntity && (ChatVisibility = (PlayerEntity)class_1658).getId() == intValue) {
               PlayerEntity = ChatVisibility;
               break;
            }
         }

         if (PlayerEntity != null) {
            this.sendMessage(this.buildMessage(PlayerEntity.getName().getString(), intValue2), intValue);
            this.popQueue.remove(intValue);
            return;
         }

         this.popQueue.remove(intValue);
      }
   }

   public void sendMessage(String s, int n) {
      MinecraftClient minecraft = this.getMinecraft();
      if (minecraft.player != null && minecraft.player.networkHandler != null) {
         String generateRandomString = this.generateRandomString(this.randomChars.getValueInt());
         if (!generateRandomString.isEmpty()) {
            s = s + " " + generateRandomString;
         }

         minecraft.player.networkHandler.sendChatMessage(s);
      }
   }

   private String generateRandomString(int capacity) {
      StringBuilder sb = new StringBuilder(capacity);

      for (int i = 0; i < capacity; i++) {
         sb.append(
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".charAt(this.random.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".length()))
         );
      }

      return sb.toString();
   }

   private MinecraftClient getMinecraft() {
      return MinecraftClient.getInstance();
   }

   private String buildMessage(String replacement, int i) {
      return this.messageFormat.getValue().replace("%player%", replacement).replace("%count%", String.valueOf(i));
   }
}
