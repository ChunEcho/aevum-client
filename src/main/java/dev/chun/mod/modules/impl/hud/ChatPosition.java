package dev.chun.mod.modules.impl.hud;

import dev.chun.mod.modules.HudElement;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;

public class ChatPosition extends Module implements HudElement {
   public static ChatPosition INSTANCE;
   public final SliderSetting x = this.add(new SliderSetting("X", 2.0, -500.0, 2000.0, 1.0));
   public final SliderSetting y = this.add(new SliderSetting("Y", 2.0, -500.0, 2000.0, 1.0));
   public final SliderSetting width = this.add(new SliderSetting("Width", 320.0, 40.0, 2000.0, 1.0));
   public final SliderSetting height = this.add(new SliderSetting("Height", 180.0, 20.0, 2000.0, 1.0));

   public ChatPosition() {
      super("ChatPosition", Module.Category.HUD);
      this.setChinese("\u804a\u5929\u4f4d\u7f6e");
      INSTANCE = this;
   }

   public static boolean shouldEdit() {
      return INSTANCE != null && INSTANCE.isOn();
   }

   public static int getXPos() {
      return shouldEdit() ? INSTANCE.x.getValueInt() : 2;
   }

   public static int getYPos() {
      return shouldEdit() ? INSTANCE.y.getValueInt() : 2;
   }

   public static int getWidthValue() {
      return shouldEdit() ? INSTANCE.width.getValueInt() : 320;
   }

   public static int getHeightValue() {
      return shouldEdit() ? INSTANCE.height.getValueInt() : 180;
   }

   @Override
   public String getElementName() {
      return this.getChinese() != null ? this.getChinese() : this.getName();
   }

   @Override
   public int getElementX() {
      return this.x.getValueInt();
   }

   @Override
   public void setElementX(int x) {
      this.x.setValue((double)x);
   }

   @Override
   public int getElementY() {
      return this.y.getValueInt();
   }

   @Override
   public void setElementY(int y) {
      this.y.setValue((double)y);
   }

   @Override
   public int getElementWidth() {
      return this.width.getValueInt();
   }

   @Override
   public int getElementHeight() {
      return this.height.getValueInt();
   }

   @Override
   public boolean isElementVisible() {
      return this.isOn();
   }

   @Override
   public void renderElement(DrawContext context, float tickDelta) {
      int x = this.getElementX();
      int y = this.getElementY();
      int w = this.getElementWidth();
      int h = this.getElementHeight();
      context.fill(x, y, x + w, y + h, 889192448);
      int lineX = x + 4;
      int lineY = y + h - 14;
      int lineHeight = 10;

      int alpha;
      for (int i = 0; i < 6 && (alpha = 80 - i * 10) > 0; i++) {
         int msgWidth = Math.max(40, w - 20 - i * 18);
         context.fill(lineX, lineY - i * lineHeight, lineX + msgWidth, lineY - i * lineHeight + 9, alpha << 24);
      }
   }

   @Override
   public void onRender2D(DrawContext drawContext, float tickDelta) {
   }
}
