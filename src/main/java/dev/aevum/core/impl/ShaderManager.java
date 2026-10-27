/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.RenderTarget
 *  net.minecraft.PostChain
 *  net.minecraft.ResourceLocation
 *  net.minecraft.Minecraft
 *  org.jetbrains.annotations.NotNull
 *  org.ladysnake.satin.api.managed.ManagedShaderEffect
 *  org.ladysnake.satin.api.managed.ShaderEffectManager
 */
package dev.aevum.core.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.api.interfaces.IShaderEffectHook;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.mod.modules.impl.render.Ambience;
import dev.aevum.mod.modules.impl.render.ShaderModule;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.RenderTarget;
import net.minecraft.PostChain;
import net.minecraft.ResourceLocation;
import net.minecraft.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.ladysnake.satin.api.managed.ShaderEffectManager;

public class ShaderManager
implements Wrapper {
    static final Timer timer = new Timer();
    private static final List<RenderTask> tasks = new ArrayList<RenderTask>();
    public static ManagedShaderEffect DEFAULT_OUTLINE;
    public static ManagedShaderEffect PULSE_OUTLINE;
    public static ManagedShaderEffect SMOKE_OUTLINE;
    public static ManagedShaderEffect GRADIENT_OUTLINE;
    public static ManagedShaderEffect SNOW_OUTLINE;
    public static ManagedShaderEffect FLOW_OUTLINE;
    public static ManagedShaderEffect RAINBOW_OUTLINE;
    public static ManagedShaderEffect DEFAULT;
    public static ManagedShaderEffect PULSE;
    public static ManagedShaderEffect SMOKE;
    public static ManagedShaderEffect GRADIENT;
    public static ManagedShaderEffect SNOW;
    public static ManagedShaderEffect FLOW;
    public static ManagedShaderEffect RAINBOW;
    public float time = 0.0f;
    private MyFramebuffer shaderBuffer;

    public void renderShader(Runnable runnable, Shader mode) {
        tasks.add(new RenderTask(runnable, mode));
    }

    public void renderShaders() {
        tasks.forEach(t -> this.applyShader(t.task(), t.shader()));
        tasks.clear();
    }

    public void applyShader(Runnable runnable, Shader mode) {
        if (this.fullNullCheck()) {
            return;
        }
        RenderSystem.assertOnRenderThreadOrInit();
        RenderTarget MCBuffer = Minecraft.getInstance().getMainRenderTarget();
        if (this.shaderBuffer.width != MCBuffer.width || this.shaderBuffer.height != MCBuffer.height) {
            this.shaderBuffer.method_1234(MCBuffer.width, MCBuffer.height, false);
        }
        GlStateManager._glBindFramebuffer((int)36009, (int)this.shaderBuffer.frameBufferId);
        this.shaderBuffer.method_1235(true);
        runnable.run();
        this.shaderBuffer.method_1240();
        GlStateManager._glBindFramebuffer((int)36009, (int)MCBuffer.frameBufferId);
        MCBuffer.method_1235(false);
        ManagedShaderEffect shader = this.getShader(mode);
        PostChain effect = shader.getShaderEffect();
        if (effect == null) {
            return;
        }
        ((IShaderEffectHook)effect).aevumClient$addHook("bufIn", this.shaderBuffer);
        RenderTarget outBuffer = effect.method_1264("bufOut");
        if (outBuffer == null) {
            return;
        }
        this.setupShader(mode, shader);
        this.shaderBuffer.method_1230(false);
        MCBuffer.method_1235(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate((GlStateManager.class_4535)GlStateManager.class_4535.SRC_ALPHA, (GlStateManager.class_4534)GlStateManager.class_4534.ONE_MINUS_SRC_ALPHA, (GlStateManager.class_4535)GlStateManager.class_4535.ZERO, (GlStateManager.class_4534)GlStateManager.class_4534.ONE);
        RenderSystem.backupProjectionMatrix();
        outBuffer.method_22594(outBuffer.width, outBuffer.height, false);
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.disableBlend();
    }

    public void applySky(Runnable runnable) {
        PostChain effect;
        Ambience module = Ambience.INSTANCE;
        if (module == null || !module.shouldRenderSkyShader()) {
            return;
        }
        if (this.fullNullCheck()) {
            return;
        }
        RenderSystem.assertOnRenderThreadOrInit();
        if (this.shaderBuffer == null || this.shaderBuffer.frameBufferId == -1) {
            return;
        }
        Shader shaderMode = module.getSkyShader();
        if (shaderMode == null) {
            shaderMode = Shader.Flow;
        }
        RenderTarget MCBuffer = Minecraft.getInstance().getMainRenderTarget();
        int targetWidth = Math.max(MCBuffer.width, 1);
        int targetHeight = Math.max(MCBuffer.height, 1);
        if (this.shaderBuffer.width != targetWidth || this.shaderBuffer.height != targetHeight) {
            this.shaderBuffer.method_1234(targetWidth, targetHeight, false);
        }
        if (this.shaderBuffer.frameBufferId == -1) {
            return;
        }
        GlStateManager._glBindFramebuffer((int)36009, (int)this.shaderBuffer.frameBufferId);
        this.shaderBuffer.method_1236(0.0f, 0.0f, 0.0f, 1.0f);
        this.shaderBuffer.method_1235(true);
        runnable.run();
        this.shaderBuffer.method_1240();
        this.shaderBuffer.method_1236(0.0f, 0.0f, 0.0f, 0.0f);
        GlStateManager._glBindFramebuffer((int)36009, (int)MCBuffer.frameBufferId);
        MCBuffer.method_1235(false);
        ManagedShaderEffect shader = this.getShader(shaderMode);
        if (shader == null) {
            shader = DEFAULT;
        }
        if ((effect = shader.getShaderEffect()) == null) {
            return;
        }
        ((IShaderEffectHook)effect).aevumClient$addHook("bufIn", this.shaderBuffer);
        RenderTarget outBuffer = effect.method_1264("bufOut");
        if (outBuffer == null) {
            return;
        }
        this.setupSkyShader(shaderMode, shader, module);
        this.shaderBuffer.method_1230(false);
        MCBuffer.method_1235(false);
        RenderSystem.disableBlend();
        RenderSystem.backupProjectionMatrix();
        outBuffer.method_22594(outBuffer.width, outBuffer.height, false);
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.disableBlend();
    }

    private void setupSkyShader(Shader shader, ManagedShaderEffect effect, Ambience module) {
        if (module == null) {
            return;
        }
        Color color = module.skyShaderColor.getValue();
        Color background = module.skyBackgroundColor.getValue();
        this.time = (float)timer.getMs() / 5.0f * module.skySpeed.getValueFloat() * 0.004f;
        float alpha = (float)color.getAlpha() / 255.0f;
        if (shader == Shader.Cine) {
            effect.setUniformValue("mixFactor", alpha);
            effect.setUniformValue("u_Color", (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, 1.0f);
            effect.setUniformValue("u_Resolution", (float)mc.getWindow().getGuiScaledWidth(), (float)mc.getWindow().getGuiScaledHeight());
            effect.setUniformValue("u_Mouse", 0.0f, 0.0f);
            effect.setUniformValue("u_Scale", 1.0f);
            effect.setUniformValue("u_Time", this.time);
            effect.render(mc.getTimer().method_60637(true));
        } else if (shader == Shader.Aurora) {
            effect.setUniformValue("mixFactor", alpha);
            effect.setUniformValue("u_Color", (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, 1.0f);
            effect.setUniformValue("u_Resolution", (float)mc.getWindow().getGuiScaledWidth(), (float)mc.getWindow().getGuiScaledHeight());
            effect.setUniformValue("u_Mouse", 0.0f, 0.0f);
            effect.setUniformValue("u_Scale", 1.0f);
            effect.setUniformValue("u_Time", this.time);
            effect.render(mc.getTimer().method_60637(true));
        } else if (shader == Shader.Local) {
            effect.setUniformValue("mixFactor", alpha);
            effect.setUniformValue("u_Color", (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, 1.0f);
            effect.setUniformValue("u_BackgroundColor", (float)background.getRed() / 255.0f, (float)background.getGreen() / 255.0f, (float)background.getBlue() / 255.0f, (float)background.getAlpha() / 255.0f);
            effect.setUniformValue("u_Resolution", (float)mc.getWindow().getGuiScaledWidth(), (float)mc.getWindow().getGuiScaledHeight());
            effect.setUniformValue("u_Mouse", 0.0f, 0.0f);
            effect.setUniformValue("u_Scale", 1.0f);
            effect.setUniformValue("u_Time", this.time);
            effect.render(mc.getTimer().method_60637(true));
        }
    }

    public ManagedShaderEffect getShader(@NotNull Shader mode) {
        return switch (mode.ordinal()) {
            case 3 -> GRADIENT;
            case 2 -> SMOKE;
            case 1 -> PULSE;
            case 4 -> SNOW;
            case 5 -> FLOW;
            case 6 -> RAINBOW;
            default -> DEFAULT;
        };
    }

    public ManagedShaderEffect getShaderOutline(@NotNull Shader mode) {
        return switch (mode.ordinal()) {
            case 3 -> GRADIENT_OUTLINE;
            case 2 -> SMOKE_OUTLINE;
            case 4 -> SNOW_OUTLINE;
            case 5 -> FLOW_OUTLINE;
            case 6 -> RAINBOW_OUTLINE;
            case 1 -> PULSE_OUTLINE;
            default -> DEFAULT_OUTLINE;
        };
    }

    public void setupShader(Shader shader, ManagedShaderEffect effect) {
        ShaderModule module = ShaderModule.INSTANCE;
        Color color = module.fill.getValue();
        this.time = (float)timer.getMs() / 5.0f * module.speed.getValueFloat() * 0.004f;
        if (shader == Shader.Rainbow) {
            effect.setUniformValue("alpha2", (float)color.getAlpha() / 255.0f);
            effect.setUniformValue("radius", module.radius.getValueFloat());
            effect.setUniformValue("quality", module.smoothness.getValueFloat());
            effect.setUniformValue("divider", module.divider.getValueFloat());
            effect.setUniformValue("maxSample", module.maxSample.getValueFloat());
            effect.setUniformValue("resolution", (float)mc.getWindow().getGuiScaledWidth(), (float)mc.getWindow().getGuiScaledHeight());
            effect.setUniformValue("time", this.time);
            effect.render(mc.getTimer().method_60637(true));
        } else if (shader == Shader.Gradient) {
            effect.setUniformValue("alpha2", (float)color.getAlpha() / 255.0f);
            effect.setUniformValue("rgb", (float)module.smoke1.getValue().getRed() / 255.0f, (float)module.smoke1.getValue().getGreen() / 255.0f, (float)module.smoke1.getValue().getBlue() / 255.0f);
            effect.setUniformValue("rgb1", (float)module.smoke2.getValue().getRed() / 255.0f, (float)module.smoke2.getValue().getGreen() / 255.0f, (float)module.smoke2.getValue().getBlue() / 255.0f);
            effect.setUniformValue("rgb2", (float)module.smoke3.getValue().getRed() / 255.0f, (float)module.smoke3.getValue().getGreen() / 255.0f, (float)module.smoke3.getValue().getBlue() / 255.0f);
            effect.setUniformValue("rgb3", (float)module.smoke4.getValue().getRed() / 255.0f, (float)module.smoke4.getValue().getGreen() / 255.0f, (float)module.smoke4.getValue().getBlue() / 255.0f);
            effect.setUniformValue("step", module.step.getValueFloat() * 300.0f);
            effect.setUniformValue("radius", module.radius.getValueFloat());
            effect.setUniformValue("quality", module.smoothness.getValueFloat());
            effect.setUniformValue("divider", module.divider.getValueFloat());
            effect.setUniformValue("maxSample", module.maxSample.getValueFloat());
            effect.setUniformValue("resolution", (float)mc.getWindow().getGuiScaledWidth(), (float)mc.getWindow().getGuiScaledHeight());
            effect.setUniformValue("time", this.time * 300.0f);
            effect.render(mc.getTimer().method_60637(true));
        } else if (shader == Shader.Smoke) {
            effect.setUniformValue("alpha1", (float)color.getAlpha() / 255.0f);
            effect.setUniformValue("radius", module.radius.getValueFloat());
            effect.setUniformValue("quality", module.smoothness.getValueFloat());
            effect.setUniformValue("divider", module.divider.getValueFloat());
            effect.setUniformValue("maxSample", module.maxSample.getValueFloat());
            effect.setUniformValue("first", (float)module.smoke1.getValue().getRed() / 255.0f, (float)module.smoke1.getValue().getGreen() / 255.0f, (float)module.smoke1.getValue().getBlue() / 255.0f, (float)module.smoke1.getValue().getAlpha() / 255.0f);
            effect.setUniformValue("second", (float)module.smoke2.getValue().getRed() / 255.0f, (float)module.smoke2.getValue().getGreen() / 255.0f, (float)module.smoke2.getValue().getBlue() / 255.0f);
            effect.setUniformValue("third", (float)module.smoke3.getValue().getRed() / 255.0f, (float)module.smoke3.getValue().getGreen() / 255.0f, (float)module.smoke3.getValue().getBlue() / 255.0f);
            effect.setUniformValue("oct", (int)module.octaves.getValue());
            effect.setUniformValue("resolution", (float)mc.getWindow().getGuiScaledWidth(), (float)mc.getWindow().getGuiScaledHeight());
            effect.setUniformValue("time", this.time);
            effect.render(mc.getTimer().method_60637(true));
        } else if (shader == Shader.Solid) {
            effect.setUniformValue("mixFactor", (float)color.getAlpha() / 255.0f);
            effect.setUniformValue("minAlpha", module.alpha.getValueFloat() / 255.0f);
            effect.setUniformValue("radius", module.radius.getValueFloat());
            effect.setUniformValue("quality", module.smoothness.getValueFloat());
            effect.setUniformValue("divider", module.divider.getValueFloat());
            effect.setUniformValue("maxSample", module.maxSample.getValueFloat());
            effect.setUniformValue("color", (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f);
            effect.setUniformValue("resolution", (float)mc.getWindow().getGuiScaledWidth(), (float)mc.getWindow().getGuiScaledHeight());
            effect.render(mc.getTimer().method_60637(true));
        } else if (shader == Shader.Pulse) {
            effect.setUniformValue("mixFactor", (float)color.getAlpha() / 255.0f);
            effect.setUniformValue("minAlpha", module.alpha.getValueFloat() / 255.0f);
            effect.setUniformValue("radius", module.radius.getValueFloat());
            effect.setUniformValue("quality", module.smoothness.getValueFloat());
            effect.setUniformValue("divider", module.divider.getValueFloat());
            effect.setUniformValue("maxSample", module.maxSample.getValueFloat());
            effect.setUniformValue("color", (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f);
            Color color2 = module.pulse.getValue();
            effect.setUniformValue("color2", (float)color2.getRed() / 255.0f, (float)color2.getGreen() / 255.0f, (float)color2.getBlue() / 255.0f);
            effect.setUniformValue("time", this.time);
            effect.setUniformValue("size", module.pulseSpeed.getValueFloat());
            effect.setUniformValue("resolution", (float)mc.getWindow().getGuiScaledWidth(), (float)mc.getWindow().getGuiScaledHeight());
            effect.render(mc.getTimer().method_60637(true));
        } else if (shader == Shader.Snow) {
            effect.setUniformValue("color", (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
            effect.setUniformValue("radius", module.radius.getValueFloat());
            effect.setUniformValue("quality", module.smoothness.getValueFloat());
            effect.setUniformValue("divider", module.divider.getValueFloat());
            effect.setUniformValue("maxSample", module.maxSample.getValueFloat());
            effect.setUniformValue("resolution", (float)mc.getWindow().getGuiScaledWidth(), (float)mc.getWindow().getGuiScaledHeight());
            effect.setUniformValue("time", this.time);
            effect.render(mc.getTimer().method_60637(true));
        } else if (shader == Shader.Flow) {
            effect.setUniformValue("mixFactor", (float)color.getAlpha() / 255.0f);
            effect.setUniformValue("radius", module.radius.getValueFloat());
            effect.setUniformValue("quality", module.smoothness.getValueFloat());
            effect.setUniformValue("divider", module.divider.getValueFloat());
            effect.setUniformValue("maxSample", module.maxSample.getValueFloat());
            effect.setUniformValue("resolution", (float)mc.getWindow().getGuiScaledWidth(), (float)mc.getWindow().getGuiScaledHeight());
            effect.setUniformValue("time", this.time);
            effect.render(mc.getTimer().method_60637(true));
        }
    }

    public void reloadShaders() {
        DEFAULT = ShaderEffectManager.getInstance().manage(ResourceLocation.method_60654((String)"shaders/post/outline.json"));
        SMOKE = ShaderEffectManager.getInstance().manage(ResourceLocation.method_60654((String)"shaders/post/smoke.json"));
        GRADIENT = ShaderEffectManager.getInstance().manage(ResourceLocation.method_60654((String)"shaders/post/gradient.json"));
        SNOW = ShaderEffectManager.getInstance().manage(ResourceLocation.method_60654((String)"shaders/post/snow.json"));
        FLOW = ShaderEffectManager.getInstance().manage(ResourceLocation.method_60654((String)"shaders/post/flow.json"));
        RAINBOW = ShaderEffectManager.getInstance().manage(ResourceLocation.method_60654((String)"shaders/post/rainbow.json"));
        PULSE = ShaderEffectManager.getInstance().manage(ResourceLocation.method_60654((String)"shaders/post/pulse.json"));
        DEFAULT_OUTLINE = ShaderEffectManager.getInstance().manage(ResourceLocation.method_60654((String)"shaders/post/outline.json"), managedShaderEffect -> {
            PostChain effect = managedShaderEffect.getShaderEffect();
            if (effect == null) {
                return;
            }
            ((IShaderEffectHook)effect).aevumClient$addHook("bufIn", ShaderManager.mc.levelRenderer.entityTarget());
            ((IShaderEffectHook)effect).aevumClient$addHook("bufOut", ShaderManager.mc.levelRenderer.entityTarget());
        });
        PULSE_OUTLINE = ShaderEffectManager.getInstance().manage(ResourceLocation.method_60654((String)"shaders/post/pulse.json"), managedShaderEffect -> {
            PostChain effect = managedShaderEffect.getShaderEffect();
            if (effect == null) {
                return;
            }
            ((IShaderEffectHook)effect).aevumClient$addHook("bufIn", ShaderManager.mc.levelRenderer.entityTarget());
            ((IShaderEffectHook)effect).aevumClient$addHook("bufOut", ShaderManager.mc.levelRenderer.entityTarget());
        });
        SMOKE_OUTLINE = ShaderEffectManager.getInstance().manage(ResourceLocation.method_60654((String)"shaders/post/smoke.json"), managedShaderEffect -> {
            PostChain effect = managedShaderEffect.getShaderEffect();
            if (effect == null) {
                return;
            }
            ((IShaderEffectHook)effect).aevumClient$addHook("bufIn", ShaderManager.mc.levelRenderer.entityTarget());
            ((IShaderEffectHook)effect).aevumClient$addHook("bufOut", ShaderManager.mc.levelRenderer.entityTarget());
        });
        GRADIENT_OUTLINE = ShaderEffectManager.getInstance().manage(ResourceLocation.method_60654((String)"shaders/post/gradient.json"), managedShaderEffect -> {
            PostChain effect = managedShaderEffect.getShaderEffect();
            if (effect == null) {
                return;
            }
            ((IShaderEffectHook)effect).aevumClient$addHook("bufIn", ShaderManager.mc.levelRenderer.entityTarget());
            ((IShaderEffectHook)effect).aevumClient$addHook("bufOut", ShaderManager.mc.levelRenderer.entityTarget());
        });
        SNOW_OUTLINE = ShaderEffectManager.getInstance().manage(ResourceLocation.method_60654((String)"shaders/post/snow.json"), managedShaderEffect -> {
            PostChain effect = managedShaderEffect.getShaderEffect();
            if (effect == null) {
                return;
            }
            ((IShaderEffectHook)effect).aevumClient$addHook("bufIn", ShaderManager.mc.levelRenderer.entityTarget());
            ((IShaderEffectHook)effect).aevumClient$addHook("bufOut", ShaderManager.mc.levelRenderer.entityTarget());
        });
        FLOW_OUTLINE = ShaderEffectManager.getInstance().manage(ResourceLocation.method_60654((String)"shaders/post/flow.json"), managedShaderEffect -> {
            PostChain effect = managedShaderEffect.getShaderEffect();
            if (effect == null) {
                return;
            }
            ((IShaderEffectHook)effect).aevumClient$addHook("bufIn", ShaderManager.mc.levelRenderer.entityTarget());
            ((IShaderEffectHook)effect).aevumClient$addHook("bufOut", ShaderManager.mc.levelRenderer.entityTarget());
        });
        RAINBOW_OUTLINE = ShaderEffectManager.getInstance().manage(ResourceLocation.method_60654((String)"shaders/post/rainbow.json"), managedShaderEffect -> {
            PostChain effect = managedShaderEffect.getShaderEffect();
            if (effect == null) {
                return;
            }
            ((IShaderEffectHook)effect).aevumClient$addHook("bufIn", ShaderManager.mc.levelRenderer.entityTarget());
            ((IShaderEffectHook)effect).aevumClient$addHook("bufOut", ShaderManager.mc.levelRenderer.entityTarget());
        });
    }

    public boolean fullNullCheck() {
        if (GRADIENT == null || SMOKE == null || DEFAULT == null || FLOW == null || RAINBOW == null || PULSE == null || PULSE_OUTLINE == null || GRADIENT_OUTLINE == null || SMOKE_OUTLINE == null || DEFAULT_OUTLINE == null || FLOW_OUTLINE == null || RAINBOW_OUTLINE == null || this.shaderBuffer == null) {
            if (mc.getMainRenderTarget() == null) {
                return true;
            }
            this.shaderBuffer = new MyFramebuffer(ShaderManager.mc.getMainRenderTarget().width, ShaderManager.mc.getMainRenderTarget().height);
            this.reloadShaders();
            return true;
        }
        return false;
    }

    public record RenderTask(Runnable task, Shader shader) {
    }

    public static enum Shader {
        Solid,
        Pulse,
        Smoke,
        Gradient,
        Snow,
        Flow,
        Rainbow,
        Cine,
        Aurora,
        Nebula,
        Sunset,
        Vortex,
        Waves,
        Crystal,
        Local;

    }

    public static class MyFramebuffer
    extends RenderTarget {
        public MyFramebuffer(int width, int height) {
            super(false);
            RenderSystem.assertOnRenderThreadOrInit();
            this.method_1234(width, height, true);
            this.method_1236(0.0f, 0.0f, 0.0f, 0.0f);
        }
    }
}

