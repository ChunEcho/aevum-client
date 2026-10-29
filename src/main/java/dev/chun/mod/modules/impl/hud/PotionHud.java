package dev.chun.mod.modules.impl.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.chun.api.utils.Wrapper;
import dev.chun.core.impl.FontManager;
import dev.chun.mod.gui.fonts.FontRenderer;
import dev.chun.mod.modules.HudElement;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.ColorSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;

public class PotionHud extends Module implements HudElement {
   public static PotionHud INSTANCE;
   private int hudX = 2;
   private int hudY = 130;
   private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
   private final BooleanSetting showIcon = this.add(new BooleanSetting("ShowIcon", true));
   private final SliderSetting textSize = this.add(new SliderSetting("TextSize", 8.0, 4.0, 20.0, 0.5));
   private final SliderSetting iconOffset = this.add(new SliderSetting("IconOffset", 0.0, -10.0, 10.0, 0.1));
   private final SliderSetting spacing = this.add(new SliderSetting("Spacing", 2.0, 0.0, 10.0, 0.5));
   private final BooleanSetting reverseOnRight = this.add(new BooleanSetting("ReverseOnRight", true));
   private final BooleanSetting rightAlign = this.add(new BooleanSetting("RightAlign", true));
   private final ColorSetting color = this.add(new ColorSetting("Color", new Color(208, 0, 0)));

   public PotionHud() {
      super("Potions", Module.Category.HUD);
      this.setChinese("\u836f\u6c34\u6548\u679c");
      INSTANCE = this;
   }

   private static String getDuration(StatusEffectInstance pe) {
      if (pe.isInfinite()) {
         return "\u221e";
      } else {
         int d = pe.getDuration();
         return String.format("%d:%02d", d / 1200, d % 1200 / 20);
      }
   }

   private FontRenderer getPotionFont() {
      return FontManager.get(this.textSize.getValueFloat());
   }

   private float getPotionFontHeight() {
      return this.getPotionFont().getFontHeight();
   }

   private int getPotionIconSize() {
      return Math.max((int)(this.getPotionFontHeight() * 0.8F), 8);
   }

   private List<PotionHud.PotionLine> getPotionLines() {
      ArrayList<PotionHud.PotionLine> lines = new ArrayList<>();
      if (mc.player == null) {
         return lines;
      } else {
         for (StatusEffectInstance effect : mc.player.getStatusEffects()) {
            String name = ((StatusEffect)effect.getEffectType().value()).getName().getString();
            int amp = effect.getAmplifier() + 1;
            String duration = getDuration(effect);
            String text = name + " " + amp + " \u00a7f" + duration;
            lines.add(new PotionHud.PotionLine(effect, text));
         }

         return lines;
      }
   }

   private boolean isRightSide() {
      return mc != null && mc.getWindow() != null ? this.getElementX() > mc.getWindow().getScaledWidth() / 2 : false;
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
      List<PotionHud.PotionLine> lines = this.getPotionLines();
      FontRenderer font = this.getPotionFont();
      int iconPart = this.showIcon.getValue() ? this.getPotionIconSize() + (int)this.spacing.getValue() : 0;
      int max = 50;

      for (PotionHud.PotionLine line : lines) {
         int w = (int)font.getWidth(line.text) + iconPart;
         if (w > max) {
            max = w;
         }
      }

      return max + 4;
   }

   @Override
   public int getElementHeight() {
      int count = Math.max(this.getPotionLines().size(), 1);
      return (int)((this.getPotionFontHeight() + 2.0F) * (float)count);
   }

   @Override
   public boolean isElementVisible() {
      return this.isOn();
   }

   @Override
   public void renderElement(DrawContext context, float tickDelta) {
      if (mc.player != null) {
         List<PotionHud.PotionLine> lines = this.getPotionLines();
         if (!lines.isEmpty()) {
            FontRenderer font = this.getPotionFont();
            float fontHeight = font.getFontHeight();
            int iconSize = this.getPotionIconSize();
            float iconYOff = this.iconOffset.getValueFloat();
            int gap = (int)this.spacing.getValue();
            boolean reverse = this.reverseOnRight.getValue() && this.isRightSide();
            boolean alignRight = this.rightAlign.getValue() && this.isRightSide();
            int totalWidth = this.getElementWidth() - 4;
            float y = (float)this.hudY;

            for (PotionHud.PotionLine line : lines) {
               int textWidth = (int)font.getWidth(line.text);
               int lineWidth = textWidth + (this.showIcon.getValue() ? iconSize + gap : 0);
               float baseX = alignRight ? (float)(this.hudX + totalWidth - lineWidth) : (float)this.hudX;
               float iconX;
               float textX;
               if (this.showIcon.getValue()) {
                  if (reverse) {
                     textX = baseX;
                     iconX = baseX + (float)textWidth + (float)gap;
                  } else {
                     iconX = baseX;
                     textX = baseX + (float)iconSize + (float)gap;
                  }
               } else {
                  textX = baseX;
                  iconX = baseX;
               }

               if (this.showIcon.getValue()) {
                  StatusEffectSpriteManager sm = Wrapper.mc.getStatusEffectSpriteManager();
                  Sprite sprite = sm.getSprite(line.effect.getEffectType());
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                  RenderSystem.enableBlend();
                  int iconY = (int)(y + (fontHeight - (float)iconSize) / 2.0F + iconYOff);
                  context.drawSprite((int)iconX, iconY, 0, iconSize, iconSize, sprite);
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               }

               font.drawString(
                  context.getMatrices(),
                  line.text,
                  textX,
                  y,
                  (float)this.color.getValue().getRed() / 255.0F,
                  (float)this.color.getValue().getGreen() / 255.0F,
                  (float)this.color.getValue().getBlue() / 255.0F,
                  (float)this.color.getValue().getAlpha() / 255.0F,
                  this.shadow.getValue()
               );
               y += fontHeight + 2.0F;
            }
         }
      }
   }

   private static class PotionLine {
      private final StatusEffectInstance effect;
      private final String text;

      private PotionLine(StatusEffectInstance effect, String text) {
         this.effect = effect;
         this.text = text;
      }
   }
}
