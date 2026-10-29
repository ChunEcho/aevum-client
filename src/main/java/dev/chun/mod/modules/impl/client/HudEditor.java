package dev.chun.mod.modules.impl.client;

import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.TickEvent;
import dev.chun.mod.gui.ClickGuiScreen;
import dev.chun.mod.modules.Module;

public class HudEditor extends Module {
   public static HudEditor INSTANCE;

   public HudEditor() {
      super("HudEditor", "HudEditor", Module.Category.Client);
      this.setChinese("\u754c\u9762\u7f16\u8f91\u5668");
      INSTANCE = this;
   }

   @Override
   public void onEnable() {
      super.onEnable();
      if (mc.player != null) {
         mc.execute(() -> mc.setScreen(ClickGuiScreen.getInstance()));
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (mc.currentScreen instanceof ClickGuiScreen) {
         mc.execute(() -> mc.setScreen(null));
      }
   }

   @EventListener
   public void onTick(TickEvent event) {
      if (!(mc.currentScreen instanceof ClickGuiScreen)) {
         this.disable();
      }
   }
}
