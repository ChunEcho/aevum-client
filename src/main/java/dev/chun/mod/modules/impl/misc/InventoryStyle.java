package dev.chun.mod.modules.impl.misc;

import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.SliderSetting;

public class InventoryStyle extends Module {
   public static InventoryStyle INSTANCE;
   private final SliderSetting opacity = this.add(new SliderSetting("Opacity", 50.0, 10.0, 100.0, 5.0).setSuffix("%"));

   public InventoryStyle() {
      super("InventoryStyle", "Adjustable container background opacity.", Module.Category.Misc);
      this.setChinese("\u80cc\u5305\u900f\u660e");
      INSTANCE = this;
   }

   public static float getOpacityF() {
      InventoryStyle instance = INSTANCE;
      return instance != null && instance.isOn() ? (float)(instance.opacity.getValue() / 100.0) : 1.0F;
   }
}
