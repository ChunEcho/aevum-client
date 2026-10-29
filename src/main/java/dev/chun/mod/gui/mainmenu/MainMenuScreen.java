package dev.chun.mod.gui.mainmenu;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.chun.api.utils.render.AevumFonts;
import dev.chun.api.utils.render.Meteors;
import dev.chun.api.utils.render.Render2DUtil;
import dev.chun.mod.gui.fonts.FontRenderer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MainMenuScreen extends Screen {
   private static final float BTN_W = 150.0F;
   private static final float BTN_H = 20.0F;
   private static final float BTN_GAP = 6.0F;
   private static final float BTN_RADIUS = 3.0F;
   private static final float HOVER_MAX = 1.06F;
   private static final Color BTN_NORMAL = new Color(38, 42, 52, 115);
   private static final Color BTN_HOVER = new Color(46, 74, 92, 200);
   private static final int TITLE_FROM = -722689;
   private static final int TITLE_TO = -10496769;
   private static final int SUB_COLOR = -7695446;
   private static final int BTN_TEXT = -1183238;
   private static final int SHADE_TOP = -1728053248;
   private static final int SHADE_MID = 1426063360;
   private static final int SHADE_BOT = -1442840576;
   private final List<MainMenuScreen.MenuButton> buttons = new ArrayList<>();
   private Identifier bgTexture = null;
   private float openFade = 0.0F;
   private long openTime = 0L;

   public MainMenuScreen() {
      super(Text.empty());
   }

   protected void init() {
      this.openTime = System.currentTimeMillis();
      Meteors.reset();
      this.buttons.clear();
      this.buttons.add(new MainMenuScreen.MenuButton("Singleplayer", () -> this.client.setScreen(new SelectWorldScreen(this))));
      this.buttons.add(new MainMenuScreen.MenuButton("Multiplayer", () -> this.client.setScreen(new MultiplayerScreen(this))));
      this.buttons.add(new MainMenuScreen.MenuButton("Options", () -> this.client.setScreen(new OptionsScreen(this, this.client.options))));
      this.buttons.add(new MainMenuScreen.MenuButton("Quit Game", () -> this.client.scheduleStop()));
      if (this.bgTexture == null) {
         try {
            this.bgTexture = Identifier.of("aevum", "textures/gui/bg.png");
         } catch (Throwable var2) {
            this.bgTexture = null;
         }
      }
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
   }

   public boolean shouldPause() {
      return false;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      int var5 = this.width;
      int var6 = this.height;
      if (this.openFade < 1.0F) {
         this.openFade = Math.min(1.0F, this.openFade + delta * 1.8F);
      }

      long var7 = System.currentTimeMillis();
      float var9 = (float)(var7 - this.openTime) / 1000.0F;
      float var10 = (float)Math.sin((double)(var9 * 0.07F)) * 22.0F;
      float var11 = (float)Math.cos((double)(var9 * 0.05F)) * 14.0F;
      MatrixStack var12 = context.getMatrices();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      Render2DUtil.drawRect(var12, 0.0F, 0.0F, (float)var5, (float)var6, -16381938);
      if (this.bgTexture != null) {
         float var13 = 1.12F;
         int var14 = (int)((float)var5 * var13);
         int var15 = (int)((float)var6 * var13);
         int var16 = (int)((float)(var5 - var14) / 2.0F - var10);
         int var17 = (int)((float)(var6 - var15) / 2.0F - var11);

         try {
            context.drawTexture(this.bgTexture, var16, var17, var14, var15, 0.0F, 0.0F, 1920, 1080, 1920, 1080);
         } catch (Throwable var25) {
         }
      }

      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      Meteors.update(delta, (float)var5, (float)var6);
      Meteors.draw(var12);
      Render2DUtil.drawRect(var12, 0.0F, 0.0F, (float)var5, (float)var6, 1426063360);
      Render2DUtil.verticalGradient(var12, 0.0F, 0.0F, (float)var5, (float)var6 * 0.3F, -1728053248, 0);
      Render2DUtil.verticalGradient(var12, 0.0F, (float)var6 * 0.72F, (float)var5, (float)var6, 0, -1442840576);
      float var26 = (float)var5 / 2.0F;
      float var27 = (float)var6 * 0.235F;
      boolean var28 = AevumFonts.available();
      float var29;
      if (var28) {
         FontRenderer var30 = AevumFonts.title();
         var29 = AevumFonts.width(var30, "AEVUM");
         AevumFonts.drawCenteredGradient(var30, var12, "AEVUM", var26, var27, -722689, -10496769, true);
      } else {
         var29 = (float)this.client.textRenderer.getWidth("AEVUM") * 2.6F;
         this.drawVanillaScaled(context, "AEVUM", var26, var27, 2.6F, -854273, true);
      }

      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      float var31 = 18.0F;
      float var18 = 54.0F;
      float var19 = var27 + 14.0F;
      Render2DUtil.horizontalGradient(var12, var26 - var29 / 2.0F - var31 - var18, var19, var26 - var29 / 2.0F - var31, var19 + 1.0F, 5233151, -2008032769);
      Render2DUtil.horizontalGradient(var12, var26 + var29 / 2.0F + var31, var19, var26 + var29 / 2.0F + var31 + var18, var19 + 1.0F, -2008032769, 5233151);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      if (var28) {
         AevumFonts.drawCentered(AevumFonts.body(), var12, "Eternal Client", var26, var27 + 24.0F, -7695446, false);
      } else {
         this.drawVanillaScaled(context, "ETERNAL CLIENT", var26, var27 + 24.0F, 1.0F, -7695446, false);
      }

      float var20 = 20.0F * (float)this.buttons.size() + 6.0F * (float)(this.buttons.size() - 1);
      float var21 = (float)var6 * 0.55F - var20 / 2.0F;

      for (int var22 = 0; var22 < this.buttons.size(); var22++) {
         MainMenuScreen.MenuButton var23 = this.buttons.get(var22);
         var23.x = var26 - 75.0F;
         var23.y = var21 + (float)var22 * 26.0F;
         var23.render(context, mouseX, mouseY, delta, var28);
      }

      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      String var32 = "Aevum 0.1   |   Minecraft 1.21.1";
      if (var28) {
         FontRenderer var33 = AevumFonts.small();
         float var24 = AevumFonts.width(var33, var32);
         var33.drawString(var12, var32, (double)((float)var5 - var24 - 12.0F), (double)((float)var6 - 18.0F), -1702194262, false);
      } else {
         int var34 = this.client.textRenderer.getWidth(var32);
         context.drawText(this.client.textRenderer, var32, var5 - var34 - 12, var6 - 16, -1702194262, false);
      }

      context.draw();
   }

   private void drawVanillaScaled(DrawContext var1, String var2, float var3, float var4, float var5, int var6, boolean var7) {
      MatrixStack var8 = var1.getMatrices();
      int var9 = this.client.textRenderer.getWidth(var2);
      var8.push();
      var8.scale(var5, var5, 1.0F);
      var1.drawText(this.client.textRenderer, var2, (int)((var3 - (float)var9 * var5 / 2.0F) / var5), (int)(var4 / var5), var6, var7);
      var8.pop();
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         for (MainMenuScreen.MenuButton var7 : this.buttons) {
            if (var7.isOver(mouseX, mouseY)) {
               try {
                  var7.action.run();
               } catch (Throwable var9) {
                  System.out.println("[Aevum] menu button '" + var7.text + "' failed: " + var9);
               }

               return true;
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   private static Color lerpColor(Color var0, Color var1, float var2) {
      if (var2 < 0.0F) {
         var2 = 0.0F;
      }

      if (var2 > 1.0F) {
         var2 = 1.0F;
      }

      return new Color(
         (int)((float)var0.getRed() + (float)(var1.getRed() - var0.getRed()) * var2),
         (int)((float)var0.getGreen() + (float)(var1.getGreen() - var0.getGreen()) * var2),
         (int)((float)var0.getBlue() + (float)(var1.getBlue() - var0.getBlue()) * var2),
         (int)((float)var0.getAlpha() + (float)(var1.getAlpha() - var0.getAlpha()) * var2)
      );
   }

   private final class MenuButton {
      final String text;
      final Runnable action;
      float x;
      float y;
      float hover = 0.0F;

      MenuButton(String nullx, Runnable nullxx) {
         this.text = nullx;
         this.action = nullxx;
      }

      boolean isOver(double var1, double var3) {
         return var1 >= (double)this.x && var1 <= (double)(this.x + 150.0F) && var3 >= (double)this.y && var3 <= (double)(this.y + 20.0F);
      }

      void render(DrawContext var1, int var2, int var3, float var4, boolean var5) {
         boolean var6 = this.isOver((double)var2, (double)var3);
         float var7 = var6 ? 1.0F : 0.0F;
         float var8 = Math.min(1.0F, var4 * 10.0F);
         this.hover = this.hover + (var7 - this.hover) * var8;
         if (this.hover < 0.001F) {
            this.hover = 0.0F;
         }

         if (this.hover > 0.999F) {
            this.hover = 1.0F;
         }

         float var9 = 1.0F + 0.059999943F * this.hover;
         float var10 = 150.0F * var9;
         float var11 = 20.0F * var9;
         float var12 = this.x + (150.0F - var10) / 2.0F;
         float var13 = this.y + (20.0F - var11) / 2.0F;
         MatrixStack var14 = var1.getMatrices();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         Render2DUtil.drawRoundedRect(
            var14, var12, var13, var10, var11, 3.0F, MainMenuScreen.lerpColor(MainMenuScreen.BTN_NORMAL, MainMenuScreen.BTN_HOVER, this.hover)
         );
         if (this.hover > 0.01F) {
            Render2DUtil.drawRect(var14, var12, var13 + 4.0F, 2.0F * this.hover, var11 - 8.0F, -11544065);
         }

         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         if (var5) {
            FontRenderer var15 = AevumFonts.body();
            float var16 = AevumFonts.width(var15, this.text);
            var15.drawString(var14, this.text, (double)(var12 + (var10 - var16) / 2.0F), (double)(var13 + (var11 - 11.0F) / 2.0F), -1183238, false);
         } else {
            String var17 = this.text.toUpperCase();
            int var18 = MainMenuScreen.this.client.textRenderer.getWidth(var17);
            var1.drawText(
               MainMenuScreen.this.client.textRenderer,
               var17,
               (int)(var12 + (var10 - (float)var18) / 2.0F),
               (int)(var13 + (var11 - 8.0F) / 2.0F),
               -1183238,
               false
            );
         }
      }
   }
}
