package dev.chun.mod.modules.impl.render;

import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.Render2DEvent;
import dev.chun.api.utils.render.ColorUtil;
import dev.chun.api.utils.render.Render2DUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.ColorSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class Keystrokes extends Module {
   public static Keystrokes INSTANCE;
   public final SliderSetting posX = this.add(new SliderSetting("PosX", 26.0, 0.0, 1920.0, 1.0));
   public final SliderSetting posY = this.add(new SliderSetting("PosY", 96.0, 0.0, 1080.0, 1.0));
   public final SliderSetting keyWidth = this.add(new SliderSetting("KeyWidth", 52.0, 20.0, 90.0, 1.0));
   public final SliderSetting keyHeight = this.add(new SliderSetting("KeyHeight", 34.0, 16.0, 60.0, 1.0));
   public final SliderSetting gap = this.add(new SliderSetting("Gap", 2.0, 0.0, 12.0, 1.0));
   private final SliderSetting radius = this.add(new SliderSetting("Radius", 2.0, 0.0, 12.0, 1.0));
   private final SliderSetting activeGlow = this.add(new SliderSetting("Glow", 10.0, 0.0, 20.0, 1.0));
   private final ColorSetting idleColor = this.add(new ColorSetting("IdleColor", new Color(189, 153, 255, 170)));
   private final ColorSetting idleStroke = this.add(new ColorSetting("IdleStroke", new Color(189, 153, 255, 34)));
   private final ColorSetting textColor = this.add(new ColorSetting("TextColor", new Color(189, 153, 255, 255)));
   private final Keystrokes.KeyVisual forwardVisual = new Keystrokes.KeyVisual();
   private final Keystrokes.KeyVisual leftVisual = new Keystrokes.KeyVisual();
   private final Keystrokes.KeyVisual backVisual = new Keystrokes.KeyVisual();
   private final Keystrokes.KeyVisual rightVisual = new Keystrokes.KeyVisual();
   private final Keystrokes.KeyVisual leftMouseVisual = new Keystrokes.KeyVisual();
   private final Keystrokes.KeyVisual rightMouseVisual = new Keystrokes.KeyVisual();
   private final Keystrokes.KeyVisual jumpVisual = new Keystrokes.KeyVisual();

   public Keystrokes() {
      super("Keystrokes", Module.Category.Render);
      this.setChinese("\u6309\u952e\u663e\u793a");
      INSTANCE = this;
   }

   @EventListener
   public void onRender2D(Render2DEvent render2DEvent) {
      if (mc.player != null && mc.world != null && !mc.options.hudHidden) {
         DrawContext drawContext = render2DEvent.drawContext;
         MatrixStack getMatrices = render2DEvent.drawContext.getMatrices();
         float valueFloat = this.posX.getValueFloat();
         float valueFloat2 = this.posY.getValueFloat();
         float valueFloat3 = this.keyWidth.getValueFloat();
         float valueFloat4 = this.keyHeight.getValueFloat();
         float valueFloat5 = this.gap.getValueFloat();
         float n = valueFloat2 + valueFloat4 + valueFloat5;
         float n2 = n + valueFloat4 + valueFloat5;
         float n3 = n2 + valueFloat4 + valueFloat5;
         this.update(this.forwardVisual, mc.options.forwardKey);
         this.update(this.leftVisual, mc.options.leftKey);
         this.update(this.backVisual, mc.options.backKey);
         this.update(this.rightVisual, mc.options.rightKey);
         this.update(this.leftMouseVisual, mc.options.attackKey);
         this.update(this.rightMouseVisual, mc.options.useKey);
         this.update(this.jumpVisual, mc.options.jumpKey);
         this.drawKey(drawContext, getMatrices, valueFloat + valueFloat3 + valueFloat5, valueFloat2, valueFloat3, valueFloat4, "W", this.forwardVisual);
         this.drawKey(drawContext, getMatrices, valueFloat, n, valueFloat3, valueFloat4, "A", this.leftVisual);
         this.drawKey(drawContext, getMatrices, valueFloat + valueFloat3 + valueFloat5, n, valueFloat3, valueFloat4, "S", this.backVisual);
         this.drawKey(drawContext, getMatrices, valueFloat + (valueFloat3 + valueFloat5) * 2.0F, n, valueFloat3, valueFloat4, "D", this.rightVisual);
         this.drawKey(drawContext, getMatrices, valueFloat, n2, valueFloat3 * 1.5F + valueFloat5 * 0.5F, valueFloat4, "LMB", this.leftMouseVisual);
         this.drawKey(
            drawContext,
            getMatrices,
            valueFloat + valueFloat3 * 1.5F + valueFloat5 * 1.5F,
            n2,
            valueFloat3 * 1.5F + valueFloat5 * 0.5F,
            valueFloat4,
            "RMB",
            this.rightMouseVisual
         );
         this.drawKey(drawContext, getMatrices, valueFloat, n3, valueFloat3 * 3.0F + valueFloat5 * 2.0F, valueFloat4, "SPACE", this.jumpVisual);
      }
   }

   private void update(Keystrokes.KeyVisual keyVisual, KeyBinding KeyBinding) {
      keyVisual.progress = keyVisual.progress + ((KeyBinding.isPressed() ? 1.0F : 0.0F) - keyVisual.progress) * 0.22F;
   }

   private void drawKey(DrawContext DrawContext, MatrixStack NarratorManager, float n, float n2, float n3, float n4, String s, Keystrokes.KeyVisual keyVisual) {
      Color themeColor = this.getThemeColor();
      float clamp = this.clamp(keyVisual.progress);
      float min = Math.min(this.radius.getValueFloat(), n4 / 2.0F);
      Color blend = this.blend(new Color(38, 56, 92, 220), ColorUtil.injectAlpha(themeColor.brighter(), 255), clamp);
      Color color = clamp > 0.01F ? blend : this.idleColor.getValue();
      Color color2 = clamp > 0.01F ? ColorUtil.injectAlpha(Color.WHITE, (int)(100.0F + 90.0F * clamp)) : this.idleStroke.getValue();
      Render2DUtil.drawRoundedRect(NarratorManager, n, n2, n3, n4, min, color);
      Render2DUtil.drawRoundedStroke(NarratorManager, n, n2, n3, n4, min, color2, 18);
      if (clamp > 0.01F) {
         float n5 = this.activeGlow.getValueFloat() * clamp;
         Render2DUtil.drawRoundedRect(
            NarratorManager,
            n - n5 * 0.18F,
            n2 - n5 * 0.18F,
            n3 + n5 * 0.36F,
            n4 + n5 * 0.36F,
            min + n5 * 0.12F,
            ColorUtil.injectAlpha(themeColor, (int)(95.0F * clamp))
         );
         Render2DUtil.drawRoundedRect(
            NarratorManager,
            n + 1.0F,
            n2 + 1.0F,
            n3 - 2.0F,
            Math.max(2.0F, n4 * 0.22F),
            Math.max(1.0F, min - 1.0F),
            new Color(255, 255, 255, (int)(22.0F * clamp))
         );
      }

      DrawContext.drawText(
         mc.textRenderer,
         Text.literal(s),
         Math.round(n + (n3 - (float)mc.textRenderer.getWidth(s)) / 2.0F),
         Math.round(n2 + (n4 - 8.0F) / 2.0F),
         this.textColor.getValue().getRGB(),
         false
      );
   }

   private Color getThemeColor() {
      return new Color(95, 155, 255, 255);
   }

   private float clamp(float b) {
      return Math.max(0.0F, Math.min(1.0F, b));
   }

   private Color blend(Color color, Color color2, float n) {
      float clamp = this.clamp(n);
      return new Color(
         (int)((float)color.getRed() + (float)(color2.getRed() - color.getRed()) * clamp),
         (int)((float)color.getGreen() + (float)(color2.getGreen() - color.getGreen()) * clamp),
         (int)((float)color.getBlue() + (float)(color2.getBlue() - color.getBlue()) * clamp),
         (int)((float)color.getAlpha() + (float)(color2.getAlpha() - color.getAlpha()) * clamp)
      );
   }

   private static class KeyVisual {
      private float progress;

      private KeyVisual() {
      }
   }
}
