package dev.chun.mod.modules.impl.render;

import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PlayerModel extends Module {
   public static PlayerModel INSTANCE;
   public final SliderSetting x = this.add(new SliderSetting("X", 80.0, 0.0, 1920.0, 1.0));
   public final SliderSetting y = this.add(new SliderSetting("Y", 140.0, 0.0, 1080.0, 1.0));
   public final SliderSetting size = this.add(new SliderSetting("Size", 30.0, 10.0, 120.0, 1.0));
   public final BooleanSetting followMouse = this.add(new BooleanSetting("FollowMouse", false));
   public final BooleanSetting mirror = this.add(new BooleanSetting("Mirror", false));

   public PlayerModel() {
      super("PlayerModel", Module.Category.Client);
      this.setChinese("\u73a9\u5bb6\u6a21\u578b");
      INSTANCE = this;
   }

   @Override
   public void onRender2D(DrawContext DrawContext, float n) {
      if (!nullCheck()) {
         float n2 = (float)this.size.getValue();
         float n3 = (float)this.x.getValue() + n2;
         float n4 = (float)this.y.getValue() + n2 * 2.0F;
         float n5 = this.followMouse.getValue() ? (float)((double)n3 - mc.mouse.getX()) : 0.0F;
         float n6 = this.followMouse.getValue() ? (float)((double)n4 - mc.mouse.getY()) : 0.0F;
         if (this.mirror.getValue()) {
            n5 = -n5;
         }

         InventoryScreen.drawEntity(
            DrawContext,
            n3,
            n4,
            n2,
            new Vector3f(0.0F, mc.player.getHeight() / 2.0F, 0.0F),
            new Quaternionf().rotateZ((float) Math.PI),
            new Quaternionf().rotateZ((float) Math.PI).rotateX(n6 * 0.01F).rotateY(n5 * 0.01F),
            mc.player
         );
      }
   }
}
