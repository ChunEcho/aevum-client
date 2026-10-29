package dev.chun.mod.modules.impl.misc;

import dev.chun.Aevum;
import dev.chun.core.impl.FPSManager;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.util.Random;

public class FakeFPS extends Module {
   public static FakeFPS INSTANCE;
   private FPSManager originalFPS;
   private FPSManager fakeFPS;
   private final Random random = new Random();
   private long lastUpdate;
   private int currentFakeFPS;
   private final SliderSetting minFPS = this.add(new SliderSetting("MinFPS", 60.0, 0.0, 10000.0, 1.0));
   private final SliderSetting maxFPS = this.add(new SliderSetting("MaxFPS", 500.0, 0.0, 10000.0, 1.0));

   public FakeFPS() {
      super("FakeFPS", Module.Category.Misc);
      this.setChinese("\u5047FPS");
      INSTANCE = this;
   }

   @Override
   public void onEnable() {
      if (!nullCheck()) {
         this.originalFPS = Aevum.FPS;
         this.lastUpdate = System.currentTimeMillis();
         this.currentFakeFPS = this.getRandomFPS();
         this.fakeFPS = new FPSManager() {
            @Override
            public int getFps() {
               long now = System.currentTimeMillis();
               if (now - FakeFPS.this.lastUpdate >= 100L) {
                  FakeFPS.this.lastUpdate = now;
                  FakeFPS.this.currentFakeFPS = FakeFPS.this.getRandomFPS();
               }

               return FakeFPS.this.currentFakeFPS;
            }
         };
         Aevum.FPS = this.fakeFPS;
      }
   }

   @Override
   public void onDisable() {
      if (this.originalFPS != null) {
         Aevum.FPS = this.originalFPS;
         this.originalFPS = null;
      }
   }

   @Override
   public String getInfo() {
      return String.valueOf(this.currentFakeFPS);
   }

   private int getRandomFPS() {
      int min = this.minFPS.getValueInt();
      int max = this.maxFPS.getValueInt();
      return min >= max ? Math.max(min, 1) : Math.max(this.random.nextInt(max - min + 1) + min, 1);
   }
}
