package dev.chun.mod.modules.impl.misc;

import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.UpdateEvent;
import dev.chun.api.utils.math.Timer;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.impl.client.IRC;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import dev.chun.mod.modules.settings.impl.StringSetting;

public class AutoXYZ extends Module {
   public static AutoXYZ INSTANCE;
   private final SliderSetting delay = this.add(new SliderSetting("Delay", 20.0, 0.0, 600.0, 1.0).setSuffix("s"));
   private final StringSetting message = this.add(new StringSetting("Message", "I am in %x% %y% %z%"));
   private final BooleanSetting onEnable = this.add(new BooleanSetting("SendOnEnable", true));
   private final BooleanSetting ircMark = this.add(new BooleanSetting("IrcMark", false));
   private final Timer timer = new Timer();

   public AutoXYZ() {
      super("AutoXYZ", Module.Category.Misc);
      this.setChinese("\u81ea\u52a8\u62a5\u5750\u6807");
      INSTANCE = this;
   }

   @Override
   public void onEnable() {
      this.timer.reset();
      if (this.onEnable.getValue()) {
         this.send();
      }
   }

   @EventListener
   public void onUpdate(UpdateEvent updateEvent) {
      if (!Module.nullCheck() && !(this.delay.getValue() <= 0.0)) {
         if (this.timer.passed((long)(this.delay.getValue() * 1000.0))) {
            this.timer.reset();
            this.send();
         }
      }
   }

   private void send() {
      if (!nullCheck()) {
         int i = (int)Math.floor(mc.player.getX());
         int j = (int)Math.floor(mc.player.getY());
         int k = (int)Math.floor(mc.player.getZ());
         mc.getNetworkHandler()
            .sendChatMessage(
               this.message.getValue().replace("%x%", Integer.toString(i)).replace("%y%", Integer.toString(j)).replace("%z%", Integer.toString(k))
            );
         if (this.ircMark.getValue() && IRC.INSTANCE != null && IRC.INSTANCE.isConnected()) {
            IRC.sendMark((double)i, (double)j, (double)k, "xyz");
         }
      }
   }
}
