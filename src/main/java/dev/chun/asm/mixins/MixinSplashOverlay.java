package dev.chun.asm.mixins;

import dev.chun.api.utils.render.SplashRenderer;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   targets = {"net.minecraft.class_425"},
   remap = false
)
public class MixinSplashOverlay {
   @Shadow
   private ResourceReload field_17767;
   @Shadow
   private long field_17771;
   @Shadow
   private Consumer<Optional<Throwable>> field_18218;
   @Shadow
   private boolean field_18219;
   @Shadow
   private MinecraftClient field_18217;
   private static final float REMOVE_AFTER = 0.6F;

   public MixinSplashOverlay() {
   }

   @Inject(
      method = {"method_25394"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private void aevum$renderSplash(DrawContext var1, int var2, int var3, float var4, CallbackInfo var5) {
      try {
         this.aevum$tickState(var1);
         float var6 = this.field_17767 != null ? this.field_17767.getProgress() : 1.0F;
         SplashRenderer.render(var1, var6, var4);
      } catch (Throwable var7) {
         return;
      }

      var5.cancel();
   }

   private void aevum$tickState(DrawContext var1) {
      long var2 = Util.getMeasuringTimeMs();
      float var4 = this.field_17771 > -1L ? (float)(var2 - this.field_17771) / 1000.0F : -1.0F;
      if (this.field_17771 == -1L && this.field_17767 != null && this.field_17767.isComplete()) {
         try {
            this.field_17767.throwException();
            this.field_18218.accept(Optional.empty());
         } catch (Throwable var6) {
            this.field_18218.accept(Optional.of(var6));
         }

         this.field_17771 = var2;
         if (this.field_18217.currentScreen != null) {
            this.field_18217.currentScreen.init(this.field_18217, var1.getScaledWindowWidth(), var1.getScaledWindowHeight());
         }
      }

      if (var4 >= 0.6F) {
         this.field_18217.setOverlay(null);
      }
   }
}
