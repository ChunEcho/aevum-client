package dev.chun.mod.modules.impl.hud;

import dev.chun.core.impl.BlurManager;
import dev.chun.mod.modules.HudElement;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.ColorSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class BackpackContents extends Module implements HudElement {
   public static BackpackContents INSTANCE;
   private int hudX = 100;
   private int hudY = 100;
   private final SliderSetting slotSize = this.add(new SliderSetting("SlotSize", 16, 12, 24));
   private final SliderSetting spacing = this.add(new SliderSetting("Spacing", 2, 0, 6));
   private final BooleanSetting showBackground = this.add(new BooleanSetting("ShowBackground", true));
   private final SliderSetting blurStrength = this.add(new SliderSetting("BlurStrength", 50, 0, 100));
   private final BooleanSetting showBorder = this.add(new BooleanSetting("ShowBorder", true));
   private final ColorSetting borderColor = this.add(new ColorSetting("BorderColor", new Color(255, 255, 255)));
   private final BooleanSetting showCount = this.add(new BooleanSetting("ShowCount", true));
   private final BooleanSetting showDurability = this.add(new BooleanSetting("ShowDurability", true));

   public BackpackContents() {
      super("BackpackContents", Module.Category.HUD);
      this.setChinese("\u80cc\u5305\u7269\u8d44\u663e\u793a");
      INSTANCE = this;
   }

   @Override
   public String getElementName() {
      return this.getChinese() != null ? this.getChinese() : this.getName();
   }

   @Override
   public int getElementX() {
      return this.hudX;
   }

   @Override
   public void setElementX(int x) {
      this.hudX = x;
   }

   @Override
   public int getElementY() {
      return this.hudY;
   }

   @Override
   public void setElementY(int y) {
      this.hudY = y;
   }

   @Override
   public int getElementWidth() {
      return 9 * this.slotSize.getValueInt() + 8 * this.spacing.getValueInt();
   }

   @Override
   public int getElementHeight() {
      return 3 * this.slotSize.getValueInt() + 2 * this.spacing.getValueInt();
   }

   @Override
   public boolean isElementVisible() {
      return this.isOn();
   }

   @Override
   public void onRender2D(DrawContext drawContext, float tickDelta) {
      this.renderElement(drawContext, tickDelta);
   }

   @Override
   public void renderElement(DrawContext drawContext, float tickDelta) {
      if (mc.player != null) {
         int ss = this.slotSize.getValueInt();
         int sp = this.spacing.getValueInt();
         int tw = this.getElementWidth();
         int th = this.getElementHeight();
         int sx = this.hudX;
         int sy = this.hudY;
         if (this.showBackground.getValue()) {
            float percentage = (float)this.blurStrength.getValueInt() / 100.0F;
            float actualRadius = percentage * 20.0F;
            if (actualRadius > 0.0F) {
               drawContext.draw();
               new BlurManager().applyBlur(actualRadius, (float)sx, (float)sy, (float)tw, (float)th);
            }

            drawContext.fill(sx, sy, sx + tw, sy + th, 1073741824);
         }

         if (this.showBorder.getValue()) {
            int c = this.borderColor.getValue().getRGB();
            drawContext.fill(sx - 1, sy - 1, sx + tw + 1, sy, c);
            drawContext.fill(sx - 1, sy + th, sx + tw + 1, sy + th + 1, c);
            drawContext.fill(sx - 1, sy, sx, sy + th, c);
            drawContext.fill(sx + tw, sy, sx + tw + 1, sy + th, c);
         }

         for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
               ItemStack stack = mc.player.getInventory().getStack(row * 9 + col + 9);
               if (!stack.isEmpty()) {
                  int x = sx + col * (ss + sp);
                  int y = sy + row * (ss + sp);
                  drawContext.drawItem(stack, x + 1, y + 1);
                  if (this.showCount.getValue() || this.showDurability.getValue()) {
                     ItemStack rs = stack.copy();
                     if (!this.showCount.getValue()) {
                        rs.setCount(1);
                     }

                     drawContext.drawItemInSlot(mc.textRenderer, rs, x + 1, y + 1);
                  }
               }
            }
         }
      }
   }
}
