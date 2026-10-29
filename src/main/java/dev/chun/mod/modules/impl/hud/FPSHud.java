package dev.chun.mod.modules.impl.hud;

import dev.chun.Aevum;
import dev.chun.api.utils.render.TextUtil;
import dev.chun.core.impl.FontManager;
import dev.chun.mod.modules.HudElement;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.ColorSetting;
import dev.chun.mod.modules.settings.impl.EnumSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.Random;
import net.minecraft.client.gui.DrawContext;

public class FPSHud extends Module implements HudElement {
   public static FPSHud INSTANCE;
   private int hudX = 2;
   private int hudY = 50;
   private HudElement.AlignMode alignMode = HudElement.AlignMode.LEFT;
   private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
   private final ColorSetting color = this.add(new ColorSetting("Color", new Color(208, 0, 0)));
   private final BooleanSetting enableFake = this.add(new BooleanSetting("FakeFPS", false));
   private final EnumSetting<FPSHud.FakeMode> fakeMode = this.add(new EnumSetting<>("FakeMode", FPSHud.FakeMode.Custom, () -> this.enableFake.getValue()));
   private final SliderSetting multiplier = this.add(
      new SliderSetting("Multiplier", 2.0, 1.0, 10.0, 0.1, () -> this.enableFake.getValue() && this.fakeMode.is(FPSHud.FakeMode.Multiplier)).setSuffix("x")
   );
   private final SliderSetting minFps = this.add(
      new SliderSetting("Min", 120.0, 0.0, 9999.0, 1.0, () -> this.enableFake.getValue() && this.fakeMode.is(FPSHud.FakeMode.Custom))
   );
   private final SliderSetting maxFps = this.add(
      new SliderSetting("Max", 240.0, 0.0, 9999.0, 1.0, () -> this.enableFake.getValue() && this.fakeMode.is(FPSHud.FakeMode.Custom))
   );
   private final SliderSetting updateDelay = this.add(
      new SliderSetting("UpdateDelay", 500.0, 50.0, 2000.0, 50.0, () -> this.enableFake.getValue() && this.fakeMode.is(FPSHud.FakeMode.Custom)).setSuffix("ms")
   );
   private int currentFakeFps = 0;
   private long lastUpdateTime = 0L;
   private final Random random = new Random();

   public FPSHud() {
      super("FPS", Module.Category.HUD);
      this.setChinese("FPS\u663e\u793a");
      INSTANCE = this;
   }

   private String getText() {
      if (!this.enableFake.getValue()) {
         return "FPS \u00a7f" + Aevum.FPS.getFps();
      } else {
         int displayFps = 0;
         if (this.fakeMode.getValue() == FPSHud.FakeMode.Multiplier) {
            displayFps = (int)((double)Aevum.FPS.getFps() * this.multiplier.getValue());
         } else if (this.fakeMode.getValue() == FPSHud.FakeMode.Custom) {
            if ((double)(System.currentTimeMillis() - this.lastUpdateTime) >= this.updateDelay.getValue()) {
               int min = (int)this.minFps.getValue();
               int max;
               if (min > (max = (int)this.maxFps.getValue())) {
                  int temp = min;
                  min = max;
                  max = temp;
               }

               this.currentFakeFps = min == max ? min : min + this.random.nextInt(max - min + 1);
               this.lastUpdateTime = System.currentTimeMillis();
            }

            displayFps = this.currentFakeFps;
         }

         return "FPS \u00a7f" + displayFps;
      }
   }

   @Override
   public void onRender2D(DrawContext drawContext, float tickDelta) {
      this.renderElement(drawContext, tickDelta);
   }

   @Override
   public String getElementName() {
      return this.getChinese() != null ? this.getChinese() : this.getName();
   }

   @Override
   public int getElementX() {
      return this.alignMode == HudElement.AlignMode.RIGHT ? this.hudX - this.getElementWidth() : this.hudX;
   }

   @Override
   public void setElementX(int x) {
      this.hudX = this.alignMode == HudElement.AlignMode.RIGHT ? x + this.getElementWidth() : x;
   }

   @Override
   public int getElementY() {
      return this.hudY;
   }

   @Override
   public void setElementY(int y) {
      this.hudY = y;
   }

   @Override
   public int getElementWidth() {
      return (int)FontManager.getWidth(this.getText()) + 4;
   }

   @Override
   public int getElementHeight() {
      return (int)FontManager.getFontHeight() + 2;
   }

   @Override
   public boolean isElementVisible() {
      return this.isOn();
   }

   @Override
   public HudElement.AlignMode getAlignMode() {
      return this.alignMode;
   }

   @Override
   public void setAlignMode(HudElement.AlignMode mode) {
      if (mode != this.alignMode) {
         if (mode == HudElement.AlignMode.RIGHT && this.alignMode == HudElement.AlignMode.LEFT) {
            this.hudX = this.hudX + this.getElementWidth();
         } else if (mode == HudElement.AlignMode.LEFT && this.alignMode == HudElement.AlignMode.RIGHT) {
            this.hudX = this.hudX - this.getElementWidth();
         }

         this.alignMode = mode;
      }
   }

   @Override
   public int getRightAnchorX() {
      return this.alignMode == HudElement.AlignMode.RIGHT ? this.hudX : this.hudX + this.getElementWidth();
   }

   @Override
   public void setRightAnchorX(int rightX) {
      this.hudX = this.alignMode == HudElement.AlignMode.RIGHT ? rightX : rightX - this.getElementWidth();
   }

   @Override
   public void renderElement(DrawContext context, float tickDelta) {
      int renderX = this.getElementX();
      TextUtil.drawString(context, this.getText(), (double)renderX, (double)this.hudY, this.color.getValue().getRGB(), this.shadow.getValue());
   }

   public static enum FakeMode {
      Multiplier,
      Custom;

      private FakeMode() {
      }
   }
}
