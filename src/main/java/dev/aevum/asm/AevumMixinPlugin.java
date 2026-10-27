/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  org.objectweb.asm.tree.ClassNode
 *  org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
 *  org.spongepowered.asm.mixin.extensibility.IMixinInfo
 */
package dev.aevum.asm;

import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class AevumMixinPlugin
implements IMixinConfigPlugin {
    private static final String SODIUM_PKG = "dev.aevum.asm.mixins.sodium.";
    private boolean sodiumLoaded;

    public void onLoad(String mixinPackage) {
        this.sodiumLoaded = AevumMixinPlugin.isModLoaded("sodium");
    }

    private static boolean isModLoaded(String id) {
        try {
            return FabricLoader.getInstance().isModLoaded(id);
        }
        catch (Throwable t) {
            return false;
        }
    }

    public String getRefMapperConfig() {
        return null;
    }

    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith(SODIUM_PKG)) {
            return this.sodiumLoaded;
        }
        return true;
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

