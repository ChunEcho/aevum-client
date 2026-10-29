package dev.chun.asm.mixins;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
@Mixin(targets = {"net.minecraft.class_1076"}, remap = false)
public class MixinLanguageManager {
    public MixinLanguageManager() {
    }
    @ModifyVariable(method = {"<init>"}, at = @At(value = "HEAD"), argsOnly = true, ordinal = 0, remap = false)
    private static String aevum$forceEnglish(String var0) {
        return "en_us";
    }
}
