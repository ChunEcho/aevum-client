package dev.chun.mod.modules.impl.hud;

import dev.chun.api.utils.render.TextUtil;
import dev.chun.core.impl.FontManager;
import dev.chun.mod.modules.HudElement;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.ColorSetting;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;

public class WelcomerHud extends Module implements HudElement {
   public static WelcomerHud INSTANCE;
   private int hudX = -1;
   private int hudY = 2;
   private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
   private final ColorSetting color = this.add(new ColorSetting("Color", new Color(208, 0, 0)));

   public WelcomerHud() {
      super("Welcomer", Module.Category.HUD);
      this.setChinese("\u6b22\u8fce\u8bed");
      INSTANCE = this;
   }

   private String getText() {
      return mc.player == null ? "Welcome :')" : "Welcome, \u00a7f" + mc.player.getName().getString() + " \u00a7r:')";
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
      return this.hudX < 0 ? mc.getWindow().getScaledWidth() / 2 - this.getElementWidth() / 2 : this.hudX;
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
