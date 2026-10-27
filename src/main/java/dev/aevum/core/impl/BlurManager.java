/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ResourceLocation
 *  org.ladysnake.satin.api.managed.ManagedShaderEffect
 *  org.ladysnake.satin.api.managed.ShaderEffectManager
 */
package dev.aevum.core.impl;

import dev.aevum.api.utils.Wrapper;
import net.minecraft.ResourceLocation;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.ladysnake.satin.api.managed.ShaderEffectManager;

public class BlurManager
implements Wrapper {
    public static final ManagedShaderEffect BLUR = ShaderEffectManager.getInstance().manage(ResourceLocation.method_60654((String)"shaders/post/blurarea.json"));

    public void applyBlur(float radius, float startX, float startY, float width, float height) {
        this.applyBlur(radius, startX, startY, width, height, 0.0f);
    }

    public void applyBlur(float radius, float startX, float startY, float width, float height, float blurType) {
        float factor = (float)mc.getWindow().getGuiScale() / 2.0f;
        BLUR.setUniformValue("Radius", radius);
        BLUR.setUniformValue("BlurType", blurType);
        BLUR.setUniformValue("BlurXY", startX * factor, (float)mc.getWindow().getScreenHeight() / 2.0f - (startY + height) * factor);
        BLUR.setUniformValue("BlurCoord", width * factor, height * factor);
        BLUR.render(mc.getTimer().method_60637(true));
    }
}

