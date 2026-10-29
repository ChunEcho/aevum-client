package dev.chun.mod.modules.impl.hud;

import dev.chun.api.utils.render.TextUtil;
import dev.chun.core.impl.FontManager;
import dev.chun.mod.modules.HudElement;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.ColorSetting;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;

public class DirectionHud extends Module implements HudElement {
   public static DirectionHud INSTANCE;
   private int hudX = 2;
   private int hudY = -1;
   private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
   private final ColorSetting color = this.add(new ColorSetting("Color", new Color(208, 0, 0)));

   public DirectionHud() {
      super("Direction", Module.Category.HUD);
      this.setChinese("\u65b9\u5411\u663e\u793a");
      INSTANCE = this;
   }

   private String getText() {
      if (mc.player == null) {
         return "North \u00a77[\u00a7f-Z\u00a77]";
      } else {
         float yaw = mc.player.getYaw();
         String a;
         String f;
         if ((double)(yaw = (yaw % 360.0F + 360.0F) % 360.0F) >= 337.5 || (double)yaw < 22.5) {
            f = "South";
            a = "+Z";
         } else if ((double)yaw < 67.5) {
            f = "Southwest";
            a = "-X +Z";
         } else if ((double)yaw < 112.5) {
            f = "West";
            a = "-X";
         } else if ((double)yaw < 157.5) {
            f = "Northwest";
            a = "-X -Z";
         } else if ((double)yaw < 202.5) {
            f = "North";
            a = "-Z";
         } else if ((double)yaw < 247.5) {
            f = "Northeast";
            a = "+X -Z";
         } else if ((double)yaw < 292.5) {
            f = "East";
            a = "+X";
         } else {
            f = "Southeast";
            a = "+X +Z";
         }

         return f + " \u00a77[\u00a7f" + a + "\u00a77]";
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
      return this.hudX;
   }

   @Override
   public void setElementX(int x) {
      this.hudX = x;
   }

   @Override
   public int getElementY() {
      return this.hudY < 0 ? mc.getWindow().getScaledHeight() - (int)FontManager.getFontHeight() * 2 - 4 : this.hudY;
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
   public void renderElement(DrawContext context, float tickDelta) {
      TextUtil.drawString(
         context, this.getText(), (double)this.getElementX(), (double)this.getElementY(), this.color.getValue().getRGB(), this.shadow.getValue()
      );
   }
}
