/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.MobEffectInstance
 *  net.minecraft.MobEffects
 *  net.minecraft.Entity
 *  net.minecraft.Player
 *  net.minecraft.BufferUploader
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tesselator
 *  net.minecraft.DefaultVertexFormat
 *  net.minecraft.VertexFormat$Mode
 *  net.minecraft.ResourceLocation
 *  net.minecraft.GuiGraphics
 *  net.minecraft.Mth
 *  net.minecraft.GameRenderer
 *  net.minecraft.MeshData
 *  org.joml.Matrix4f
 */
package dev.aevum.mod.modules.impl.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.api.utils.skija.SkijaGlowRenderer;
import dev.aevum.mod.modules.HudElement;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.MobEffectInstance;
import net.minecraft.MobEffects;
import net.minecraft.Entity;
import net.minecraft.Player;
import net.minecraft.BufferUploader;
import net.minecraft.BufferBuilder;
import net.minecraft.Tesselator;
import net.minecraft.DefaultVertexFormat;
import net.minecraft.VertexFormat;
import net.minecraft.ResourceLocation;
import net.minecraft.GuiGraphics;
import net.minecraft.Mth;
import net.minecraft.GameRenderer;
import net.minecraft.MeshData;
import org.joml.Matrix4f;

public class CircularStatusHud
extends Module
implements HudElement {
    public static CircularStatusHud INSTANCE;
    private float currentFade = 1.0f;
    private long lastRenderTime = 0L;
    public final BooleanSetting hideVanillaCrosshair = this.add(new BooleanSetting("HideVanillaCrosshair", true));
    private final EnumSetting<CrosshairMode> crosshairMode = this.add(new EnumSetting<CrosshairMode>("CrosshairMode", CrosshairMode.SolidCircle, () -> this.hideVanillaCrosshair.getValue()));
    private final SliderSetting crosshairSize = this.add(new SliderSetting("CrosshairSize", 4.0, 1.0, 20.0, 0.5, () -> this.hideVanillaCrosshair.getValue() && this.crosshairMode.getValue() != CrosshairMode.None));
    private final ColorSetting crosshairColor = this.add(new ColorSetting("CrosshairColor", new Color(255, 255, 255, 255)));
    private final SliderSetting crosshairThick = this.add(new SliderSetting("CrosshairThick", 1.5, 0.5, 5.0, 0.1, () -> this.hideVanillaCrosshair.getValue() && this.crosshairMode.getValue() == CrosshairMode.Arcs));
    private final SliderSetting crosshairGap = this.add(new SliderSetting("CrosshairGap", 30.0, 0.0, 80.0, 1.0, () -> this.hideVanillaCrosshair.getValue() && this.crosshairMode.getValue() == CrosshairMode.Arcs));
    private final SliderSetting radius = this.add(new SliderSetting("Radius", 25.0, 10.0, 100.0, 1.0));
    private final SliderSetting thickness = this.add(new SliderSetting("Thickness", 4.0, 1.0, 20.0, 0.5));
    private final SliderSetting gap = this.add(new SliderSetting("GapDegree", 10.0, 0.0, 30.0, 1.0));
    private final SliderSetting glow = this.add(new SliderSetting("Glow", 8.0, 0.0, 20.0, 0.5));
    private final SliderSetting glowIntensity = this.add(new SliderSetting("GlowIntensity", 2.0, 1.0, 5.0, 1.0));
    private final SliderSetting globalAlpha = this.add(new SliderSetting("GlobalAlpha", 1.0, 0.1, 1.0, 0.01));
    private final SliderSetting fadeRange = this.add(new SliderSetting("FadeRange", 20.0, 0.0, 120.0, 1.0).setSuffix("m"));
    private final ColorSetting bgColor = this.add(new ColorSetting("BgColor", new Color(40, 40, 40, 150)));
    private final ColorSetting healthColor = this.add(new ColorSetting("HealthColor", new Color(255, 50, 50, 255)));
    private final ColorSetting absorbColor = this.add(new ColorSetting("AbsorbColor", new Color(255, 200, 0, 255)));
    private final ColorSetting turtleColor = this.add(new ColorSetting("TurtleColor", new Color(0, 200, 150, 255)));

    public CircularStatusHud() {
        super("CircularStatus", Module.Category.HUD);
        this.setChinese("\u73af\u5f62\u72b6\u6001\u680f");
        INSTANCE = this;
    }

    public static boolean shouldHideVanillaCrosshair() {
        return INSTANCE != null && INSTANCE.isOn() && CircularStatusHud.INSTANCE.hideVanillaCrosshair.getValue();
    }

    @Override
    public void onRender2D(GuiGraphics drawContext, float tickDelta) {
        this.renderElement(drawContext, tickDelta);
    }

    @Override
    public void renderElement(GuiGraphics context, float tickDelta) {
        if (CircularStatusHud.mc.player == null || mc.getWindow() == null) {
            return;
        }
        int centerX = mc.getWindow().getGuiScaledWidth() / 2;
        int centerY = mc.getWindow().getGuiScaledHeight() / 2;
        if (this.hideVanillaCrosshair.getValue() && this.crosshairMode.getValue() != CrosshairMode.None) {
            float size = this.crosshairSize.getValueFloat();
            if (this.crosshairMode.getValue() == CrosshairMode.Square) {
                float offset = size / 2.0f;
                context.method_25294((int)((float)centerX - offset), (int)((float)centerY - offset), (int)((float)centerX - offset + size), (int)((float)centerY - offset + size), this.crosshairColor.getValue().getRGB());
            } else if (this.crosshairMode.getValue() == CrosshairMode.SolidCircle) {
                float r = size / 4.0f;
                float thick = size / 2.0f;
                this.drawSkijaArc(context, centerX, centerY, r, thick, 0.0f, 360.0f, this.crosshairColor.getValue(), 0.0f, 0, 1.0f);
            } else if (this.crosshairMode.getValue() == CrosshairMode.Arcs) {
                float thick = this.crosshairThick.getValueFloat();
                float gapDeg = this.crosshairGap.getValueFloat();
                float sweep = 90.0f - gapDeg;
                if (sweep > 0.0f) {
                    float[] centers = new float[]{0.0f, 90.0f, 180.0f, 270.0f};
                    for (float cAngle : centers) {
                        float start = cAngle - sweep / 2.0f;
                        this.drawSkijaArc(context, centerX, centerY, size, thick, start, sweep, this.crosshairColor.getValue(), 0.0f, 0, 1.0f);
                    }
                }
            }
        }
        long currentTime = System.currentTimeMillis();
        if (this.lastRenderTime == 0L) {
            this.lastRenderTime = currentTime;
        }
        long delta = currentTime - this.lastRenderTime;
        this.lastRenderTime = currentTime;
        float fadeSpeed = (float)delta / 300.0f;
        float f = this.currentFade = this.hasPlayerInRange() ? Math.min(1.0f, this.currentFade + fadeSpeed) : Math.max(0.0f, this.currentFade - fadeSpeed);
        if (this.currentFade <= 0.0f) {
            return;
        }
        float gR = this.glow.getValueFloat();
        int gInt = this.glowIntensity.getValueInt();
        float r = this.radius.getValueFloat();
        float t = this.thickness.getValueFloat();
        float g = this.gap.getValueFloat();
        float maxSweep = 120.0f - g;
        float leftBgStart = 270.0f - g / 2.0f;
        float leftBgSweep = -maxSweep;
        int hpSteps = Math.round(CircularStatusHud.mc.player.getHealth());
        float hpRatio = Mth.method_15363((float)((float)hpSteps / 20.0f), (float)0.0f, (float)1.0f);
        float leftSweep = -maxSweep * hpRatio;
        float rightBgStart = 30.0f - g / 2.0f;
        float rightBgSweep = -maxSweep;
        int abSteps = Math.round(CircularStatusHud.mc.player.getAbsorptionAmount());
        float absorbRatio = Mth.method_15363((float)((float)abSteps / 16.0f), (float)0.0f, (float)1.0f);
        float rightSweep = -maxSweep * absorbRatio;
        float bottomBgStart = 30.0f + g / 2.0f;
        float bottomBgSweep = maxSweep;
        float turtleRatio = 0.0f;
        MobEffectInstance resistance = CircularStatusHud.mc.player.method_6112(MobEffects.DAMAGE_RESISTANCE);
        if (resistance != null && resistance.method_5578() >= 2) {
            int ticks = resistance.method_5584();
            int turtleSteps = Math.round((float)ticks / 400.0f * 100.0f);
            turtleRatio = Mth.method_15363((float)((float)turtleSteps / 100.0f), (float)0.0f, (float)1.0f);
        }
        float bottomSweep = maxSweep * turtleRatio;
        float currentGlobalAlpha = this.currentFade * this.globalAlpha.getValueFloat();
        if (this.bgColor.getValue().getAlpha() > 0) {
            this.drawSkijaArc(context, centerX, centerY, r, t, leftBgStart, leftBgSweep, this.bgColor.getValue(), gR, gInt, currentGlobalAlpha);
            this.drawSkijaArc(context, centerX, centerY, r, t, rightBgStart, rightBgSweep, this.bgColor.getValue(), gR, gInt, currentGlobalAlpha);
            this.drawSkijaArc(context, centerX, centerY, r, t, bottomBgStart, bottomBgSweep, this.bgColor.getValue(), gR, gInt, currentGlobalAlpha);
        }
        if (hpRatio > 0.0f) {
            this.drawSkijaArc(context, centerX, centerY, r, t, leftBgStart, leftSweep, this.healthColor.getValue(), gR, gInt, currentGlobalAlpha);
        }
        if (absorbRatio > 0.0f) {
            this.drawSkijaArc(context, centerX, centerY, r, t, rightBgStart, rightSweep, this.absorbColor.getValue(), gR, gInt, currentGlobalAlpha);
        }
        if (turtleRatio > 0.0f) {
            this.drawSkijaArc(context, centerX, centerY, r, t, bottomBgStart, bottomSweep, this.turtleColor.getValue(), gR, gInt, currentGlobalAlpha);
        }
    }

    private void drawSkijaArc(GuiGraphics context, float centerX, float centerY, float radius, float thickness, float startAngle, float sweepAngle, Color color, float glowRad, int glowIntensity, float alphaMultiplier) {
        if (Math.abs(sweepAngle) < 0.1f) {
            return;
        }
        if (!SkijaGlowRenderer.isAvailable()) {
            this.drawFallbackArc(context, centerX, centerY, radius, thickness, startAngle, sweepAngle, color, alphaMultiplier);
            return;
        }
        Color pureColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        ResourceLocation tex = SkijaGlowRenderer.createArcTexture(radius, thickness, startAngle, sweepAngle, pureColor, glowRad, glowIntensity);
        if (!SkijaGlowRenderer.isTextureReady(tex)) {
            this.drawFallbackArc(context, centerX, centerY, radius, thickness, startAngle, sweepAngle, color, alphaMultiplier);
            return;
        }
        int[] size = SkijaGlowRenderer.getTextureSize(tex);
        if (size == null) {
            this.drawFallbackArc(context, centerX, centerY, radius, thickness, startAngle, sweepAngle, color, alphaMultiplier);
            return;
        }
        int texWidth = size[0];
        int texHeight = size[1];
        double scale = mc.getWindow().getGuiScale();
        float displayWidth = (float)((double)texWidth / scale);
        float displayHeight = (float)((double)texHeight / scale);
        float drawX = centerX - displayWidth / 2.0f;
        float drawY = centerY - displayHeight / 2.0f;
        float finalAlpha = (float)color.getAlpha() / 255.0f * alphaMultiplier;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)finalAlpha);
        context.method_25290(tex, (int)drawX, (int)drawY, 0.0f, 0.0f, (int)displayWidth, (int)displayHeight, (int)displayWidth, (int)displayHeight);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    private void drawFallbackArc(GuiGraphics context, float centerX, float centerY, float radius, float thickness, float startAngle, float sweepAngle, Color color, float alphaMultiplier) {
        float alpha = Mth.method_15363((float)((float)color.getAlpha() / 255.0f * alphaMultiplier), (float)0.0f, (float)1.0f);
        if (alpha <= 0.01f) {
            return;
        }
        int argb = (int)(alpha * 255.0f) << 24 | color.getRed() << 16 | color.getGreen() << 8 | color.getBlue();
        float outer = Math.max(0.5f, radius + thickness / 2.0f);
        float inner = Math.max(0.0f, radius - thickness / 2.0f);
        int segments = Math.max(8, (int)(Math.abs(sweepAngle) / 4.0f));
        Matrix4f matrix = context.method_51448().last().method_23761();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::method_34540);
        BufferBuilder buffer = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= segments; ++i) {
            float t = (float)i / (float)segments;
            double angle = Math.toRadians(startAngle + sweepAngle * t);
            float cos = (float)Math.cos(angle);
            float sin = (float)Math.sin(angle);
            buffer.method_22918(matrix, centerX + cos * outer, centerY + sin * outer, 0.0f).method_39415(argb);
            buffer.method_22918(matrix, centerX + cos * inner, centerY + sin * inner, 0.0f).method_39415(argb);
        }
        BufferUploader.method_43433((MeshData)buffer.method_60800());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private boolean hasPlayerInRange() {
        if (CircularStatusHud.mc.level == null || CircularStatusHud.mc.player == null) {
            return false;
        }
        float range = this.fadeRange.getValueFloat();
        if (range <= 0.0f) {
            return true;
        }
        float rangeSq = range * range;
        for (Player player : CircularStatusHud.mc.level.method_18456()) {
            if (player == CircularStatusHud.mc.player || !player.isAlive() || !(player.method_5858((Entity)CircularStatusHud.mc.player) <= (double)rangeSq)) continue;
            return true;
        }
        return false;
    }

    @Override
    public String getElementName() {
        return this.getChinese() != null ? this.getChinese() : this.getName();
    }

    @Override
    public int getElementX() {
        if (mc.getWindow() == null) {
            return 0;
        }
        return mc.getWindow().getGuiScaledWidth() / 2 - this.getElementWidth() / 2;
    }

    @Override
    public void setElementX(int x) {
    }

    @Override
    public int getElementY() {
        if (mc.getWindow() == null) {
            return 0;
        }
        return mc.getWindow().getGuiScaledHeight() / 2 - this.getElementHeight() / 2;
    }

    @Override
    public void setElementY(int y) {
    }

    @Override
    public int getElementWidth() {
        return (int)(this.radius.getValueFloat() * 2.0f);
    }

    @Override
    public int getElementHeight() {
        return (int)(this.radius.getValueFloat() * 2.0f);
    }

    @Override
    public boolean isElementVisible() {
        return this.isOn();
    }

    public static enum CrosshairMode {
        None,
        Square,
        Arcs,
        SolidCircle;

    }
}

