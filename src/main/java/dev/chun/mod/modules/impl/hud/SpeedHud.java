package dev.chun.mod.modules.impl.hud;

import dev.chun.Aevum;
import dev.chun.api.utils.render.TextUtil;
import dev.chun.core.impl.FontManager;
import dev.chun.mod.modules.HudElement;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.ColorSetting;
import java.awt.Color;
import java.text.DecimalFormat;
import net.minecraft.client.gui.DrawContext;

public class SpeedHud extends Module implements HudElement {
   public static SpeedHud INSTANCE;
   private int hudX = 2;
   private int hudY = 86;
   private HudElement.AlignMode alignMode = HudElement.AlignMode.LEFT;
   private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
   private final ColorSetting color = this.add(new ColorSetting("Color", new Color(208, 0, 0)));
   private final DecimalFormat df = new DecimalFormat("0.0");

   public SpeedHud() {
      super("SpeedHud", Module.Category.HUD);
      this.setChinese("\u901f\u5ea6\u663e\u793a");
      INSTANCE = this;
   }

   private String getText() {
      if (mc.player == null) {
         return "Speed \u00a7f0.0km/h";
      } else {
         double x = mc.player.getX() - mc.player.prevX;
         double z = mc.player.getZ() - mc.player.prevZ;
         double dist = Math.sqrt(x * x + z * z) / 1000.0;
         double div = 1.388888888888889E-5;
         float timer = Aevum.TIMER.get();
         return "Speed \u00a7f" + this.df.format(dist / div * (double)timer) + "km/h";
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
}
