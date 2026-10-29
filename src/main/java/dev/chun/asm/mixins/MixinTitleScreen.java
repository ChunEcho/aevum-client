package dev.chun.asm.mixins;

import dev.chun.mod.gui.mainmenu.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   targets = {"net.minecraft.class_442"},
   remap = false
)
public class MixinTitleScreen extends Screen {
   protected MixinTitleScreen(Text var1) {
      super(var1);
   }

   @Inject(
      method = {"method_25426"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private void aevum$replaceMainMenu(CallbackInfo var1) {
      try {
         if (this.client == null) {
            return;
         }

         this.client.setScreen(new MainMenuScreen());
         var1.cancel();
      } catch (Throwable var3) {
      }
   }
}
