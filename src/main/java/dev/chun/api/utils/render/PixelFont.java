package dev.chun.api.utils.render;

import net.minecraft.client.util.math.MatrixStack;

public final class PixelFont {
   public static final int GLYPH_W = 5;
   public static final int GLYPH_H = 7;
   public static final int SPACING = 1;

   private PixelFont() {
   }

   public static float width(String var0, float var1) {
      return var0 != null && !var0.isEmpty() ? (float)(var0.length() * 6) * var1 - 1.0F * var1 : 0.0F;
   }

   public static float height(float var0) {
      return 7.0F * var0;
   }

   public static void draw(MatrixStack var0, String var1, float var2, float var3, float var4, int var5) {
      if (var1 != null) {
         int var6 = var1.length();

         for (int var7 = 0; var7 < var6; var7++) {
            int[] var8 = glyph(Character.toUpperCase(var1.charAt(var7)));
            if (var8 != null) {
               float var9 = var2 + (float)(var7 * 6) * var4;

               for (int var10 = 0; var10 < 7; var10++) {
                  int var11 = var8[var10];
                  int var12 = 0;

                  while (var12 < 5) {
                     if ((var11 & 1 << 4 - var12) == 0) {
                        var12++;
                     } else {
                        int var13 = var12;

                        while (var12 < 5 && (var11 & 1 << 4 - var12) != 0) {
                           var12++;
                        }

                        int var14 = var12 - var13;
                        Render2DUtil.drawRect(var0, var9 + (float)var13 * var4, var3 + (float)var10 * var4, (float)var14 * var4, var4, var5);
                     }
                  }
               }
            }
         }
      }
   }

   public static void drawCentered(MatrixStack var0, String var1, float var2, float var3, float var4, int var5) {
      draw(var0, var1, var2 - width(var1, var4) / 2.0F, var3, var4, var5);
   }

   public static void drawGradient(MatrixStack var0, String var1, float var2, float var3, float var4, int var5, int var6) {
      if (var1 != null && !var1.isEmpty()) {
         int var7 = var1.length();
         float var8 = var2;

         for (int var9 = 0; var9 < var7; var9++) {
            String var10 = String.valueOf(var1.charAt(var9));
            float var11 = var7 == 1 ? 0.0F : (float)var9 / (float)(var7 - 1);
            int var12 = lerpArgb(var5, var6, var11);
            draw(var0, var10, var8, var3, var4, var12);
            var8 += 6.0F * var4;
         }
      }
   }

   public static void drawCenteredGradient(MatrixStack var0, String var1, float var2, float var3, float var4, int var5, int var6) {
      drawGradient(var0, var1, var2 - width(var1, var4) / 2.0F, var3, var4, var5, var6);
   }

   public static void drawOutlined(MatrixStack var0, String var1, float var2, float var3, float var4, int var5, int var6) {
      float var7 = Math.max(1.0F, var4 * 0.22F);

      for (int var8 = -1; var8 <= 1; var8++) {
         for (int var9 = -1; var9 <= 1; var9++) {
            if (var8 != 0 || var9 != 0) {
               draw(var0, var1, var2 + (float)var8 * var7, var3 + (float)var9 * var7, var4, var6);
            }
         }
      }

      draw(var0, var1, var2, var3, var4, var5);
   }

   public static void drawCenteredOutlined(MatrixStack var0, String var1, float var2, float var3, float var4, int var5, int var6) {
      drawOutlined(var0, var1, var2 - width(var1, var4) / 2.0F, var3, var4, var5, var6);
   }

   public static void drawCenteredGradientOutlined(MatrixStack var0, String var1, float var2, float var3, float var4, int var5, int var6, int var7) {
      float var8 = var2 - width(var1, var4) / 2.0F;
      float var9 = Math.max(1.0F, var4 * 0.22F);

      for (int var10 = -1; var10 <= 1; var10++) {
         for (int var11 = -1; var11 <= 1; var11++) {
            if (var10 != 0 || var11 != 0) {
               drawGradient(var0, var1, var8 + (float)var10 * var9, var3 + (float)var11 * var9, var4, var7, var7);
            }
         }
      }

      drawGradient(var0, var1, var8, var3, var4, var5, var6);
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
      return (int)((float)var3 + (float)(var7 - var3) * var2) << 24
         | (int)((float)var4 + (float)(var8 - var4) * var2) << 16
         | (int)((float)var5 + (float)(var9 - var5) * var2) << 8
         | (int)((float)var6 + (float)(var10 - var6) * var2);
   }

   private static int[] glyph(char var0) {
      switch (var0) {
         case ' ':
            return new int[]{0, 0, 0, 0, 0, 0, 0};
         case '!':
            return new int[]{4, 4, 4, 4, 4, 0, 4};
         case '"':
         case '#':
         case '$':
         case '&':
         case '(':
         case ')':
         case '*':
         case ',':
         case ';':
         case '<':
         case '=':
         case '>':
         case '@':
         default:
            return null;
         case '%':
            return new int[]{25, 26, 2, 4, 8, 11, 19};
         case '\'':
            return new int[]{4, 4, 0, 0, 0, 0, 0};
         case '+':
            return new int[]{0, 4, 4, 31, 4, 4, 0};
         case '-':
            return new int[]{0, 0, 0, 31, 0, 0, 0};
         case '.':
            return new int[]{0, 0, 0, 0, 0, 12, 12};
         case '/':
            return new int[]{1, 2, 2, 4, 8, 8, 16};
         case '0':
            return new int[]{14, 17, 19, 21, 25, 17, 14};
         case '1':
            return new int[]{4, 12, 4, 4, 4, 4, 14};
         case '2':
            return new int[]{14, 17, 1, 2, 4, 8, 31};
         case '3':
            return new int[]{31, 2, 4, 2, 1, 17, 14};
         case '4':
            return new int[]{2, 6, 10, 18, 31, 2, 2};
         case '5':
            return new int[]{31, 16, 30, 1, 1, 17, 14};
         case '6':
            return new int[]{6, 8, 16, 30, 17, 17, 14};
         case '7':
            return new int[]{31, 1, 2, 4, 8, 8, 8};
         case '8':
            return new int[]{14, 17, 17, 14, 17, 17, 14};
         case '9':
            return new int[]{14, 17, 17, 15, 1, 2, 12};
         case ':':
            return new int[]{0, 12, 12, 0, 12, 12, 0};
         case '?':
            return new int[]{14, 17, 1, 2, 4, 0, 4};
         case 'A':
            return new int[]{14, 17, 17, 31, 17, 17, 17};
         case 'B':
            return new int[]{30, 17, 17, 30, 17, 17, 30};
         case 'C':
            return new int[]{14, 17, 16, 16, 16, 17, 14};
         case 'D':
            return new int[]{30, 17, 17, 17, 17, 17, 30};
         case 'E':
            return new int[]{31, 16, 16, 30, 16, 16, 31};
         case 'F':
            return new int[]{31, 16, 16, 30, 16, 16, 16};
         case 'G':
            return new int[]{14, 17, 16, 23, 17, 17, 15};
         case 'H':
            return new int[]{17, 17, 17, 31, 17, 17, 17};
         case 'I':
            return new int[]{14, 4, 4, 4, 4, 4, 14};
         case 'J':
            return new int[]{7, 2, 2, 2, 2, 18, 12};
         case 'K':
            return new int[]{17, 18, 20, 24, 20, 18, 17};
         case 'L':
            return new int[]{16, 16, 16, 16, 16, 16, 31};
         case 'M':
            return new int[]{17, 27, 21, 21, 17, 17, 17};
         case 'N':
            return new int[]{17, 25, 21, 19, 17, 17, 17};
         case 'O':
            return new int[]{14, 17, 17, 17, 17, 17, 14};
         case 'P':
            return new int[]{30, 17, 17, 30, 16, 16, 16};
         case 'Q':
            return new int[]{14, 17, 17, 17, 21, 18, 13};
         case 'R':
            return new int[]{30, 17, 17, 30, 20, 18, 17};
         case 'S':
            return new int[]{15, 16, 16, 14, 1, 1, 30};
         case 'T':
            return new int[]{31, 4, 4, 4, 4, 4, 4};
         case 'U':
            return new int[]{17, 17, 17, 17, 17, 17, 14};
         case 'V':
            return new int[]{17, 17, 17, 17, 17, 10, 4};
         case 'W':
            return new int[]{17, 17, 17, 21, 21, 27, 17};
         case 'X':
            return new int[]{17, 17, 10, 4, 10, 17, 17};
         case 'Y':
            return new int[]{17, 17, 10, 4, 4, 4, 4};
         case 'Z':
            return new int[]{31, 1, 2, 4, 8, 16, 31};
      }
   }
}
