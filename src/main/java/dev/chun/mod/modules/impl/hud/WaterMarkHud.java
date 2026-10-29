package dev.chun.mod.modules.impl.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.chun.api.utils.skija.SkijaGlowRenderer;
import dev.chun.core.impl.FontManager;
import dev.chun.mod.gui.fonts.FontRenderer;
import dev.chun.mod.modules.HudElement;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.impl.client.Fonts;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.ColorSetting;
import dev.chun.mod.modules.settings.impl.EnumSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import dev.chun.mod.modules.settings.impl.StringSetting;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class WaterMarkHud extends Module implements HudElement {
   public static WaterMarkHud INSTANCE;
   private static final Identifier WURST_TEXTURE = Identifier.of("forever", "textures/images/wurst_128.png");
   private static final float WURST_LOGO_WIDTH = 72.0F;
   private static final float WURST_LOGO_HEIGHT = 18.0F;
   private static final String WURST_VERSION = "v0.01 MC1.21.1";
   private int hudX = 2;
   private int hudY = 2;
   public final EnumSetting<WaterMarkHud.Mode> mode = this.add(new EnumSetting<>("Mode", WaterMarkHud.Mode.Custom));
   public final EnumSetting<WaterMarkHud.ForeverFontMode> foreverFont = this.add(
      new EnumSetting<>("ForeverFont", WaterMarkHud.ForeverFontMode.Default, () -> this.mode.is(WaterMarkHud.Mode.Forever))
   );
   public final SliderSetting glitchInterval = this.add(
      new SliderSetting("GlitchInterval", 1050.0, 200.0, 3000.0, 50.0, () -> this.mode.is(WaterMarkHud.Mode.Forever))
   );
   public final SliderSetting glitchDuration = this.add(
      new SliderSetting("GlitchDuration", 230.0, 50.0, 1000.0, 10.0, () -> this.mode.is(WaterMarkHud.Mode.Forever))
   );
   public final SliderSetting jitterInterval = this.add(
      new SliderSetting("JitterInterval", 25.0, 10.0, 200.0, 5.0, () -> this.mode.is(WaterMarkHud.Mode.Forever))
   );
   private final StringSetting title = this.add(
      new StringSetting("Title", "%hackname% %version%", () -> this.mode.is(WaterMarkHud.Mode.Custom) || this.mode.is(WaterMarkHud.Mode.Forever))
   );
   private final SliderSetting size = this.add(
      new SliderSetting("Size", 8.0, 1.0, 20.0, 0.1, () -> this.mode.is(WaterMarkHud.Mode.Custom) || this.mode.is(WaterMarkHud.Mode.Forever))
   );
   private final BooleanSetting shadow = this.add(
      new BooleanSetting("Shadow", true, () -> this.mode.is(WaterMarkHud.Mode.Custom) || this.mode.is(WaterMarkHud.Mode.Forever))
   );
   private final ColorSetting color = this.add(new ColorSetting("Color", new Color(208, 0, 0), () -> this.mode.is(WaterMarkHud.Mode.Custom)));
   public final BooleanSetting artist = this.add(new BooleanSetting("Artist", false, () -> this.mode.is(WaterMarkHud.Mode.Custom)));
   private final BooleanSetting glow = this.add(new BooleanSetting("Glow", true, () -> this.mode.is(WaterMarkHud.Mode.Custom)).setParent());
   private final SliderSetting glowRadius = this.add(
      new SliderSetting("GlowRadius", 4.5, 0.1, 10.0, 0.1, () -> this.mode.is(WaterMarkHud.Mode.Custom) && this.glow.isOpen())
   );
   private final SliderSetting glowOpacity = this.add(
      new SliderSetting("GlowOpacity", 1.0, 0.1, 1.0, 0.05, () -> this.mode.is(WaterMarkHud.Mode.Custom) && this.glow.isOpen())
   );
   private final SliderSetting glowYOffset = this.add(
      new SliderSetting("GlowYOffset", 0.0, -20.0, 10.0, 0.1, () -> this.mode.is(WaterMarkHud.Mode.Custom) && this.glow.isOpen())
   );
   private final BooleanSetting glowGradient = this.add(
      new BooleanSetting("Gradient", false, () -> this.mode.is(WaterMarkHud.Mode.Custom) && this.glow.isOpen())
   );
   private final SliderSetting wurstLogoScale = this.add(new SliderSetting("LogoScale", 1.0, 0.5, 2.0, 0.05, () -> this.mode.is(WaterMarkHud.Mode.Wurst)));
   private Identifier currentGlowTexture = null;
   private String lastGlowConfig = "";
   private long lastGlowUpdate = 0L;
   private long lastGlitchTime = 0L;
   private long lastJitterTime = 0L;
   private boolean isGlitched = false;
   private float rgbOffsetL = 0.0F;
   private float rgbOffsetR = 0.0F;
   private final float[] sliceY = new float[3];
   private final float[] sliceH = new float[3];
   private final float[] sliceX = new float[3];

   public WaterMarkHud() {
      super("WaterMark", Module.Category.HUD);
      this.setChinese("\u6c34\u5370");
      INSTANCE = this;
   }

   public void setWatermarkText(String text) {
      this.title.setValue(text);
   }

   public String getWatermarkTextValue() {
      return this.title.getValue();
   }

   private String getWatermarkText() {
      return this.title.getValue().replaceAll("%version%", "4.0").replaceAll("%hackname%", "Alien");
   }

   private Color getCurrentColor() {
      return this.color.getValue();
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
      if (this.mode.is(WaterMarkHud.Mode.Wurst)) {
         return (int)this.getWurstWidth();
      } else if (this.mode.is(WaterMarkHud.Mode.Forever)) {
         return (int)this.getForeverFontRenderer().getWidth(this.getWatermarkText()) + 4;
      } else {
         FontRenderer artistFont;
         if (this.artist.getValue() && (artistFont = this.getArtistFont()) != null) {
            return (int)artistFont.getWidth(this.getWatermarkText()) + 4;
         } else {
            return this.isMinecraftFont()
               ? (int)((float)mc.textRenderer.getWidth(this.getWatermarkText()) * this.getMinecraftScale()) + 4
               : (int)this.getFont().getWidth(this.getWatermarkText()) + 4;
         }
      }
   }

   @Override
   public int getElementHeight() {
      if (this.mode.is(WaterMarkHud.Mode.Wurst)) {
         return (int)this.getWurstHeight();
      } else if (this.mode.is(WaterMarkHud.Mode.Forever)) {
         return (int)this.getForeverFontRenderer().getFontHeight() + 4;
      } else {
         FontRenderer artistFont;
         if (this.artist.getValue() && (artistFont = this.getArtistFont()) != null) {
            return (int)artistFont.getFontHeight() + 4;
         } else {
            return this.isMinecraftFont() ? (int)(9.0F * this.getMinecraftScale()) + 4 : (int)this.getFont().getFontHeight() + 4;
         }
      }
   }

   @Override
   public boolean isElementVisible() {
      return this.isOn();
   }

   @Override
   public void renderElement(DrawContext context, float tickDelta) {
      if (this.isOn()) {
         if (this.mode.is(WaterMarkHud.Mode.Wurst)) {
            this.cleanupGlowTexture();
            this.renderWurstWatermark(context);
         } else {
            String text = this.getWatermarkText();
            if (text == null || text.isEmpty()) {
               this.cleanupGlowTexture();
            } else if (this.mode.is(WaterMarkHud.Mode.Forever)) {
               this.cleanupGlowTexture();
               this.renderForeverWatermark(context, text);
            } else {
               this.renderWatermark(context, text);
            }
         }
      }
   }

   private void renderForeverWatermark(DrawContext context, String text) {
      MatrixStack ms = context.getMatrices();
      FontRenderer fr = this.getForeverFontRenderer();
      float textWidth = fr.getWidth(text);
      float textHeight = fr.getFontHeight();
      long time = System.currentTimeMillis();
      long interval = (long)this.glitchInterval.getValue();
      long duration = (long)this.glitchDuration.getValue();
      long jitterRate = (long)this.jitterInterval.getValue();
      long timeSinceLast = time - this.lastGlitchTime;
      if (timeSinceLast > interval) {
         this.lastGlitchTime = time;
         this.lastJitterTime = time;
         this.isGlitched = true;
         this.generateGlitchOffsets(textHeight);
      } else if (this.isGlitched && timeSinceLast > duration) {
         this.isGlitched = false;
         this.resetGlitchOffsets();
      } else if (this.isGlitched && time - this.lastJitterTime > jitterRate) {
         this.lastJitterTime = time;
         this.jitterGlitchOffsets(textHeight);
      }

      this.drawForeverRGBText(ms, fr, text, (float)this.hudX, (float)this.hudY, this.rgbOffsetL, this.rgbOffsetR);
      if (this.isGlitched) {
         for (int i = 0; i < 3; i++) {
            if (this.sliceX[i] != 0.0F) {
               int sx = this.hudX - 15;
               int sy = (int)((float)this.hudY + this.sliceY[i]);
               int sw = (int)textWidth + 30;
               int sh = (int)this.sliceH[i];
               context.enableScissor(sx, sy, sx + sw, sy + sh);
               this.drawForeverRGBText(ms, fr, text, (float)this.hudX + this.sliceX[i], (float)this.hudY, this.rgbOffsetL * 1.8F, this.rgbOffsetR * 1.8F);
               context.disableScissor();
            }
         }
      }
   }

   private void generateGlitchOffsets(float textHeight) {
      this.rgbOffsetL = (float)((Math.random() - 0.5) * 6.0);
      this.rgbOffsetR = (float)((Math.random() - 0.5) * 6.0);

      for (int i = 0; i < 3; i++) {
         this.sliceY[i] = (float)(Math.random() * (double)textHeight);
         this.sliceH[i] = (float)(Math.random() * ((double)textHeight / 2.5));
         this.sliceX[i] = (float)((Math.random() - 0.5) * 12.0);
      }
   }

   private void jitterGlitchOffsets(float textHeight) {
      this.rgbOffsetL = (float)((Math.random() - 0.5) * 6.0);
      this.rgbOffsetR = (float)((Math.random() - 0.5) * 6.0);
      if (Math.random() > 0.5) {
         for (int i = 0; i < 3; i++) {
            this.sliceX[i] = (float)((Math.random() - 0.5) * 12.0);
         }
      }
   }

   private void resetGlitchOffsets() {
      this.rgbOffsetL = 0.0F;
      this.rgbOffsetR = 0.0F;

      for (int i = 0; i < 3; i++) {
         this.sliceX[i] = 0.0F;
      }
   }

   private void drawForeverRGBText(MatrixStack ms, FontRenderer fr, String text, float x, float y, float offL, float offR) {
      RenderSystem.enableBlend();
      if (offL != 0.0F || offR != 0.0F) {
         fr.drawString(ms, text, x + offL, y, 0.0F, 1.0F, 1.0F, 0.75F, false);
         fr.drawString(ms, text, x + offR, y, 1.0F, 0.0F, 0.15F, 0.75F, false);
      }

      fr.drawString(ms, text, x, y, 1.0F, 1.0F, 1.0F, 1.0F, this.shadow.getValue());
   }

   private FontRenderer getForeverFontRenderer() {
      float s = this.size.getValueFloat();
      FontRenderer fr = null;
      if (this.foreverFont.is(WaterMarkHud.ForeverFontMode.OXANIUM)) {
         fr = FontManager.getOxaniumFont(s);
      } else if (this.foreverFont.is(WaterMarkHud.ForeverFontMode.ORBITRON)) {
         fr = FontManager.getOrbitronFont(s);
      }

      return fr != null ? fr : this.getFont();
   }

   private void renderWatermark(DrawContext context, String text) {
      MatrixStack ms = context.getMatrices();
      Color textColor = this.getCurrentColor();
      if (!this.glow.getValue()) {
         this.cleanupGlowTexture();
         this.drawPlainText(context, ms, text, textColor);
      } else if (!this.artist.getValue() && this.isMinecraftFont()) {
         this.cleanupGlowTexture();
         this.drawMinecraftGlow(context, ms, text, textColor);
      } else {
         Color glowColor = new Color(
            textColor.getRed(), textColor.getGreen(), textColor.getBlue(), Math.min(255, (int)((double)textColor.getAlpha() * this.glowOpacity.getValue()))
         );
         boolean artistFont = this.artist.getValue();
         String forceFont = artistFont ? "borel-regular.ttf" : null;
         float fontSize = this.size.getValueFloat();
         String config = String.format(
            "%s_%.1f_%.2f_%.1f_%08x_%08x_%b_%b_%s_%.1f_%s",
            text,
            this.glowRadius.getValue(),
            this.glowOpacity.getValue(),
            this.glowYOffset.getValue(),
            textColor.getRGB(),
            glowColor.getRGB(),
            this.glowGradient.getValue(),
            artistFont,
            FontManager.getMode(),
            fontSize,
            forceFont
         );
         boolean needsUpdate = this.currentGlowTexture == null
            || !config.equals(this.lastGlowConfig)
            || System.currentTimeMillis() - this.lastGlowUpdate > 5000L;
         if (needsUpdate) {
            this.cleanupGlowTexture();
            this.currentGlowTexture = SkijaGlowRenderer.createGlowTexture(
               text,
               fontSize,
               textColor,
               glowColor,
               this.glowRadius.getValueFloat(),
               this.glowGradient.getValue(),
               this.glowGradient.getValue() ? glowColor : null,
               forceFont
            );
            this.lastGlowConfig = config;
            this.lastGlowUpdate = System.currentTimeMillis();
         }

         if (this.currentGlowTexture != null && SkijaGlowRenderer.isTextureReady(this.currentGlowTexture)) {
            this.drawGlowTexture(context, ms, text);
         } else {
            this.drawPlainText(context, ms, text, textColor);
         }
      }
   }

   private void drawPlainText(DrawContext context, MatrixStack matrixStack, String text, Color textColor) {
      FontRenderer artistFont;
      if (this.artist.getValue() && (artistFont = this.getArtistFont()) != null) {
         artistFont.drawString(
            matrixStack,
            text,
            (float)this.hudX,
            (float)this.hudY,
            (float)textColor.getRed() / 255.0F,
            (float)textColor.getGreen() / 255.0F,
            (float)textColor.getBlue() / 255.0F,
            (float)textColor.getAlpha() / 255.0F,
            this.shadow.getValue()
         );
      } else if (this.isMinecraftFont()) {
         this.drawMinecraftText(context, matrixStack, text, textColor);
      } else {
         this.getFont()
            .drawString(
               matrixStack,
               text,
               (float)this.hudX,
               (float)this.hudY,
               (float)textColor.getRed() / 255.0F,
               (float)textColor.getGreen() / 255.0F,
               (float)textColor.getBlue() / 255.0F,
               (float)textColor.getAlpha() / 255.0F,
               this.shadow.getValue()
            );
      }
   }

   private void drawGlowTexture(DrawContext context, MatrixStack matrixStack, String text) {
      float textWidth = this.getTextWidth(text);
      float textHeight = this.getTextHeight();
      float glowPadding = this.glowRadius.getValueFloat() * 3.0F;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      matrixStack.push();
      matrixStack.translate((float)this.hudX - glowPadding, (float)this.hudY + this.glowYOffset.getValueFloat() - glowPadding, 0.0F);
      context.drawTexture(
         this.currentGlowTexture,
         0,
         0,
         0.0F,
         0.0F,
         (int)(textWidth + glowPadding * 2.0F),
         (int)(textHeight + glowPadding * 2.0F),
         (int)(textWidth + glowPadding * 2.0F),
         (int)(textHeight + glowPadding * 2.0F)
      );
      matrixStack.pop();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void renderWurstWatermark(DrawContext context) {
      MatrixStack matrixStack = context.getMatrices();
      float scale = this.wurstLogoScale.getValueFloat();
      float scaledWidth = 72.0F * scale;
      float scaledHeight = 18.0F * scale;
      float textScale = 0.95F;
      float textWidth = (float)mc.textRenderer.getWidth("v0.01 MC1.21.1") * textScale;
      float backgroundWidth = textWidth + scaledWidth + 3.0F;
      float backgroundHeight = 11.0F * scale;
      float logoY = (float)this.hudY + 3.0F * scale;
      float backgroundY = logoY + 7.0F - 2.0F * scale;
      context.fill(
         this.hudX, (int)backgroundY, (int)((float)this.hudX + backgroundWidth), (int)(backgroundY + backgroundHeight), new Color(255, 255, 255, 80).getRGB()
      );
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      matrixStack.push();
      matrixStack.translate((float)this.hudX, logoY, 0.0F);
      matrixStack.scale(scale, scale, 1.0F);
      context.drawTexture(WURST_TEXTURE, 0, 0, 0.0F, 0.0F, 72, 18, 72, 18);
      matrixStack.pop();
      matrixStack.push();
      matrixStack.translate((float)this.hudX + scaledWidth + scale, logoY + 7.0F, 0.0F);
      matrixStack.scale(textScale, textScale, 1.0F);
      context.drawText(mc.textRenderer, "v0.01 MC1.21.1", 0, 0, Color.WHITE.getRGB(), false);
      matrixStack.pop();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.disableBlend();
   }

   private float getWurstWidth() {
      float scale = this.wurstLogoScale.getValueFloat();
      float textScale = 0.95F;
      return (float)mc.textRenderer.getWidth("v0.01 MC1.21.1") * textScale + 72.0F * scale + 3.0F;
   }

   private float getWurstHeight() {
      return 21.0F * this.wurstLogoScale.getValueFloat();
   }

   private void drawMinecraftGlow(DrawContext context, MatrixStack matrixStack, String text, Color textColor) {
      float radius = this.glowRadius.getValueFloat();
      float yOffset = this.glowYOffset.getValueFloat();

      for (int i = Math.max(1, (int)Math.ceil((double)(radius * 2.0F))); i > 0; i--) {
         float offset = (float)i * 0.35F;
         int alpha = Math.min(255, (int)((double)textColor.getAlpha() * this.glowOpacity.getValue() / (double)(i + 1)));
         Color glowColor = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), alpha);
         this.drawMinecraftText(context, matrixStack, text, glowColor, -offset, yOffset - offset);
         this.drawMinecraftText(context, matrixStack, text, glowColor, offset, yOffset - offset);
         this.drawMinecraftText(context, matrixStack, text, glowColor, -offset, yOffset + offset);
         this.drawMinecraftText(context, matrixStack, text, glowColor, offset, yOffset + offset);
      }

      this.drawMinecraftText(context, matrixStack, text, textColor, 0.0F, yOffset);
   }

   private void drawMinecraftText(DrawContext context, MatrixStack matrixStack, String text, Color textColor) {
      this.drawMinecraftText(context, matrixStack, text, textColor, 0.0F, 0.0F);
   }

   private void drawMinecraftText(DrawContext context, MatrixStack matrixStack, String text, Color textColor, float offsetX, float offsetY) {
      float scale = this.getMinecraftScale();
      matrixStack.push();
      matrixStack.translate((float)this.hudX + offsetX, (float)this.hudY + offsetY, 0.0F);
      matrixStack.scale(scale, scale, 1.0F);
      context.drawText(mc.textRenderer, text, 0, 0, textColor.getRGB(), this.shadow.getValue());
      matrixStack.pop();
   }

   private FontRenderer getArtistFont() {
      return FontManager.getArtistFont(this.size.getValueFloat());
   }

   private FontRenderer getFont() {
      return FontManager.get(this.size.getValueFloat());
   }

   private float getTextWidth(String text) {
      FontRenderer artistFont;
      if (this.artist.getValue() && (artistFont = this.getArtistFont()) != null) {
         return artistFont.getWidth(text);
      } else {
         return this.isMinecraftFont() ? (float)mc.textRenderer.getWidth(text) * this.getMinecraftScale() : this.getFont().getWidth(text);
      }
   }

   private float getTextHeight() {
      FontRenderer artistFont;
      if (this.artist.getValue() && (artistFont = this.getArtistFont()) != null) {
         return artistFont.getFontHeight();
      } else {
         return this.isMinecraftFont() ? 9.0F * this.getMinecraftScale() : this.getFont().getFontHeight();
      }
   }

   private boolean isMinecraftFont() {
      return FontManager.getMode() == Fonts.FontMode.MINECRAFT;
   }

   private float getMinecraftScale() {
      return this.size.getValueFloat() / 8.0F;
   }

   private void cleanupGlowTexture() {
      if (this.currentGlowTexture != null) {
         SkijaGlowRenderer.cleanupTexture(this.currentGlowTexture);
         this.currentGlowTexture = null;
      }

      this.lastGlowConfig = "";
      this.lastGlowUpdate = 0L;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.cleanupGlowTexture();
   }

   public static enum ForeverFontMode {
      Default,
      OXANIUM,
      ORBITRON;

      private ForeverFontMode() {
      }
   }

   public static enum Mode {
      Custom,
      Wurst,
      Forever;

      private Mode() {
      }
   }
}
