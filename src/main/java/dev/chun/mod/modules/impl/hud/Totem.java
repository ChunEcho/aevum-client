package dev.chun.mod.modules.impl.hud;

import dev.chun.Aevum;
import dev.chun.api.utils.player.InventoryUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;

public class Totem extends Module {
   public static Totem INSTANCE;
   public final BooleanSetting lockOnHotbar = this.add(new BooleanSetting("LockOnHotbar", true));
   public final SliderSetting posX = this.add(new SliderSetting("PosX", 0.0, 0.0, 1920.0, 1.0));
   public final SliderSetting posY = this.add(new SliderSetting("PosY", 150.0, 0.0, 1080.0, 1.0));
   private final int width = 16;
   private final int height = 16;

   public Totem() {
      super("Totem", Module.Category.HUD);
      this.setChinese("\u56fe\u817e");
      INSTANCE = this;
      this.posX.setValue((double)Aevum.CONFIG.getInt("totem_x", 0));
      this.posY.setValue((double)Aevum.CONFIG.getInt("totem_y", 150));
      this.posX.injectTask(() -> Aevum.CONFIG.set("totem_x", (int)this.posX.getValue()));
      this.posY.injectTask(() -> Aevum.CONFIG.set("totem_y", (int)this.posY.getValue()));
   }

   @Override
   public void onRender2D(DrawContext drawContext, float tickDelta) {
      if (mc.player != null) {
         MatrixStack matrixStack = drawContext.getMatrices();
         int renderX;
         int renderY;
         if (this.lockOnHotbar.getValue()) {
            renderX = mc.getWindow().getWidth() / 4 - 8;
            renderY = mc.getWindow().getScaledHeight() - 16;
            renderY = mc.player.isCreative() ? renderY - 28 : renderY - 38;
         } else {
            renderX = (int)this.posX.getValue();
            renderY = (int)this.posY.getValue();
         }

         matrixStack.push();
         int count = InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING);
         drawContext.drawItem(Items.TOTEM_OF_UNDYING.getDefaultStack(), renderX + 16 - 16, renderY + 16 - 16);
         String countText = String.valueOf(count);
         int textX = renderX + 16 - 16 + 16 - mc.textRenderer.getWidth(countText) + 2;
         int textY = renderY + 16 - 16 + 16 - 9 + 2 + 2;
         drawContext.drawText(mc.textRenderer, countText, textX, textY, new Color(255, 255, 255, 255).getRGB(), true);
         matrixStack.pop();
      }
   }
}
