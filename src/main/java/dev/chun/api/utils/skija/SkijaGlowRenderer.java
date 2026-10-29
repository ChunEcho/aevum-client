package dev.chun.api.utils.skija;

import dev.chun.core.impl.FontManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D.Float;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.NativeImage.Format;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public final class SkijaGlowRenderer {
   private static final Map<Identifier, NativeImageBackedTexture> TEXTURES = new ConcurrentHashMap<>();
   private static final Map<Identifier, int[]> SIZES = new ConcurrentHashMap<>();
   private static int counter = 0;
   private static boolean disabled = false;

   private SkijaGlowRenderer() {
   }

   public static boolean isAvailable() {
      return !disabled;
   }

   public static void disable(Throwable t) {
      disabled = true;
   }

   public static Identifier createGlowTexture(
      String text, float fontSize, Color textColor, Color glowColor, float glowRadius, boolean useGradient, Color gradientEndColor
   ) {
      return createGlowTexture(text, fontSize, textColor, glowColor, glowRadius, useGradient, gradientEndColor, null);
   }

   public static Identifier createGlowTexture(
      String text, float fontSize, Color textColor, Color glowColor, float glowRadius, boolean useGradient, Color gradientEndColor, String forceFontFile
   ) {
      if (!disabled && text != null && !text.isEmpty()) {
         try {
            int pad = Math.max(2, (int)Math.ceil((double)glowRadius * 3.0));
            Font font = pickFont(forceFontFile, (int)Math.max(1.0F, fontSize));
            BufferedImage measure = new BufferedImage(1, 1, 2);
            Graphics2D mg = measure.createGraphics();
            mg.setFont(font);
            FontMetrics fm = mg.getFontMetrics();
            int textWidth = Math.max(1, fm.stringWidth(text));
            int textHeight = Math.max(1, fm.getHeight());
            mg.dispose();
            int width = textWidth + pad * 2;
            int height = textHeight + pad * 2;
            BufferedImage glowLayer = new BufferedImage(width, height, 2);
            Graphics2D g = glowLayer.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(font);
            g.setColor(glowColor);
            g.drawString(text, pad, pad + fm.getAscent());
            g.dispose();
            BufferedImage out = glowRadius > 0.0F ? blur(glowLayer, glowRadius) : glowLayer;
            Graphics2D c = out.createGraphics();
            c.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            c.setFont(font);
            if (useGradient && gradientEndColor != null) {
               c.setPaint(new GradientPaint(0.0F, 0.0F, textColor, 0.0F, (float)height, gradientEndColor));
            } else {
               c.setColor(textColor);
            }

            c.drawString(text, pad, pad + fm.getAscent());
            c.dispose();
            return register(out);
         } catch (Throwable var21) {
            disable(var21);
            return null;
         }
      } else {
         return null;
      }
   }

   public static Identifier createArcTexture(
      float radius, float thickness, float startAngle, float sweepAngle, Color color, float glowRadius, int glowIntensity
   ) {
      if (disabled) {
         return null;
      } else {
         try {
            float r = Math.max(1.0F, radius);
            float t = Math.max(1.0F, thickness);
            float extra = Math.max(0.0F, glowRadius) * 2.5F + 4.0F;
            int size = (int)Math.ceil((double)((r + t / 2.0F + extra) * 2.0F));
            BufferedImage layer = new BufferedImage(size, size, 2);
            Graphics2D g = layer.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(t, 1, 1));
            g.setColor(color);
            float cx = (float)size / 2.0F;
            float cy = (float)size / 2.0F;
            g.draw(new Float(cx - r, cy - r, r * 2.0F, r * 2.0F, -startAngle - sweepAngle, sweepAngle, 0));
            g.dispose();
            BufferedImage out = glowRadius > 0.0F ? blur(layer, glowRadius) : layer;
            return register(out);
         } catch (Throwable var16) {
            disable(var16);
            return null;
         }
      }
   }

   public static Identifier createGlowDiamondTexture(float size, Color innerColor, Color glowColor, float glowRadius) {
      if (disabled) {
         return null;
      } else {
         try {
            float s = Math.max(2.0F, size);
            float extra = Math.max(0.0F, glowRadius) * 2.5F + 4.0F;
            int px = (int)Math.ceil((double)((s + extra) * 2.0F));
            BufferedImage layer = new BufferedImage(px, px, 2);
            Graphics2D g = layer.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            float cx = (float)px / 2.0F;
            float cy = (float)px / 2.0F;
            java.awt.geom.Path2D.Float path = new java.awt.geom.Path2D.Float();
            path.moveTo(cx, cy - s);
            path.lineTo(cx + s, cy);
            path.lineTo(cx, cy + s);
            path.lineTo(cx - s, cy);
            path.closePath();
            g.setColor(glowColor);
            g.fill(path);
            g.dispose();
            BufferedImage out = glowRadius > 0.0F ? blur(layer, glowRadius) : layer;
            Graphics2D c = out.createGraphics();
            c.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            c.setColor(innerColor);
            c.fill(path);
            c.dispose();
            return register(out);
         } catch (Throwable var14) {
            disable(var14);
            return null;
         }
      }
   }

   public static boolean isTextureReady(Identifier id) {
      return id != null && TEXTURES.containsKey(id);
   }

   public static int[] getTextureSize(Identifier id) {
      int[] arr = SIZES.get(id);
      return arr == null ? new int[]{0, 0} : arr;
   }

   public static void renderSimpleGlow(
      MatrixStack matrices, String text, float x, float y, Color color, float scale, float glowRadius, int layers, boolean shadow
   ) {
      if (FontManager.ui != null) {
         FontManager.draw(matrices, text, (double)x, (double)y, color.getRGB(), shadow);
      }
   }

   public static void cleanupTexture(Identifier id) {
      if (id != null) {
         TEXTURES.remove(id);
         SIZES.remove(id);

         try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.getTextureManager() != null) {
               mc.getTextureManager().destroyTexture(id);
            }
         } catch (Throwable var2) {
         }
      }
   }

   public static void clearAllTextures() {
      for (Identifier id : new ArrayList<>(TEXTURES.keySet())) {
         cleanupTexture(id);
      }
   }

   private static Font pickFont(String forceFontFile, int size) {
      if (forceFontFile != null && !forceFontFile.isEmpty()) {
         try {
            String path = forceFontFile.startsWith("assets/") ? forceFontFile : "assets/aevum/font/" + forceFontFile;
            InputStream in = SkijaGlowRenderer.class.getClassLoader().getResourceAsStream(path);
            if (in != null) {
               return Font.createFont(0, in).deriveFont((float)size);
            }
         } catch (Throwable var5) {
         }
      }

      try {
         Font f = new Font("Microsoft YaHei", 0, size);
         if (f.getFamily() != null) {
            return f;
         }
      } catch (Throwable var4) {
      }

      return new Font("SansSerif", 0, size);
   }

   private static BufferedImage blur(BufferedImage src, float radius) {
      int r = Math.max(1, Math.min(12, (int)Math.ceil((double)radius)));
      int k = r * 2 + 1;
      float[] kernel = new float[k * k];
      float sigma = Math.max(0.1F, radius / 2.0F);
      float sum = 0.0F;

      for (int y = -r; y <= r; y++) {
         for (int x = -r; x <= r; x++) {
            float v = (float)Math.exp(-((double)(x * x + y * y)) / (double)(2.0F * sigma * sigma));
            kernel[(y + r) * k + x + r] = v;
            sum += v;
         }
      }

      for (int i = 0; i < kernel.length; i++) {
         kernel[i] /= sum;
      }

      BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), 2);
      new ConvolveOp(new Kernel(k, k, kernel), 1, null).filter(src, dst);
      return dst;
   }

   private static Identifier register(BufferedImage img) {
      try {
         int w = img.getWidth();
         int h = img.getHeight();
         NativeImage ni = new NativeImage(Format.RGBA, w, h, false);
         int[] row = new int[w];

         for (int y = 0; y < h; y++) {
            img.getRGB(0, y, w, 1, row, 0, w);

            for (int x = 0; x < w; x++) {
               int argb = row[x];
               int a = argb >>> 24 & 0xFF;
               int rr = argb >> 16 & 0xFF;
               int gg = argb >> 8 & 0xFF;
               int bb = argb & 0xFF;
               int abgr = a << 24 | bb << 16 | gg << 8 | rr;
               ni.setColor(x, y, abgr);
            }
         }

         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc == null) {
            ni.close();
            return null;
         } else {
            Identifier id = Identifier.of("aevum", "skija/glow_" + counter++);
            NativeImageBackedTexture tex = new NativeImageBackedTexture(ni);
            mc.getTextureManager().registerTexture(id, tex);
            TEXTURES.put(id, tex);
            SIZES.put(id, new int[]{w, h});
            return id;
         }
      } catch (Throwable var13) {
         return null;
      }
   }
}
