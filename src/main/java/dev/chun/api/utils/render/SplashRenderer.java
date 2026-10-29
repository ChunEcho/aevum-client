package dev.chun.api.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.chun.mod.gui.fonts.FontRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public final class SplashRenderer {
   private static final int BG_TOP = -16447732;
   private static final int BG_BOT = -16052192;
   private static final int CYAN = -11544065;
   private static final int PURPLE = -6394881;
   private static final int WHITE = -1;
   private static final int TEXT_MAIN = -722945;
   private static final int TEXT_DIM = -7695446;
   private static final int TEXT_FAINT = -11906716;
   private static final String[] TIPS = new String[]{
      "ETERNITY IS A CHOICE", "TIME IS ON YOUR SIDE", "NOBODY LEAVES", "SILENCE CASTS FASTEST", "COUNT BEATS NOT SECONDS", "PATIENCE IS A WEAPON"
   };
   private static final long T0 = System.nanoTime();
   private static float displayed = 0.0F;
   private static String tipText = null;
   private static final int STAR_COUNT = 120;
   private static final float[][] STARS = buildStars();
   private static final int RING_DOTS = 56;
   private static final float TAU = (float) (Math.PI * 2);
   private static boolean TTF_ENABLED = true;
   private static boolean LOGGED = false;
   private static boolean PROBED = false;
   private static boolean TTF_USABLE = false;

   private SplashRenderer() {
   }

   private static void logOnce(String var0, Throwable var1) {
      if (!LOGGED) {
         LOGGED = true;
         System.out.println("[Aevum] " + var0 + ": " + var1);
      }
   }

   private static float[][] buildStars() {
      float[][] var0 = new float[120][4];
      long var1 = 2685821657736338717L;

      for (int var3 = 0; var3 < 120; var3++) {
         long var8 = var1 * 6364136223846793005L + 1442695040888963407L;
         float var4 = (float)(var8 >>> 11 & 16777215L) / 1.6777215E7F;
         long var9 = var8 * 6364136223846793005L + 1442695040888963407L;
         float var5 = (float)(var9 >>> 11 & 16777215L) / 1.6777215E7F;
         long var10 = var9 * 6364136223846793005L + 1442695040888963407L;
         float var6 = (float)(var10 >>> 11 & 255L) / 255.0F;
         var1 = var10 * 6364136223846793005L + 1442695040888963407L;
         float var7 = (float)(var1 >>> 11 & 65535L) / 65535.0F;
         var0[var3][0] = var4;
         var0[var3][1] = var5;
         var0[var3][2] = var6;
         var0[var3][3] = var7;
      }

      return var0;
   }

   private static float time() {
      return (float)(System.nanoTime() - T0) / 1.0E9F;
   }

   private static int withAlpha(int var0, int var1) {
      if (var1 < 0) {
         var1 = 0;
      }

      if (var1 > 255) {
         var1 = 255;
      }

      return var0 & 16777215 | var1 << 24;
   }

   private static int lerpColor(int var0, int var1, float var2) {
      return AevumFonts.lerpArgb(var0, var1, var2);
   }

   private static void blendOn() {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
   }

   public static void render(DrawContext var0, float var1, float var2) {
      if (!PROBED) {
         PROBED = true;

         try {
            boolean var3 = AevumFonts.available();
            FontRenderer var4 = AevumFonts.title();
            FontRenderer var5 = AevumFonts.body();
            float var6 = var4 != null ? var4.getWidth("AEVUM") : -1.0F;
            float var7 = var5 != null ? var5.getWidth("A") : -1.0F;
            TTF_USABLE = var3 && var6 > 1.0F && var7 > 0.5F;
            System.out.println("[Aevum] Splash probe: available=" + var3 + " titleW=" + var6 + " bodyW=" + var7 + " -> useTtf=" + TTF_USABLE);
         } catch (Throwable var10) {
            TTF_USABLE = false;
            System.out.println("[Aevum] Splash probe failed: " + var10);
         }
      }

      if (TTF_ENABLED && TTF_USABLE) {
         try {
            renderImpl(var0, var1, var2, true);
            return;
         } catch (Throwable var9) {
            TTF_ENABLED = false;
            logOnce("Splash: TTF path failed, falling back to pixel font", var9);
         }
      }

      try {
         renderImpl(var0, var1, var2, false);
      } catch (Throwable var8) {
         logOnce("Splash: render failed entirely", var8);
      }
   }

   private static void drawTitleText(MatrixStack var0, String var1, float var2, float var3, int var4, int var5, boolean var6) {
      blendOn();
      if (var6) {
         FontRenderer var7 = AevumFonts.title();
         float var8 = AevumFonts.width(var7, var1);
         AevumFonts.drawGradient(var7, var0, var1, var2 - var8 / 2.0F, var3, var4, var5, true);
      } else {
         PixelFont.drawCenteredGradientOutlined(var0, var1, var2, var3, 7.0F, var4, var5, -872151540);
      }
   }

   private static void drawBodyText(MatrixStack var0, String var1, float var2, float var3, int var4, boolean var5) {
      blendOn();
      if (var5) {
         AevumFonts.body().drawString(var0, var1, (double)var2, (double)var3, var4, false);
      } else {
         PixelFont.draw(var0, var1.toUpperCase(), var2, var3, 1.5F, var4);
      }
   }

   private static void drawBodyCentered(MatrixStack var0, String var1, float var2, float var3, int var4, boolean var5) {
      blendOn();
      if (var5) {
         AevumFonts.drawCentered(AevumFonts.body(), var0, var1, var2, var3, var4, false);
      } else {
         PixelFont.drawCentered(var0, var1.toUpperCase(), var2, var3, 1.5F, var4);
      }
   }

   private static void drawSmallCentered(MatrixStack var0, String var1, float var2, float var3, int var4, boolean var5) {
      blendOn();
      if (var5) {
         AevumFonts.drawCentered(AevumFonts.small(), var0, var1, var2, var3, var4, false);
      } else {
         PixelFont.drawCentered(var0, var1.toUpperCase(), var2, var3, 1.5F, var4);
      }
   }

   private static float measure(FontRenderer var0, String var1, float var2, boolean var3) {
      return var3 && var0 != null ? AevumFonts.width(var0, var1) : PixelFont.width(var1.toUpperCase(), var2);
   }

   private static void renderImpl(DrawContext var0, float var1, float var2, boolean var3) {
      int var4 = var0.getScaledWindowWidth();
      int var5 = var0.getScaledWindowHeight();
      MatrixStack var6 = var0.getMatrices();
      float var7 = time();
      float var8 = (float)var4 / 2.0F;
      float var9 = Math.max(0.0F, Math.min(1.0F, var1));
      if (var9 < displayed) {
         displayed = var9;
      } else {
         displayed = displayed + (var9 - displayed) * Math.min(1.0F, var2 * 5.0F);
      }

      blendOn();
      Render2DUtil.verticalGradient(var6, 0.0F, 0.0F, (float)var4, (float)var5, -16447732, -16052192);
      blendOn();

      for (int var10 = 0; var10 < 120; var10++) {
         float[] var11 = STARS[var10];
         float var12 = var11[0] * (float)var4;
         float var13 = var11[1] * (float)var5;
         float var14 = 0.5F + 0.5F * (float)Math.sin((double)var7 * (0.4 + (double)var11[3] * 1.3) + (double)(var11[3] * (float) (Math.PI * 2)));
         int var15 = (int)(10.0F + 120.0F * var14 * (0.3F + 0.7F * var11[2]));
         int var16 = lerpColor(withAlpha(-1, var15), withAlpha(-11544065, var15), var11[2] * 0.5F);
         float var17 = 0.7F + var11[2] * 1.1F;
         Render2DUtil.drawRect(var6, var12, var13, var17, var17, var16);
      }

      blendOn();
      Meteors.update(var2, (float)var4, (float)var5);
      Meteors.draw(var6);
      float var34 = (float)var5 * 0.4F;
      float var35 = (float)Math.min(var4, var5) * 0.245F;
      float var36 = 0.45F;
      float var37 = var7 * 0.5F;
      blendOn();

      for (int var38 = 0; var38 < 56; var38++) {
         float var40 = (float)var38 / 56.0F;
         float var42 = var40 * (float) (Math.PI * 2) + var37;
         float var44 = var8 + (float)Math.cos((double)var42) * var35;
         float var18 = var34 + (float)Math.sin((double)var42) * var35 * var36;
         float var19 = (1.0F - var40) % 1.0F;
         float var20 = (float)Math.pow((double)var19, 2.4);
         int var21 = (int)(12.0F + 145.0F * var20);
         int var22 = lerpColor(-11544065, -6394881, var40);
         float var23 = 1.2F + 1.1F * var20;
         Render2DUtil.drawRect(var6, var44, var18, var23, var23, withAlpha(var22, var21));
      }

      String var39 = "AEVUM";
      float var41 = var3 ? AevumFonts.title().getFontHeight("AEVUM") : PixelFont.height(7.0F);
      float var43 = var34 - var41 / 2.0F - 2.0F;
      if (var3) {
         blendOn();
         FontRenderer var45 = AevumFonts.title();
         float var47 = AevumFonts.width(var45, var39);

         for (int var49 = 3; var49 >= 1; var49--) {
            float var51 = (float)var49 * 1.5F;
            int var53 = 10 + (3 - var49) * 8;
            float var55 = 1.06F + (float)var49 * 0.01F;
            var6.push();
            var6.scale(var55, var55, 1.0F);
            AevumFonts.drawGradient(
               var45,
               var6,
               var39,
               (var8 - var47 / 2.0F - var51) / var55,
               (var43 - var51) / var55,
               withAlpha(-11544065, var53),
               withAlpha(-6394881, var53),
               false
            );
            var6.pop();
         }
      }

      float var46 = 0.5F + 0.5F * (float)Math.sin((double)(var7 * 1.0F));
      int var48 = lerpColor(-722945, -11544065, var46 * 0.45F);
      int var50 = lerpColor(-722945, -6394881, var46 * 0.45F);
      drawTitleText(var6, var39, var8, var43, var48, var50, var3);
      blendOn();
      float var52 = measure(var3 ? AevumFonts.title() : null, var39, 5.0F, var3);
      float var54 = 16.0F;
      float var56 = 58.0F;
      float var57 = var43 + var41 / 2.0F;
      Render2DUtil.horizontalGradient(
         var6, var8 - var52 / 2.0F - var54 - var56, var57, var8 - var52 / 2.0F - var54, var57 + 1.0F, withAlpha(-11544065, 0), withAlpha(-11544065, 120)
      );
      Render2DUtil.horizontalGradient(
         var6, var8 + var52 / 2.0F + var54, var57, var8 + var52 / 2.0F + var54 + var56, var57 + 1.0F, withAlpha(-11544065, 120), withAlpha(-11544065, 0)
      );
      drawBodyCentered(var6, "ETERNAL CLIENT", var8, var43 + var41 + 12.0F, withAlpha(-7695446, 225), var3);
      float var24 = Math.min(340.0F, (float)var4 * 0.38F);
      float var25 = 2.0F;
      float var26 = var8 - var24 / 2.0F;
      float var27 = (float)var5 * 0.655F;
      blendOn();
      Render2DUtil.drawRect(var6, var26, var27, var24, var25, withAlpha(-1, 22));
      float var28 = var24 * displayed;
      if (var28 > 0.5F) {
         blendOn();
         Render2DUtil.horizontalGradient(var6, var26, var27, var26 + var28, var27 + var25, withAlpha(-11544065, 255), withAlpha(-6394881, 255));
         Render2DUtil.drawRect(var6, var26 + var28 - 1.0F, var27 - 1.5F, 3.0F, var25 + 3.0F, withAlpha(-1, 210));
         Render2DUtil.drawRect(var6, var26 + var28 - 4.0F, var27 - 1.0F, 2.0F, var25 + 2.0F, withAlpha(-11544065, 100));
         Render2DUtil.drawRect(var6, var26 + var28 - 8.0F, var27 - 0.5F, 3.0F, var25 + 1.0F, withAlpha(-11544065, 45));
      }

      drawBodyText(var6, displayed >= 0.999F ? "READY" : "LOADING", var26, var27 - 14.0F, withAlpha(-11906716, 240), var3);
      String var29 = (int)(displayed * 100.0F) + "%";
      float var30 = measure(var3 ? AevumFonts.body() : null, var29, 1.5F, var3);
      drawBodyText(var6, var29, var26 + var24 - var30, var27 - 14.0F, withAlpha(-7695446, 240), var3);
      if (tipText == null) {
         tipText = TIPS[(int)(System.nanoTime() / 1000000L % (long)TIPS.length)];
      }

      float var31 = 95.0F + 45.0F * (float)Math.sin((double)(var7 * 0.8F));
      drawSmallCentered(var6, tipText, var8, (float)var5 - 34.0F, withAlpha(-11906716, (int)var31), var3);
      blendOn();
      Render2DUtil.horizontalGradient(var6, 0.0F, 0.0F, (float)var4, 1.0F, withAlpha(-11544065, 0), withAlpha(-11544065, 22));
      Render2DUtil.horizontalGradient(var6, 0.0F, (float)var5 - 1.0F, (float)var4, (float)var5, withAlpha(-11544065, 22), withAlpha(-11544065, 0));
      drawCorners(var6, var4, var5, var7);
      String var32 = "AEVUM 0.1";
      float var33 = measure(var3 ? AevumFonts.small() : null, var32, 1.5F, var3);
      blendOn();
      if (var3) {
         AevumFonts.small().drawString(var6, var32, (double)((float)var4 - var33 - 14.0F), (double)((float)var5 - 20.0F), withAlpha(-11906716, 150), false);
      } else {
         PixelFont.draw(var6, var32, (float)var4 - var33 - 14.0F, (float)var5 - 20.0F, 1.5F, withAlpha(-11906716, 150));
      }

      var0.draw();
   }

   private static void drawCorners(MatrixStack var0, int var1, int var2, float var3) {
      float var4 = 12.0F;
      float var5 = 22.0F;
      float var6 = 1.0F;
      int var7 = 65 + (int)(38.0 * Math.sin((double)(var3 * 1.4F)));
      int var8 = withAlpha(-11544065, var7);
      Render2DUtil.drawRect(var0, var4, var4, var5, var6, var8);
      Render2DUtil.drawRect(var0, var4, var4, var6, var5, var8);
      Render2DUtil.drawRect(var0, (float)var1 - var4 - var5, var4, var5, var6, var8);
      Render2DUtil.drawRect(var0, (float)var1 - var4 - var6, var4, var6, var5, var8);
      Render2DUtil.drawRect(var0, var4, (float)var2 - var4 - var6, var5, var6, var8);
      Render2DUtil.drawRect(var0, var4, (float)var2 - var4 - var5, var6, var5, var8);
      Render2DUtil.drawRect(var0, (float)var1 - var4 - var5, (float)var2 - var4 - var6, var5, var6, var8);
      Render2DUtil.drawRect(var0, (float)var1 - var4 - var6, (float)var2 - var4 - var5, var6, var5, var8);
   }
}
