package dev.chun.mod.modules.impl.hud;

import dev.chun.api.utils.player.EntityUtil;
import dev.chun.api.utils.render.ColorUtil;
import dev.chun.api.utils.render.TextUtil;
import dev.chun.core.impl.FontManager;
import dev.chun.mod.modules.HudElement;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class ArmorHud extends Module implements HudElement {
   public static ArmorHud INSTANCE;
   private int hudX = -1;
   private int hudY = -1;
   private HudElement.AlignMode alignMode = HudElement.AlignMode.LEFT;
   private static final int ITEM_SIZE = 16;
   private static final int ITEM_SPACING = 20;
   private static final int ARMOR_SLOTS = 4;
   public final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
   public final BooleanSetting durability = this.add(new BooleanSetting("Durability", true));
   private static final Color MIN_COLOR = new Color(196, 0, 0);
   private static final Color MAX_COLOR = new Color(0, 227, 0);

   public ArmorHud() {
      super("Armor", Module.Category.HUD);
      this.setChinese("\u88c5\u5907\u663e\u793a");
      INSTANCE = this;
   }

   private void initDefaultPosition() {
      if (mc != null && mc.getWindow() != null) {
         this.hudX = mc.getWindow().getScaledWidth() / 2 - this.getElementWidth() / 2;
         this.hudY = mc.getWindow().getScaledHeight() - 55;
         this.alignMode = HudElement.AlignMode.LEFT;
      }
   }

   @Override
   public void onRender2D(DrawContext drawContext, float tickDelta) {
      this.renderElement(drawContext, tickDelta);
   }

   @Override
   public String getElementName() {
      return this.getChinese() != null ? this.getChinese() : this.getName();
   }

   @Override
   public int getElementX() {
      if (this.hudX < 0) {
         this.initDefaultPosition();
      }

      return this.alignMode == HudElement.AlignMode.RIGHT ? this.hudX - this.getElementWidth() : this.hudX;
   }

   @Override
   public void setElementX(int x) {
      this.hudX = this.alignMode == HudElement.AlignMode.RIGHT ? x + this.getElementWidth() : x;
   }

   @Override
   public int getElementY() {
      if (this.hudY < 0) {
         this.initDefaultPosition();
      }

      return this.hudY;
   }

   @Override
   public void setElementY(int y) {
      this.hudY = y;
   }

   @Override
   public int getElementWidth() {
      return 80;
   }

   @Override
   public int getElementHeight() {
      return 16 + (this.durability.getValue() ? 6 : 0);
   }

   @Override
   public boolean isElementVisible() {
      return this.isOn();
   }

   @Override
   public HudElement.AlignMode getAlignMode() {
      return this.alignMode;
   }

   @Override
   public void setAlignMode(HudElement.AlignMode mode) {
      if (mode != this.alignMode) {
         if (mode == HudElement.AlignMode.RIGHT && this.alignMode == HudElement.AlignMode.LEFT) {
            this.hudX = this.hudX + this.getElementWidth();
         } else if (mode == HudElement.AlignMode.LEFT && this.alignMode == HudElement.AlignMode.RIGHT) {
            this.hudX = this.hudX - this.getElementWidth();
         }

         this.alignMode = mode;
      }
   }

   @Override
   public int getRightAnchorX() {
      return this.alignMode == HudElement.AlignMode.RIGHT ? this.hudX : this.hudX + this.getElementWidth();
   }

   @Override
   public void setRightAnchorX(int rightX) {
      this.hudX = this.alignMode == HudElement.AlignMode.RIGHT ? rightX : rightX - this.getElementWidth();
   }

   @Override
   public void renderElement(DrawContext context, float tickDelta) {
      if (mc.player != null) {
         int baseX = this.getElementX();
         int baseY = this.getElementY();
         int drawX = baseX;

         for (int i = 3; i >= 0; i--) {
            ItemStack stack = mc.player.getInventory().getArmorStack(i);
            if (!stack.isEmpty()) {
               context.drawItem(stack, drawX, baseY);
               context.drawItemInSlot(mc.textRenderer, stack, drawX, baseY);
               if (this.durability.getValue() && stack.isDamageable()) {
                  int damage = EntityUtil.getDamagePercent(stack);
                  Color color = ColorUtil.fadeColor(MIN_COLOR, MAX_COLOR, (double)((float)damage / 100.0F));
                  if (FontManager.isCustom()) {
                     FontManager.getSmall()
                        .drawString(
                           context.getMatrices(),
                           damage + "%",
                           (double)(drawX + 1),
                           (double)((float)baseY - FontManager.getSmall().getFontHeight() / 2.0F),
                           color.getRGB(),
                           this.shadow.getValue()
                        );
                  } else {
                     TextUtil.drawStringScale(context, damage + "%", (float)(drawX + 2), (float)baseY - 2.25F, color.getRGB(), 0.5F, this.shadow.getValue());
                  }
               }
            }

            drawX += 20;
         }
      }
   }
}
