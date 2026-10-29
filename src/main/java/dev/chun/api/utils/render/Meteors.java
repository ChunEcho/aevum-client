package dev.chun.api.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.util.math.MatrixStack;

public final class Meteors {
   private static final List<Meteors.Meteor> LIST = new ArrayList<>();
   private static final Random RNG = new Random(1592598566L);
   private static final int MAX = 7;

   private Meteors() {
   }

   public static void reset() {
      LIST.clear();
   }

   public static void update(float var0, float var1, float var2) {
      if (var0 <= 0.0F) {
         var0 = 0.016F;
      }

      if (var0 > 0.2F) {
         var0 = 0.2F;
      }

      if (LIST.size() < 7 && RNG.nextFloat() < var0 * 1.6F) {
         spawn(var1, var2);
      }

      for (int var3 = LIST.size() - 1; var3 >= 0; var3--) {
         Meteors.Meteor var4 = LIST.get(var3);
         var4.x = var4.x + var4.vx * var0;
         var4.y = var4.y + var4.vy * var0;
         var4.life -= var0;
         if (var4.life <= 0.0F || var4.x < -160.0F || var4.x > var1 + 160.0F || var4.y < -160.0F || var4.y > var2 + 160.0F) {
            LIST.remove(var3);
         }
      }
   }

   private static void spawn(float var0, float var1) {
      Meteors.Meteor var2 = new Meteors.Meteor();
      boolean var3 = RNG.nextBoolean();
      if (var3) {
         var2.x = RNG.nextFloat() * (var0 + 200.0F) - 100.0F;
         var2.y = -40.0F - RNG.nextFloat() * 60.0F;
      } else {
         var2.x = var0 + 40.0F + RNG.nextFloat() * 60.0F;
         var2.y = RNG.nextFloat() * var1 * 0.6F;
      }

      float var4 = 150.0F + RNG.nextFloat() * 190.0F;
      double var5 = Math.toRadians((double)(200.0F + RNG.nextFloat() * 40.0F));
      var2.vx = (float)(Math.cos(var5) * (double)var4);
      var2.vy = (float)(-Math.sin(var5) * (double)var4) * -1.0F;
      var2.maxLife = 1.6F + RNG.nextFloat() * 1.8F;
      var2.life = var2.maxLife;
      var2.size = 1.6F + RNG.nextFloat() * 1.8F;
      float var7 = RNG.nextFloat();
      if (var7 < 0.62F) {
         var2.color = -1;
      } else if (var7 < 0.88F) {
         var2.color = -6297345;
      } else {
         var2.color = -8024;
      }

      LIST.add(var2);
   }

   public static void draw(MatrixStack var0) {
      if (!LIST.isEmpty()) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();

         for (Meteors.Meteor var2 : LIST) {
            float var3 = Math.max(0.0F, Math.min(1.0F, var2.life / var2.maxLife));
            float var4 = Math.min(1.0F, (1.0F - var3) / 0.15F) * Math.min(1.0F, var3 / 0.35F);
            var4 = Math.max(0.0F, Math.min(1.0F, var4));
            float var5 = (float)Math.hypot((double)var2.vx, (double)var2.vy);
            if (var5 < 1.0F) {
               var5 = 1.0F;
            }

            float var6 = var2.vx / var5;
            float var7 = var2.vy / var5;

            for (int var8 = 14; var8 >= 1; var8--) {
               float var9 = (float)var8 / 14.0F;
               float var10 = var9 * (58.0F + var2.size * 8.0F);
               float var11 = var2.x - var6 * var10;
               float var12 = var2.y - var7 * var10;
               int var13 = (int)(230.0F * var4 * (1.0F - var9) * (1.0F - var9));
               if (var13 > 2) {
                  float var14 = Math.max(0.6F, var2.size * (1.0F - var9 * 0.8F));
                  Render2DUtil.drawRect(var0, var11, var12, var14, var14, withAlpha(var2.color, var13));
               }
            }

            int var16 = (int)(255.0F * var4);
            Render2DUtil.drawRect(var0, var2.x - var2.size / 2.0F, var2.y - var2.size / 2.0F, var2.size, var2.size, withAlpha(var2.color, var16));
            Render2DUtil.drawRect(
               var0, var2.x - var2.size, var2.y - var2.size, var2.size * 2.0F, var2.size * 2.0F, withAlpha(var2.color, (int)((float)var16 * 0.22F))
            );
            Render2DUtil.drawRect(
               var0,
               var2.x - var2.size * 1.8F,
               var2.y - var2.size * 1.8F,
               var2.size * 3.6F,
               var2.size * 3.6F,
               withAlpha(var2.color, (int)((float)var16 * 0.09F))
            );
         }
      }
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

   private static final class Meteor {
      float x;
      float y;
      float vx;
      float vy;
      float life;
      float maxLife;
      float size;
      int color;

      private Meteor() {
      }
   }
}
