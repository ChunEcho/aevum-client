package dev.chun.mod.modules.impl.render;

import dev.chun.api.utils.render.TextUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class LowHealthWarning extends Module {
   public static LowHealthWarning INSTANCE;
   private final SliderSetting healthThreshold = this.add(new SliderSetting("HealthThreshold", 6.0, 0.0, 36.0, 0.5));
   private final SliderSetting textScale = this.add(new SliderSetting("TextScale", 1.5, 0.5, 3.0, 0.1));
   private final SliderSetting posX = this.add(new SliderSetting("PosX", 0.0, -1000.0, 1000.0, 1.0));
   private final SliderSetting posY = this.add(new SliderSetting("PosY", 0.0, -1000.0, 1000.0, 1.0));
   private static final int WARNING_COLOR = Color.RED.getRGB();

   public LowHealthWarning() {
      super("LowHealthWarning", Module.Category.Render);
      this.setChinese("\u4f4e\u8840\u91cf\u9884\u8b66");
      INSTANCE = this;
   }

   @Override
   public void onRender2D(DrawContext DrawContext, float n) {
      if (mc.player != null) {
         if (!(mc.player.getHealth() + mc.player.getAbsorptionAmount() > (float)this.healthThreshold.getValue())) {
            float n2 = (float)mc.getWindow().getScaledWidth();
            float n3 = (float)mc.getWindow().getScaledHeight();
            float valueFloat = this.textScale.getValueFloat();
            float n4 = n2 / 2.0F / valueFloat - TextUtil.getWidth("Your health is too low!") / 2.0F + (float)this.posX.getValue() / valueFloat;
            float n5 = n3 / 2.0F / valueFloat + (float)this.posY.getValue() / valueFloat;
            MatrixStack getMatrices = DrawContext.getMatrices();
            getMatrices.push();
            getMatrices.scale(valueFloat, valueFloat, 1.0F);
            TextUtil.drawString(DrawContext, "Your health is too low!", (double)n4, (double)n5, WARNING_COLOR, false, true);
            getMatrices.pop();
         }
      }
   }
}
