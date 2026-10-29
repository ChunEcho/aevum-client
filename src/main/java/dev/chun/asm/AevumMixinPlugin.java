package dev.chun.asm;

import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class AevumMixinPlugin implements IMixinConfigPlugin {
   private static final String SODIUM_PKG = "dev.chun.asm.mixins.sodium.";
   private boolean sodiumLoaded;

   public AevumMixinPlugin() {
   }

   public void onLoad(String mixinPackage) {
      this.sodiumLoaded = isModLoaded("sodium");
   }

   private static boolean isModLoaded(String id) {
      try {
         return FabricLoader.getInstance().isModLoaded(id);
      } catch (Throwable var2) {
         return false;
      }
   }

   public String getRefMapperConfig() {
      return null;
   }

   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      return mixinClassName.startsWith("dev.chun.asm.mixins.sodium.") ? this.sodiumLoaded : true;
   }

   public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
   }

   public List<String> getMixins() {
      return null;
   }

   public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }

   public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }
}
