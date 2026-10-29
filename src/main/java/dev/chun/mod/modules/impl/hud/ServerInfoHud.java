package dev.chun.mod.modules.impl.hud;

import dev.chun.api.utils.render.TextUtil;
import dev.chun.core.impl.FontManager;
import dev.chun.mod.modules.HudElement;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.ColorSetting;
import dev.chun.mod.modules.settings.impl.EnumSetting;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;

public class ServerInfoHud extends Module implements HudElement {
   public static ServerInfoHud INSTANCE;
   private int hudX = 2;
   private int hudY = 110;
   private final EnumSetting<ServerInfoHud.Mode> mode = this.add(new EnumSetting<>("Mode", ServerInfoHud.Mode.IP));
   private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
   private final ColorSetting color = this.add(new ColorSetting("Color", new Color(208, 0, 0)));

   public ServerInfoHud() {
      super("ServerInfo", Module.Category.HUD);
      this.setChinese("\u670d\u52a1\u5668\u4fe1\u606f");
      INSTANCE = this;
   }

   private String getText() {
      return this.mode.getValue() == ServerInfoHud.Mode.IP
         ? "Server \u00a7f" + (mc.isInSingleplayer() ? "SinglePlayer" : (mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "Unknown"))
         : "Brand \u00a7f"
            + (mc.isInSingleplayer() ? "Vanilla" : (mc.getNetworkHandler() != null ? mc.getNetworkHandler().getBrand().replaceAll("\\(.*?\\)", "") : "Unknown"));
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
      TextUtil.drawString(context, this.getText(), (double)this.hudX, (double)this.hudY, this.color.getValue().getRGB(), this.shadow.getValue());
   }

   private static enum Mode {
      IP,
      Brand;

      private Mode() {
      }
   }
}
