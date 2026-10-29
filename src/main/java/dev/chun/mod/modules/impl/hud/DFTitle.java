package dev.chun.mod.modules.impl.hud;

import dev.chun.api.utils.render.Render2DUtil;
import dev.chun.core.impl.FontManager;
import dev.chun.mod.modules.HudElement;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.EnumSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class DFTitle extends Module implements HudElement {
   public static DFTitle INSTANCE;
   private int hudX = 10;
   private int hudY = 8;
   public final EnumSetting<DFTitle.Mode> mode = this.add(new EnumSetting<>("Mode", DFTitle.Mode.Normal));
   public final SliderSetting paddingY = this.add(new SliderSetting("HeightAdjust", 0.0, -10.0, 10.0, 0.5));
   public final SliderSetting textYOffset = this.add(new SliderSetting("TextYOffset", 3.0, -10.0, 10.0, 0.5));
   private static final int BG_ALPHA = 29;
   private static final float PADDING_X = 8.0F;
   private static final boolean SHADOW = false;

   public DFTitle() {
      super("DFTitle", Module.Category.HUD);
      this.setChinese("\u4e09\u89d2\u6d32\u6807\u9898");
      INSTANCE = this;
   }

   @Override
   public String getElementName() {
      return this.getChinese() != null ? this.getChinese() : this.getName();
   }

   @Override
   public int getElementX() {
      return this.hudX;
   }

   @Override
   public void setElementX(int x) {
      this.hudX = x;
   }

   @Override
   public int getElementY() {
      return this.hudY;
   }

   @Override
   public void setElementY(int y) {
      this.hudY = y;
   }

   private String getDisplayString() {
      return switch ((DFTitle.Mode)this.mode.getValue()) {
         case Normal -> "\u5e38\u89c4";
         case Medium -> "\u673a\u5bc6";
         case Hard -> "\u7edd\u5bc6";
      };
   }

   private Color getTextColor() {
      return switch ((DFTitle.Mode)this.mode.getValue()) {
         case Hard -> new Color(255, 0, 0, 255);
         default -> Color.WHITE;
      };
   }

   @Override
   public int getElementWidth() {
      float textWidth = FontManager.getWidth(this.getDisplayString());
      return (int)(textWidth + 16.0F);
   }

   @Override
   public int getElementHeight() {
      float textHeight = FontManager.getFontHeight();
      return (int)(textHeight + this.paddingY.getValueFloat() * 2.0F);
   }

   @Override
   public boolean isElementVisible() {
      return this.isOn();
   }

   @Override
   public void onRender2D(DrawContext drawContext, float tickDelta) {
      this.renderElement(drawContext, tickDelta);
   }

   @Override
   public void renderElement(DrawContext drawContext, float tickDelta) {
      if (mc.player != null && mc.world != null) {
         String displayString = this.getDisplayString();
         Color textColor = this.getTextColor();
         float rectWidth = (float)this.getElementWidth();
         float rectHeight = (float)this.getElementHeight();
         float x = (float)this.hudX;
         float y = (float)this.hudY;
         MatrixStack matrices = drawContext.getMatrices();
         Render2DUtil.drawRect(matrices, x, y, rectWidth, rectHeight, new Color(0, 0, 0, 29));
         float textX = x + 8.0F;
         float textY = y + this.paddingY.getValueFloat() + this.textYOffset.getValueFloat();
         this.drawContextText(drawContext, displayString, textX, textY, textColor, false);
      }
   }

   private void drawContextText(DrawContext drawContext, String text, float x, float y, Color color, boolean shadow) {
      MatrixStack matrices = drawContext.getMatrices();
      if (FontManager.isCustom()) {
         FontManager.get()
            .drawString(
               matrices,
               text,
               x,
               y,
               (float)color.getRed() / 255.0F,
               (float)color.getGreen() / 255.0F,
               (float)color.getBlue() / 255.0F,
               (float)color.getAlpha() / 255.0F,
               shadow
            );
      } else {
         drawContext.drawText(mc.textRenderer, text, (int)x, (int)y, color.getRGB(), shadow);
      }
   }

   public static enum Mode {
      Normal,
      Medium,
      Hard;

      private Mode() {
      }
   }
}
