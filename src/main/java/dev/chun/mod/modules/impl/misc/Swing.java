package dev.chun.mod.modules.impl.misc;

import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.EnumSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;

public class Swing extends Module {
   public static Swing INSTANCE;
   public final EnumSetting<Swing.HandMode> hand = this.add(new EnumSetting<>("Hand", Swing.HandMode.Mainhand));
   public final EnumSetting<Swing.AnimMode> type = this.add(new EnumSetting<>("Type", Swing.AnimMode.Vanilla));
   public final SliderSetting speed = this.add(new SliderSetting("Speed", 1.0, 0.5, 2.0, 0.1));

   public Swing() {
      super("Swing", "Changes your swinging hand.", Module.Category.Misc);
      this.setChinese("\u6325\u624b");
      INSTANCE = this;
   }

   @Override
   public String getInfo() {
      return this.hand.getValue().name();
   }

   public static enum AnimMode {
      Vanilla,
      ONE_EIGHT,
      ONE_TWELVE;

      private AnimMode() {
      }
   }

   public static enum HandMode {
      Mainhand,
      Offhand,
      Packet,
      Vanilla;

      private HandMode() {
      }
   }
}
