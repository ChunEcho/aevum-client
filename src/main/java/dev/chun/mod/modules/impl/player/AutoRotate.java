package dev.chun.mod.modules.impl.player;

import dev.chun.Aevum;
import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.SendMovementPacketsEvent;
import dev.chun.api.events.impl.UpdateEvent;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.SliderSetting;

public class AutoRotate extends Module {
   public static AutoRotate INSTANCE;
   private final SliderSetting speed = this.add(new SliderSetting("Speed", 3.0, 1.0, 10.0, 0.1));
   private float rotateYaw;

   public AutoRotate() {
      super("AutoRotate", Module.Category.Player);
      this.setChinese("\u5e7b\u5f71\u65cb\u8f6c");
      INSTANCE = this;
   }

   @Override
   public void onEnable() {
      if (nullCheck()) {
         this.disable();
      } else {
         this.rotateYaw = mc.player.getYaw();
      }
   }

   @Override
   public void onDisable() {
      if (!nullCheck()) {
         mc.player.input.pressingForward = false;
         mc.player.input.pressingBack = false;
         mc.player.input.pressingLeft = false;
         mc.player.input.pressingRight = false;
      }
   }

   @EventListener
   public void onUpdate(UpdateEvent event) {
      float rotationsPerSecond = this.speed.getValueFloat();
      float yawIncrement = rotationsPerSecond * 360.0F / 20.0F;
      this.rotateYaw += yawIncrement;
      Aevum.ROTATION.setRenderRotation(this.rotateYaw, mc.player.getPitch(), true);
   }

   @EventListener
   public void onSendMovement(SendMovementPacketsEvent event) {
      event.setYaw(this.rotateYaw);
   }
}
