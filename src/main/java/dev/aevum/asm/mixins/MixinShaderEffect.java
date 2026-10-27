/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.RenderTarget
 *  net.minecraft.PostChain
 *  net.minecraft.PostPass
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.api.interfaces.IShaderEffectHook;
import dev.aevum.asm.accessors.IPostProcessShader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.RenderTarget;
import net.minecraft.PostChain;
import net.minecraft.PostPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={PostChain.class})
public class MixinShaderEffect
implements IShaderEffectHook {
    @Unique
    private final List<String> fakedBufferNames = new ArrayList<String>();
    @Shadow
    @Final
    private Map<String, RenderTarget> customRenderTargets;
    @Shadow
    @Final
    private List<PostPass> passes;

    @Override
    public void aevumClient$addHook(String name, RenderTarget buffer) {
        RenderTarget previousFramebuffer = this.customRenderTargets.get(name);
        if (previousFramebuffer == buffer) {
            return;
        }
        if (previousFramebuffer != null) {
            for (PostPass pass : this.passes) {
                if (pass.inTarget == previousFramebuffer) {
                    ((IPostProcessShader)pass).setInput(buffer);
                }
                if (pass.outTarget != previousFramebuffer) continue;
                ((IPostProcessShader)pass).setOutput(buffer);
            }
            this.customRenderTargets.remove(name);
            this.fakedBufferNames.remove(name);
        }
        this.customRenderTargets.put(name, buffer);
        this.fakedBufferNames.add(name);
    }

    @Inject(method={"close"}, at={@At(value="HEAD")})
    void deleteFakeBuffersHook(CallbackInfo ci) {
        for (String fakedBufferName : this.fakedBufferNames) {
            this.customRenderTargets.remove(fakedBufferName);
        }
    }
}

