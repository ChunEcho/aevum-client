package dev.chun.api.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public final class AevumBackground {
   private static final int BG_FLAT = -16381938;
   private static final int SHADE_MID = 1996488704;
   private static final int SHADE_TOP = -1728053248;
   private static final int SHADE_BOT = -1442840576;
   private static final long T0 = System.nanoTime();
   private static Identifier bgTex = null;
   private static boolean texTried = false;

   private AevumBackground() {
   }

   private static Identifier tex() {
      if (!texTried) {
         texTried = true;

         try {
            bgTex = Identifier.of("aevum", "textures/gui/bg.png");
         } catch (Throwable var1) {
            bgTex = null;
         }
      }

      return bgTex;
   }

   public static void renderBackground(DrawContext var0, int var1, int var2, float var3) {
      try {
         MinecraftClient var4 = MinecraftClient.getInstance();
         boolean var5 = var4 == null || var4.world == null;
         render(var0, var1, var2, var3, var5);
      } catch (Throwable var7) {
         try {
            render(var0, var1, var2, var3, true);
         } catch (Throwable var6) {
         }
      }
   }

   public static void render(DrawContext var0, int var1, int var2, float var3, boolean var4) {
      MatrixStack var5 = var0.getMatrices();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      float var6 = (float)(System.nanoTime() - T0) / 1.0E9F;
      float var7 = (float)Math.sin((double)(var6 * 0.07F)) * 22.0F;
      float var8 = (float)Math.cos((double)(var6 * 0.05F)) * 14.0F;
      Render2DUtil.drawRect(var5, 0.0F, 0.0F, (float)var1, (float)var2, var4 ? -16381938 : 1711671310);
      Identifier var9 = tex();
      if (var9 != null) {
         float var10 = 1.12F;
         int var11 = (int)((float)var1 * var10);
         int var12 = (int)((float)var2 * var10);
         int var13 = (int)((float)(var1 - var11) / 2.0F - var7);
         int var14 = (int)((float)(var2 - var12) / 2.0F - var8);

         try {
            var0.drawTexture(var9, var13, var14, var11, var12, 0.0F, 0.0F, 1920, 1080, 1920, 1080);
         } catch (Throwable var16) {
         }
      }

      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      Meteors.update(var3, (float)var1, (float)var2);
      Meteors.draw(var5);
      if (var4) {
         Render2DUtil.drawRect(var5, 0.0F, 0.0F, (float)var1, (float)var2, 1996488704);
         Render2DUtil.verticalGradient(var5, 0.0F, 0.0F, (float)var1, (float)var2 * 0.28F, -1728053248, 0);
         Render2DUtil.verticalGradient(var5, 0.0F, (float)var2 * 0.72F, (float)var1, (float)var2, 0, -1442840576);
      } else {
         Render2DUtil.drawRect(var5, 0.0F, 0.0F, (float)var1, (float)var2, 1426063360);
      }
   }
}
