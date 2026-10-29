package dev.chun.mod.modules.impl.hud;

import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.PacketEvent;
import dev.chun.api.utils.math.Timer;
import dev.chun.api.utils.render.TextUtil;
import dev.chun.core.impl.FontManager;
import dev.chun.mod.modules.HudElement;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.ColorSetting;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;

public class PacketHud extends Module implements HudElement {
   public static PacketHud INSTANCE;
   private int hudX = 2;
   private int hudY = 122;
   private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
   private final ColorSetting color = this.add(new ColorSetting("Color", new Color(208, 0, 0)));
   private int incomingPackets = 0;
   private int outgoingPackets = 0;
   private int lastIn = 0;
   private int lastOut = 0;
   private final Timer resetTimer = new Timer();

   public PacketHud() {
      super("Packets", Module.Category.HUD);
      this.setChinese("\u6570\u636e\u5305\u663e\u793a");
      INSTANCE = this;
   }

   private String getText() {
      return "Packets \u00a7f" + this.lastIn + "<-" + this.lastOut;
   }

   @EventListener
   public void onReceive(PacketEvent.Receive e) {
      this.incomingPackets++;
   }

   @EventListener
   public void onSend(PacketEvent.Send e) {
      this.outgoingPackets++;
   }

   @Override
   public void onRender2D(DrawContext drawContext, float tickDelta) {
      if (this.resetTimer.passedMs(150.0)) {
         this.lastIn = this.incomingPackets;
         this.lastOut = this.outgoingPackets;
         this.incomingPackets = 0;
         this.outgoingPackets = 0;
         this.resetTimer.reset();
      }

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
}
