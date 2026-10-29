package dev.chun.api.utils.render;

import dev.chun.mod.gui.fonts.FontRenderer;
import java.awt.Font;
import java.io.InputStream;
import net.minecraft.client.util.math.MatrixStack;

public final class AevumFonts {
   private static boolean tried = false;
   private static boolean ok = false;
   private static FontRenderer title;
   private static FontRenderer body;
   private static FontRenderer small;

   private AevumFonts() {
   }

   private static void ensure() {
      if (!tried) {
         tried = true;

         try {
            title = create("orbitron", 26.0F);
            FontRenderer var0 = create("urbanist-variablefont_wght", 13.0F);
            FontRenderer var1 = create("urbanist-variablefont_wght", 9.5F);
            if (title == null) {
               title = create("urbanist-variablefont_wght", 26.0F);
            }

            body = var0;
            small = var1;
            ok = title != null && body != null && small != null;
         } catch (Throwable var2) {
            ok = false;
            System.out.println("[Aevum] AevumFonts unavailable: " + var2);
         }
      }
   }

   private static FontRenderer create(String var0, float var1) {
      try {
         FontRenderer var4;
         try (InputStream var2 = AevumFonts.class.getClassLoader().getResourceAsStream("assets/aevum/font/" + var0 + ".ttf")) {
            if (var2 == null) {
               return null;
            }

            Font var3 = Font.createFont(0, var2).deriveFont(0, var1);
            var4 = new FontRenderer(var3, var1);
         }

         return var4;
      } catch (Throwable var7) {
         return null;
      }
   }

   public static boolean available() {
      ensure();
      return ok;
   }

   public static FontRenderer title() {
      ensure();
      return title;
   }

   public static FontRenderer body() {
      ensure();
      return body;
   }

   public static FontRenderer small() {
      ensure();
      return small;
   }

   public static float width(FontRenderer var0, String var1) {
      if (var0 != null && var1 != null) {
         try {
            return var0.getWidth(var1);
         } catch (Throwable var3) {
            return 0.0F;
         }
      } else {
         return 0.0F;
      }
   }

   public static void drawCentered(FontRenderer var0, MatrixStack var1, String var2, float var3, float var4, int var5, boolean var6) {
      if (var0 != null) {
         float var7 = width(var0, var2);
         var0.drawString(var1, var2, (double)(var3 - var7 / 2.0F), (double)var4, var5, var6);
      }
   }

   public static void drawGradient(FontRenderer var0, MatrixStack var1, String var2, float var3, float var4, int var5, int var6, boolean var7) {
      if (var0 != null && var2 != null && !var2.isEmpty()) {
         int var8 = var2.length();
         float var9 = var3;

         for (int var10 = 0; var10 < var8; var10++) {
            char var11 = var2.charAt(var10);
            String var12 = String.valueOf(var11);
            float var13 = var8 == 1 ? 0.0F : (float)var10 / (float)(var8 - 1);
            int var14 = lerpArgb(var5, var6, var13);
            float var15 = (float)(var14 >> 16 & 0xFF) / 255.0F;
            float var16 = (float)(var14 >> 8 & 0xFF) / 255.0F;
            float var17 = (float)(var14 & 0xFF) / 255.0F;
            float var18 = (float)(var14 >>> 24 & 0xFF) / 255.0F;
            var0.drawString(var1, var12, var9, var4, var15, var16, var17, var18, var7);
            var9 += width(var0, var12);
         }
      }
   }

   public static void drawCenteredGradient(FontRenderer var0, MatrixStack var1, String var2, float var3, float var4, int var5, int var6, boolean var7) {
      if (var0 != null) {
         float var8 = width(var0, var2);
         drawGradient(var0, var1, var2, var3 - var8 / 2.0F, var4, var5, var6, var7);
      }
   }

   public static int lerpArgb(int var0, int var1, float var2) {
      if (var2 < 0.0F) {
         var2 = 0.0F;
      }

      if (var2 > 1.0F) {
         var2 = 1.0F;
      }

      int var3 = var0 >>> 24 & 0xFF;
      int var4 = var0 >> 16 & 0xFF;
      int var5 = var0 >> 8 & 0xFF;
      int var6 = var0 & 0xFF;
      int var7 = var1 >>> 24 & 0xFF;
      int var8 = var1 >> 16 & 0xFF;
      int var9 = var1 >> 8 & 0xFF;
      int var10 = var1 & 0xFF;
      int var11 = (int)((float)var3 + (float)(var7 - var3) * var2);
      int var12 = (int)((float)var4 + (float)(var8 - var4) * var2);
      int var13 = (int)((float)var5 + (float)(var9 - var5) * var2);
      int var14 = (int)((float)var6 + (float)(var10 - var6) * var2);
      return var11 << 24 | var12 << 16 | var13 << 8 | var14;
   }
}
