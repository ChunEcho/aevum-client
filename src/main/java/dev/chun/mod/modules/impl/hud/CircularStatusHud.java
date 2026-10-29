package dev.chun.mod.modules.impl.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.chun.api.utils.skija.SkijaGlowRenderer;
import dev.chun.mod.modules.HudElement;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.ColorSetting;
import dev.chun.mod.modules.settings.impl.EnumSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

public class CircularStatusHud extends Module implements HudElement {
   public static CircularStatusHud INSTANCE;
   private float currentFade = 1.0F;
   private long lastRenderTime = 0L;
   public final BooleanSetting hideVanillaCrosshair = this.add(new BooleanSetting("HideVanillaCrosshair", true));
   private final EnumSetting<CircularStatusHud.CrosshairMode> crosshairMode = this.add(
      new EnumSetting<>("CrosshairMode", CircularStatusHud.CrosshairMode.SolidCircle, () -> this.hideVanillaCrosshair.getValue())
   );
   private final SliderSetting crosshairSize = this.add(
      new SliderSetting(
         "CrosshairSize",
         4.0,
         1.0,
         20.0,
         0.5,
         () -> this.hideVanillaCrosshair.getValue() && this.crosshairMode.getValue() != CircularStatusHud.CrosshairMode.None
      )
   );
   private final ColorSetting crosshairColor = this.add(new ColorSetting("CrosshairColor", new Color(255, 255, 255, 255)));
   private final SliderSetting crosshairThick = this.add(
      new SliderSetting(
         "CrosshairThick",
         1.5,
         0.5,
         5.0,
         0.1,
         () -> this.hideVanillaCrosshair.getValue() && this.crosshairMode.getValue() == CircularStatusHud.CrosshairMode.Arcs
      )
   );
   private final SliderSetting crosshairGap = this.add(
      new SliderSetting(
         "CrosshairGap",
         30.0,
         0.0,
         80.0,
         1.0,
         () -> this.hideVanillaCrosshair.getValue() && this.crosshairMode.getValue() == CircularStatusHud.CrosshairMode.Arcs
      )
   );
   private final SliderSetting radius = this.add(new SliderSetting("Radius", 25.0, 10.0, 100.0, 1.0));
   private final SliderSetting thickness = this.add(new SliderSetting("Thickness", 4.0, 1.0, 20.0, 0.5));
   private final SliderSetting gap = this.add(new SliderSetting("GapDegree", 10.0, 0.0, 30.0, 1.0));
   private final SliderSetting glow = this.add(new SliderSetting("Glow", 8.0, 0.0, 20.0, 0.5));
   private final SliderSetting glowIntensity = this.add(new SliderSetting("GlowIntensity", 2.0, 1.0, 5.0, 1.0));
   private final SliderSetting globalAlpha = this.add(new SliderSetting("GlobalAlpha", 1.0, 0.1, 1.0, 0.01));
   private final SliderSetting fadeRange = this.add(new SliderSetting("FadeRange", 20.0, 0.0, 120.0, 1.0).setSuffix("m"));
   private final ColorSetting bgColor = this.add(new ColorSetting("BgColor", new Color(40, 40, 40, 150)));
   private final ColorSetting healthColor = this.add(new ColorSetting("HealthColor", new Color(255, 50, 50, 255)));
   private final ColorSetting absorbColor = this.add(new ColorSetting("AbsorbColor", new Color(255, 200, 0, 255)));
   private final ColorSetting turtleColor = this.add(new ColorSetting("TurtleColor", new Color(0, 200, 150, 255)));

   public CircularStatusHud() {
      super("CircularStatus", Module.Category.HUD);
      this.setChinese("\u73af\u5f62\u72b6\u6001\u680f");
      INSTANCE = this;
   }

   public static boolean shouldHideVanillaCrosshair() {
      return INSTANCE != null && INSTANCE.isOn() && INSTANCE.hideVanillaCrosshair.getValue();
   }

   @Override
   public void onRender2D(DrawContext drawContext, float tickDelta) {
      this.renderElement(drawContext, tickDelta);
   }

   @Override
   public void renderElement(DrawContext context, float tickDelta) {
      if (mc.player != null && mc.getWindow() != null) {
         int centerX = mc.getWindow().getScaledWidth() / 2;
         int centerY = mc.getWindow().getScaledHeight() / 2;
         if (this.hideVanillaCrosshair.getValue() && this.crosshairMode.getValue() != CircularStatusHud.CrosshairMode.None) {
            float size = this.crosshairSize.getValueFloat();
            if (this.crosshairMode.getValue() == CircularStatusHud.CrosshairMode.Square) {
               float offset = size / 2.0F;
               context.fill(
                  (int)((float)centerX - offset),
                  (int)((float)centerY - offset),
                  (int)((float)centerX - offset + size),
                  (int)((float)centerY - offset + size),
                  this.crosshairColor.getValue().getRGB()
               );
            } else if (this.crosshairMode.getValue() == CircularStatusHud.CrosshairMode.SolidCircle) {
               float r = size / 4.0F;
               float thick = size / 2.0F;
               this.drawSkijaArc(context, (float)centerX, (float)centerY, r, thick, 0.0F, 360.0F, this.crosshairColor.getValue(), 0.0F, 0, 1.0F);
            } else if (this.crosshairMode.getValue() == CircularStatusHud.CrosshairMode.Arcs) {
               float thick = this.crosshairThick.getValueFloat();
               float gapDeg = this.crosshairGap.getValueFloat();
               float sweep = 90.0F - gapDeg;
               if (sweep > 0.0F) {
                  for (float cAngle : new float[]{0.0F, 90.0F, 180.0F, 270.0F}) {
                     float start = cAngle - sweep / 2.0F;
                     this.drawSkijaArc(context, (float)centerX, (float)centerY, size, thick, start, sweep, this.crosshairColor.getValue(), 0.0F, 0, 1.0F);
                  }
               }
            }
         }

         long currentTime = System.currentTimeMillis();
         if (this.lastRenderTime == 0L) {
            this.lastRenderTime = currentTime;
         }

         long delta = currentTime - this.lastRenderTime;
         this.lastRenderTime = currentTime;
         float fadeSpeed = (float)delta / 300.0F;
         this.currentFade = this.hasPlayerInRange() ? Math.min(1.0F, this.currentFade + fadeSpeed) : Math.max(0.0F, this.currentFade - fadeSpeed);
         if (!(this.currentFade <= 0.0F)) {
            float gR = this.glow.getValueFloat();
            int gInt = this.glowIntensity.getValueInt();
            float r = this.radius.getValueFloat();
            float t = this.thickness.getValueFloat();
            float g = this.gap.getValueFloat();
            float maxSweep = 120.0F - g;
            float leftBgStart = 270.0F - g / 2.0F;
            float leftBgSweep = -maxSweep;
            int hpSteps = Math.round(mc.player.getHealth());
            float hpRatio = MathHelper.clamp((float)hpSteps / 20.0F, 0.0F, 1.0F);
            float leftSweep = -maxSweep * hpRatio;
            float rightBgStart = 30.0F - g / 2.0F;
            float rightBgSweep = -maxSweep;
            int abSteps = Math.round(mc.player.getAbsorptionAmount());
            float absorbRatio = MathHelper.clamp((float)abSteps / 16.0F, 0.0F, 1.0F);
            float rightSweep = -maxSweep * absorbRatio;
            float bottomBgStart = 30.0F + g / 2.0F;
            float turtleRatio = 0.0F;
            StatusEffectInstance resistance = mc.player.getStatusEffect(StatusEffects.RESISTANCE);
            if (resistance != null && resistance.getAmplifier() >= 2) {
               int ticks = resistance.getDuration();
               int turtleSteps = Math.round((float)ticks / 400.0F * 100.0F);
               turtleRatio = MathHelper.clamp((float)turtleSteps / 100.0F, 0.0F, 1.0F);
            }

            float bottomSweep = maxSweep * turtleRatio;
            float currentGlobalAlpha = this.currentFade * this.globalAlpha.getValueFloat();
            if (this.bgColor.getValue().getAlpha() > 0) {
               this.drawSkijaArc(context, (float)centerX, (float)centerY, r, t, leftBgStart, leftBgSweep, this.bgColor.getValue(), gR, gInt, currentGlobalAlpha);
               this.drawSkijaArc(
                  context, (float)centerX, (float)centerY, r, t, rightBgStart, rightBgSweep, this.bgColor.getValue(), gR, gInt, currentGlobalAlpha
               );
               this.drawSkijaArc(context, (float)centerX, (float)centerY, r, t, bottomBgStart, maxSweep, this.bgColor.getValue(), gR, gInt, currentGlobalAlpha);
            }

            if (hpRatio > 0.0F) {
               this.drawSkijaArc(
                  context, (float)centerX, (float)centerY, r, t, leftBgStart, leftSweep, this.healthColor.getValue(), gR, gInt, currentGlobalAlpha
               );
            }

            if (absorbRatio > 0.0F) {
               this.drawSkijaArc(
                  context, (float)centerX, (float)centerY, r, t, rightBgStart, rightSweep, this.absorbColor.getValue(), gR, gInt, currentGlobalAlpha
               );
            }

            if (turtleRatio > 0.0F) {
               this.drawSkijaArc(
                  context, (float)centerX, (float)centerY, r, t, bottomBgStart, bottomSweep, this.turtleColor.getValue(), gR, gInt, currentGlobalAlpha
               );
            }
         }
      }
   }

   private void drawSkijaArc(
      DrawContext context,
      float centerX,
      float centerY,
      float radius,
      float thickness,
      float startAngle,
      float sweepAngle,
      Color color,
      float glowRad,
      int glowIntensity,
      float alphaMultiplier
   ) {
      if (!(Math.abs(sweepAngle) < 0.1F)) {
         if (!SkijaGlowRenderer.isAvailable()) {
            this.drawFallbackArc(context, centerX, centerY, radius, thickness, startAngle, sweepAngle, color, alphaMultiplier);
         } else {
            Color pureColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
            Identifier tex = SkijaGlowRenderer.createArcTexture(radius, thickness, startAngle, sweepAngle, pureColor, glowRad, glowIntensity);
            if (!SkijaGlowRenderer.isTextureReady(tex)) {
               this.drawFallbackArc(context, centerX, centerY, radius, thickness, startAngle, sweepAngle, color, alphaMultiplier);
            } else {
               int[] size = SkijaGlowRenderer.getTextureSize(tex);
               if (size == null) {
                  this.drawFallbackArc(context, centerX, centerY, radius, thickness, startAngle, sweepAngle, color, alphaMultiplier);
               } else {
                  int texWidth = size[0];
                  int texHeight = size[1];
                  double scale = mc.getWindow().getScaleFactor();
                  float displayWidth = (float)((double)texWidth / scale);
                  float displayHeight = (float)((double)texHeight / scale);
                  float drawX = centerX - displayWidth / 2.0F;
                  float drawY = centerY - displayHeight / 2.0F;
                  float finalAlpha = (float)color.getAlpha() / 255.0F * alphaMultiplier;
                  RenderSystem.enableBlend();
                  RenderSystem.defaultBlendFunc();
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, finalAlpha);
                  context.drawTexture(tex, (int)drawX, (int)drawY, 0.0F, 0.0F, (int)displayWidth, (int)displayHeight, (int)displayWidth, (int)displayHeight);
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               }
            }
         }
      }
   }

   private void drawFallbackArc(
      DrawContext context, float centerX, float centerY, float radius, float thickness, float startAngle, float sweepAngle, Color color, float alphaMultiplier
   ) {
      float alpha = MathHelper.clamp((float)color.getAlpha() / 255.0F * alphaMultiplier, 0.0F, 1.0F);
      if (!(alpha <= 0.01F)) {
         int argb = (int)(alpha * 255.0F) << 24 | color.getRed() << 16 | color.getGreen() << 8 | color.getBlue();
         float outer = Math.max(0.5F, radius + thickness / 2.0F);
         float inner = Math.max(0.0F, radius - thickness / 2.0F);
         int segments = Math.max(8, (int)(Math.abs(sweepAngle) / 4.0F));
         Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         RenderSystem.setShader(GameRenderer::getPositionColorProgram);
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

         for (int i = 0; i <= segments; i++) {
            float t = (float)i / (float)segments;
            double angle = Math.toRadians((double)(startAngle + sweepAngle * t));
            float cos = (float)Math.cos(angle);
            float sin = (float)Math.sin(angle);
            buffer.vertex(matrix, centerX + cos * outer, centerY + sin * outer, 0.0F).color(argb);
            buffer.vertex(matrix, centerX + cos * inner, centerY + sin * inner, 0.0F).color(argb);
         }

         BufferRenderer.drawWithGlobalProgram(buffer.end());
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
      }
   }

   private boolean hasPlayerInRange() {
      if (mc.world != null && mc.player != null) {
         float range = this.fadeRange.getValueFloat();
         if (range <= 0.0F) {
            return true;
         } else {
            float rangeSq = range * range;

            for (PlayerEntity player : mc.world.getPlayers()) {
               if (player != mc.player && player.isAlive() && player.squaredDistanceTo(mc.player) <= (double)rangeSq) {
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public String getElementName() {
      return this.getChinese() != null ? this.getChinese() : this.getName();
   }

   @Override
   public int getElementX() {
      return mc.getWindow() == null ? 0 : mc.getWindow().getScaledWidth() / 2 - this.getElementWidth() / 2;
   }

   @Override
   public void setElementX(int x) {
   }

   @Override
   public int getElementY() {
      return mc.getWindow() == null ? 0 : mc.getWindow().getScaledHeight() / 2 - this.getElementHeight() / 2;
   }

   @Override
   public void setElementY(int y) {
   }

   @Override
   public int getElementWidth() {
      return (int)(this.radius.getValueFloat() * 2.0F);
   }

   @Override
   public int getElementHeight() {
      return (int)(this.radius.getValueFloat() * 2.0F);
   }

   @Override
   public boolean isElementVisible() {
      return this.isOn();
   }

   public static enum CrosshairMode {
      None,
      Square,
      Arcs,
      SolidCircle;

      private CrosshairMode() {
      }
   }
}
