package dev.chun.mod.modules.impl.hud;

import dev.chun.api.utils.render.Render2DUtil;
import dev.chun.core.impl.FontManager;
import dev.chun.mod.gui.fonts.FontRenderer;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class DFWeapon extends Module {
   public static DFWeapon INSTANCE;
   public final SliderSetting xOffset = this.add(new SliderSetting("XOffset", -16.0, -400.0, 400.0, 1.0));
   public final SliderSetting yOffset = this.add(new SliderSetting("YOffset", -52.0, -400.0, 400.0, 1.0));
   public final SliderSetting iconX = this.add(new SliderSetting("IconX", 46.0, 0.0, 120.0, 1.0));
   public final SliderSetting iconY = this.add(new SliderSetting("IconY", 17.0, 0.0, 80.0, 1.0));
   public final SliderSetting leftCountY = this.add(new SliderSetting("LeftCountY", 0.0, -20.0, 20.0, 1.0));
   public final SliderSetting rightCountY = this.add(new SliderSetting("RightCountY", 0.0, -20.0, 20.0, 1.0));
   private static final float WIDTH = 118.0F;
   private static final float HEIGHT = 34.0F;
   private static final float RADIUS = 2.5F;
   private static final Color BG_COLOR = new Color(1593835520, true);
   private static final String DISPLAY_NAME = "EndCrystal";
   private static final Color NAME_COLOR = new Color(-1426063361, true);
   private static final Color HOTBAR_COLOR = new Color(-1, true);
   private static final Color OTHER_COLOR = new Color(-1090519041, true);
   private static final boolean TEXT_SHADOW = true;
   private static final float NAME_X = 3.0F;
   private static final float NAME_BOTTOM_MARGIN = 3.0F;
   private static final float ICON_SCALE = 1.75F;
   private static final float COUNT_RIGHT_MARGIN = 6.0F;
   private static final float COUNT_BOTTOM_TO_BOX_TOP = 2.0F;
   private static final float NAME_FONT_SIZE = 8.0F;
   private static final float BIG_COUNT_FONT_SIZE = 18.0F;
   private static final float SMALL_COUNT_FONT_SIZE = 9.0F;
   private static final float COUNT_GAP = 1.0F;
   private static final ItemStack END_CRYSTAL_STACK = new ItemStack(Items.END_CRYSTAL);

   public DFWeapon() {
      super("DFWeapon", Module.Category.HUD);
      this.setChinese("\u4e09\u89d2\u6d32\u6b66\u5668");
      INSTANCE = this;
   }

   @Override
   public void onRender2D(DrawContext drawContext, float tickDelta) {
      if (mc.player != null && mc.world != null) {
         MatrixStack matrices = drawContext.getMatrices();
         float screenWidth = (float)mc.getWindow().getScaledWidth();
         float screenHeight = (float)mc.getWindow().getScaledHeight();
         float x = screenWidth - 118.0F + this.xOffset.getValueFloat();
         float y = screenHeight - 34.0F + this.yOffset.getValueFloat();
         Render2DUtil.drawRoundedRect(matrices, x, y, 118.0F, 34.0F, 2.5F, BG_COLOR);
         int hotbarCrystalCount = this.countHotbarCrystals();
         int totalCrystalCount = this.countTotalCrystals();
         this.renderCrystalIcon(drawContext, x, y);
         this.renderName(drawContext, x, y);
         this.renderCount(drawContext, x, y, hotbarCrystalCount, totalCrystalCount);
      }
   }

   private void renderCrystalIcon(DrawContext drawContext, float x, float y) {
      MatrixStack matrices = drawContext.getMatrices();
      float scaledSize = 28.0F;
      float drawX = x + this.iconX.getValueFloat() - scaledSize / 2.0F;
      float drawY = y + this.iconY.getValueFloat() - scaledSize / 2.0F;
      matrices.push();
      matrices.translate(drawX, drawY, 0.0F);
      matrices.scale(1.75F, 1.75F, 1.0F);
      drawContext.drawItem(END_CRYSTAL_STACK, 0, 0);
      matrices.pop();
   }

   private void renderName(DrawContext drawContext, float x, float y) {
      FontRenderer font = FontManager.get(8.0F);
      float textX = x + 3.0F;
      float textY = y + 34.0F - font.getFontHeight("EndCrystal") - 3.0F;
      this.drawFontText(drawContext, font, "EndCrystal", textX, textY, NAME_COLOR, true);
   }

   private void renderCount(DrawContext drawContext, float x, float y, int hotbarCount, int totalCount) {
      FontRenderer bigFont = FontManager.get(18.0F);
      FontRenderer smallFont = FontManager.get(9.0F);
      String bigText = String.valueOf(hotbarCount);
      String smallText = "/" + totalCount;
      float bigWidth = bigFont.getWidth(bigText);
      float smallWidth = smallFont.getWidth(smallText);
      float bigHeight = bigFont.getFontHeight(bigText);
      float smallHeight = smallFont.getFontHeight(smallText);
      float totalWidth = bigWidth + 1.0F + smallWidth;
      float baselineBottom = y - 2.0F;
      float startX = x + 118.0F - 6.0F - totalWidth;
      float bigY = baselineBottom - bigHeight + this.leftCountY.getValueFloat();
      float smallY = baselineBottom - smallHeight + this.rightCountY.getValueFloat();
      this.drawFontText(drawContext, bigFont, bigText, startX, bigY, HOTBAR_COLOR, true);
      this.drawFontText(drawContext, smallFont, smallText, startX + bigWidth + 1.0F, smallY, OTHER_COLOR, true);
   }

   private void drawFontText(DrawContext drawContext, FontRenderer font, String text, float x, float y, Color color, boolean shadow) {
      MatrixStack matrices = drawContext.getMatrices();
      font.drawString(
         matrices,
         text,
         x,
         y,
         (float)color.getRed() / 255.0F,
         (float)color.getGreen() / 255.0F,
         (float)color.getBlue() / 255.0F,
         (float)color.getAlpha() / 255.0F,
         shadow
      );
   }

   private int countHotbarCrystals() {
      if (mc.player == null) {
         return 0;
      } else {
         int count = 0;

         for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.END_CRYSTAL) {
               count += stack.getCount();
            }
         }

         return count;
      }
   }

   private int countTotalCrystals() {
      if (mc.player == null) {
         return 0;
      } else {
         int count = 0;

         for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
            ItemStack stack = (ItemStack)mc.player.getInventory().main.get(i);
            if (stack.getItem() == Items.END_CRYSTAL) {
               count += stack.getCount();
            }
         }

         ItemStack offhand = mc.player.getOffHandStack();
         if (offhand.getItem() == Items.END_CRYSTAL) {
            count += offhand.getCount();
         }

         return count;
      }
   }
}
