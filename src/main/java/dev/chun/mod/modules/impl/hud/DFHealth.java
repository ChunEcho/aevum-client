package dev.chun.mod.modules.impl.hud;

import dev.chun.Aevum;
import dev.chun.api.utils.render.Render2DUtil;
import dev.chun.core.impl.FontManager;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class DFHealth extends Module {
   public static DFHealth INSTANCE;
   public final SliderSetting yOffset = this.add(new SliderSetting("YOffset", -67.0, -300.0, 300.0, 1.0));
   public final SliderSetting maxDisplay = this.add(new SliderSetting("MaxDisplay", 3.0, 1.0, 10.0, 1.0));
   public final BooleanSetting hideVanilla = this.add(new BooleanSetting("HideVanilla", true));
   public final BooleanSetting showArmor = this.add(new BooleanSetting("Armor", true).setParent());
   public final BooleanSetting showDurability = this.add(new BooleanSetting("Durability", false, () -> this.showArmor.isOpen()));
   public final SliderSetting armorScale = this.add(new SliderSetting("ArmorScale", 0.6, 0.1, 1.0, 0.1, () -> this.showArmor.isOpen()));
   public final SliderSetting armorXOffset = this.add(new SliderSetting("ArmorX", 68.0, 0.0, 110.0, 1.0, () -> this.showArmor.isOpen()));
   public final SliderSetting armorYOffset = this.add(new SliderSetting("ArmorY", -12.0, -50.0, 50.0, 1.0, () -> this.showArmor.isOpen()));
   public final BooleanSetting showGapple = this.add(new BooleanSetting("Gapple", true));
   private static final int BASE_X = 6;
   private static final int BASE_BOTTOM_OFFSET = 42;
   private static final float AVATAR_SIZE = 25.0F;
   private static final float AVATAR_OFFSET_X = 1.0F;
   private static final float SPACING = 8.0F;
   private static final float INDEX_OFFSET_X = 1.0F;
   private static final float INDEX_OFFSET_Y = 2.0F;
   private static final float BAR_OFFSET_Y = 10.0F;
   private static final float BAR_WIDTH = 110.0F;
   private static final float BAR_HEIGHT = 3.0F;
   private static final int BAR_OUTLINE = 1;
   private static final float HP_TEXT_OFFSET_Y = -9.0F;
   private static final float NAME_TEXT_OFFSET_Y = 4.0F;
   private static final int ENTRY_HEIGHT = 31;
   private static final int ENTRY_GAP = 3;
   private static final Color HP_TEXT_COLOR = new Color(255, 255, 255, 245);
   private static final Color NAME_TEXT_COLOR = new Color(255, 255, 255, 190);
   private static final Color BAR_COLOR = new Color(255, 255, 255, 255);
   private static final Color BAR_BACKGROUND_COLOR = new Color(40, 40, 40, 136);
   private static final Color BAR_OUTLINE_COLOR = new Color(0, 0, 0, 140);
   private static final int GAPPLE_X_OFFSET = 126;
   private static final int GAPPLE_Y_OFFSET = -13;
   private static final int GAPPLE_BOX_SIZE = 30;
   private static final float GAPPLE_RADIUS = 4.0F;
   private static final float GAPPLE_ICON_SCALE = 1.7F;
   private static final Color GAPPLE_BG_COLOR = new Color(0, 0, 0, 100);
   private static final Color GAPPLE_TEXT_COLOR = new Color(255, 255, 255, 255);
   private static final ItemStack ENCHANTED_GOLDEN_APPLE = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);

   public DFHealth() {
      super("DFHealth", Module.Category.HUD);
      this.setChinese("\u4e09\u89d2\u6d32\u8840\u91cf");
      INSTANCE = this;
   }

   public static boolean shouldHideVanilla() {
      return INSTANCE != null && INSTANCE.isOn() && INSTANCE.hideVanilla.getValue();
   }

   @Override
   public void onRender2D(DrawContext drawContext, float tickDelta) {
      if (mc.player != null && mc.world != null) {
         int selfBaseY = mc.getWindow().getScaledHeight() - 42 + this.yOffset.getValueInt();
         ArrayList<AbstractClientPlayerEntity> friends = new ArrayList<>();

         for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            if (player != null && player != mc.player && player.isAlive() && Aevum.FRIEND != null && Aevum.FRIEND.isFriend(player)) {
               friends.add(player);
            }
         }

         friends.sort(Comparator.comparingDouble(p -> (double)mc.player.distanceTo(p)));
         int displayCount = Math.min(this.maxDisplay.getValueInt(), friends.size());

         for (int i = 0; i < displayCount; i++) {
            AbstractClientPlayerEntity friend = friends.get(i);
            int entryY = selfBaseY - (displayCount - i) * 34;
            this.renderEntry(drawContext, friend, entryY, i + 2, false);
         }

         this.renderEntry(drawContext, mc.player, selfBaseY, 1, true);
      }
   }

   private void renderEntry(DrawContext drawContext, AbstractClientPlayerEntity player, int baseY, int indexNumber, boolean isSelf) {
      MatrixStack matrices = drawContext.getMatrices();
      int avatarX = 7;
      int rectX = (int)((float)avatarX + 25.0F + 8.0F);
      int rectY = (int)((float)baseY + 10.0F);
      this.drawPlayerHead(drawContext, player, avatarX, baseY, 25.0F);
      this.drawScaledText(drawContext, String.valueOf(indexNumber), (float)avatarX + 1.0F, (float)baseY + 2.0F, Color.WHITE, 1.0F, true);
      this.drawOutline(matrices, (float)(rectX - 1), (float)(rectY - 1), 112.0F, 5.0F, 1, BAR_OUTLINE_COLOR);
      Render2DUtil.drawRect(matrices, (float)rectX, (float)rectY, 110.0F, 3.0F, BAR_BACKGROUND_COLOR);
      float realHealth = player.getHealth() + player.getAbsorptionAmount();
      float ratio = Math.max(0.0F, Math.min(realHealth / 20.0F, 1.0F));
      if (ratio > 0.0F) {
         Render2DUtil.drawRect(matrices, (float)rectX, (float)rectY, (float)((int)(110.0F * ratio)), 3.0F, BAR_COLOR);
      }

      this.drawScaledText(drawContext, this.format(realHealth) + "/20", (float)rectX + 1.0F, (float)rectY + -9.0F, HP_TEXT_COLOR, 1.0F, true);
      this.drawScaledText(drawContext, player.getName().getString(), (float)rectX, (float)rectY + 3.0F + 4.0F, NAME_TEXT_COLOR, 1.0F, true);
      if (this.showArmor.getValue()) {
         this.renderArmorAndDurability(drawContext, player, rectX + this.armorXOffset.getValueInt(), rectY + this.armorYOffset.getValueInt());
      }

      if (isSelf && this.showGapple.getValue()) {
         this.renderGapple(drawContext, rectX + 126, rectY + -13);
      }
   }

   private void renderGapple(DrawContext drawContext, int x, int y) {
      MatrixStack matrices = drawContext.getMatrices();
      Render2DUtil.drawRoundedRect(matrices, (float)x, (float)y, 30.0F, 30.0F, 4.0F, GAPPLE_BG_COLOR);
      int gappleCount = this.countEnchantedGoldenApples();
      float scaledIconSize = 27.2F;
      float iconX = (float)x + (30.0F - scaledIconSize) / 2.0F;
      float iconY = (float)y + (30.0F - scaledIconSize) / 2.0F;
      matrices.push();
      matrices.translate(iconX, iconY, 0.0F);
      matrices.scale(1.7F, 1.7F, 1.0F);
      drawContext.drawItem(ENCHANTED_GOLDEN_APPLE, 0, 0);
      matrices.pop();
      if (gappleCount > 0) {
         String countText = String.valueOf(gappleCount);
         int textWidth = mc.textRenderer.getWidth(countText);
         float textX = (float)x + (float)(30 - textWidth) / 2.0F;
         float textY = (float)(y - 10);
         this.drawScaledText(drawContext, countText, textX, textY, GAPPLE_TEXT_COLOR, 1.0F, true);
      }
   }

   private int countEnchantedGoldenApples() {
      if (mc.player == null) {
         return 0;
      } else {
         int count = 0;

         for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
            ItemStack stack = (ItemStack)mc.player.getInventory().main.get(i);
            if (stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
               count += stack.getCount();
            }
         }

         ItemStack offhand = mc.player.getOffHandStack();
         if (offhand.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
            count += offhand.getCount();
         }

         return count;
      }
   }

   private void renderArmorAndDurability(DrawContext drawContext, AbstractClientPlayerEntity player, int x, int y) {
      MatrixStack matrices = drawContext.getMatrices();
      float scale = this.armorScale.getValueFloat();
      matrices.push();
      matrices.translate((float)x, (float)y, 0.0F);
      matrices.scale(scale, scale, 1.0F);
      int xOffset = 0;

      for (int i = 3; i >= 0; i--) {
         ItemStack armorStack = player.getInventory().getArmorStack(i);
         if (!armorStack.isEmpty()) {
            if (this.showDurability.getValue() && armorStack.isDamageable()) {
               int maxDamage = armorStack.getMaxDamage();
               int currentDamage = armorStack.getDamage();
               int durabilityPercent = (int)((float)(maxDamage - currentDamage) / (float)maxDamage * 100.0F);
               Color durColor = this.getDurabilityColor(durabilityPercent);
               String durText = durabilityPercent + "%";
               matrices.push();
               float textPaddingX = 8.0F - (float)mc.textRenderer.getWidth(durText) / 2.0F;
               matrices.translate((float)xOffset + textPaddingX, -9.5F, 0.0F);
               drawContext.drawText(mc.textRenderer, durText, 0, 0, durColor.getRGB(), true);
               matrices.pop();
            }

            drawContext.drawItem(armorStack, xOffset, 0);
            xOffset += 18;
         }
      }

      matrices.pop();
   }

   private Color getDurabilityColor(int percent) {
      if (percent > 75) {
         return Color.GREEN;
      } else if (percent > 45) {
         return Color.YELLOW;
      } else {
         return percent > 20 ? Color.ORANGE : Color.RED;
      }
   }

   private String format(float value) {
      return value == (float)((int)value) ? String.valueOf((int)value) : String.format("%.1f", value);
   }

   private void drawOutline(MatrixStack matrices, float x, float y, float width, float height, int thickness, Color color) {
      Render2DUtil.drawRect(matrices, x, y, width, (float)thickness, color);
      Render2DUtil.drawRect(matrices, x, y + height - (float)thickness, width, (float)thickness, color);
      Render2DUtil.drawRect(matrices, x, y, (float)thickness, height, color);
      Render2DUtil.drawRect(matrices, x + width - (float)thickness, y, (float)thickness, height, color);
   }

   private void drawScaledText(DrawContext drawContext, String text, float x, float y, Color color, float scale, boolean shadow) {
      MatrixStack matrices = drawContext.getMatrices();
      matrices.push();
      matrices.translate(x, y, 0.0F);
      matrices.scale(scale, scale, 1.0F);
      if (FontManager.isCustom()) {
         FontManager.get()
            .drawString(
               matrices,
               text,
               0.0F,
               0.0F,
               (float)color.getRed() / 255.0F,
               (float)color.getGreen() / 255.0F,
               (float)color.getBlue() / 255.0F,
               (float)color.getAlpha() / 255.0F,
               shadow
            );
      } else {
         drawContext.drawText(mc.textRenderer, text, 0, 0, color.getRGB(), shadow);
      }

      matrices.pop();
   }

   private void drawPlayerHead(DrawContext drawContext, AbstractClientPlayerEntity player, int x, int y, float size) {
      Identifier skin = player.getSkinTextures().texture();
      MatrixStack matrices = drawContext.getMatrices();
      float scale = size / 8.0F;
      matrices.push();
      matrices.translate((float)x, (float)y, 0.0F);
      matrices.scale(scale, scale, 1.0F);
      drawContext.drawTexture(skin, 0, 0, 8.0F, 8.0F, 8, 8, 64, 64);
      drawContext.drawTexture(skin, 0, 0, 40.0F, 8.0F, 8, 8, 64, 64);
      matrices.pop();
   }
}
