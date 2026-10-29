package dev.chun.mod.modules.impl.render;

import dev.chun.api.utils.render.TextUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class LowArmorWarning extends Module {
   public static LowArmorWarning INSTANCE;
   private final SliderSetting durabilityThreshold = this.add(new SliderSetting("DurabilityThreshold", 20.0, 0.0, 500.0, 1.0));
   private final SliderSetting textScale = this.add(new SliderSetting("TextScale", 1.5, 0.5, 3.0, 0.1));
   private final SliderSetting posX = this.add(new SliderSetting("PosX", 0.0, -1000.0, 1000.0, 1.0));
   private final SliderSetting posY = this.add(new SliderSetting("PosY", 0.0, -1000.0, 1000.0, 1.0));
   private static final int WARNING_COLOR = Color.RED.getRGB();

   public LowArmorWarning() {
      super("LowArmorWarning", Module.Category.Render);
      this.setChinese("\u4f4e\u62a4\u7532\u9884\u8b66");
      INSTANCE = this;
   }

   @Override
   public void onRender2D(DrawContext DrawContext, float n) {
      if (mc.player != null) {
         int n2 = Integer.MAX_VALUE;

         for (int i = 0; i < 4; i++) {
            ItemStack getArmorStack = mc.player.getInventory().getArmorStack(i);
            if (getArmorStack != null && !getArmorStack.isEmpty()) {
               int n3 = getArmorStack.getMaxDamage() - getArmorStack.getDamage();
               if (n3 < n2) {
                  n2 = n3;
               }
            }
         }

         if (n2 != Integer.MAX_VALUE) {
            if (n2 <= (int)this.durabilityThreshold.getValue()) {
               float n4 = (float)mc.getWindow().getScaledWidth();
               float n5 = (float)mc.getWindow().getScaledHeight();
               float valueFloat = this.textScale.getValueFloat();
               float n6 = n4 / 2.0F / valueFloat - TextUtil.getWidth("Your armor durability is too low!") / 2.0F + (float)this.posX.getValue() / valueFloat;
               float n7 = n5 / 2.0F / valueFloat + (float)this.posY.getValue() / valueFloat;
               MatrixStack scrollInHotbar = DrawContext.getMatrices();
               scrollInHotbar.push();
               scrollInHotbar.scale(valueFloat, valueFloat, 1.0F);
               TextUtil.drawString(DrawContext, "Your armor durability is too low!", (double)n6, (double)n7, WARNING_COLOR, false, true);
               scrollInHotbar.pop();
            }
         }
      }
   }
}
